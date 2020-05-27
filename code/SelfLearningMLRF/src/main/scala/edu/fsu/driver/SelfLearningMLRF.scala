/******************************************************************************
* SelfLearningMLRF.scala
* @author zennisarix
* This code implements a self learning version of the standard mllib RandomForest
* algorithm. It works by training a model on labeled data, using it to label 
* the unlabeled data, keeping the highest confidence labels and adding them to 
* the test set.
******************************************************************************/
package edu.fsu.driver

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import java.io.StringWriter
import scala.collection.Seq

object SelfLearningMLRF {
  def main(args: Array[String]) {
    if (args.length < 7) {
      System.err.println("Must supply valid arguments: [numClasses] [numTrees] " +
        "[impurity] [maxDepth] [maxBins] [input filename] [output filename] " +
        "[percent labeled]")
      System.exit(1)
    }
    // setup parameters from command line arguments
    val numClasses = args(0).toInt
    val numTrees = args(1).toInt 
    val impurity = args(2)
    val maxDepth = args(3).toInt
    val maxBins = args(4).toInt
    val survey = args(5)
    val inFile = "/data/" + survey + "/" + survey + "_2class_labeled.csv"
    val outName = args(6)
    val outFile =  "hdfs://master00.local:8020/data/results/" + survey + "/" + outName
    val percentLabeled = args(7).toDouble * 0.01
    val MAX_ITERATIONS = 25
    // initialize spark 
    val sparkConf = new SparkConf().setAppName("SupervisedMLRF")
    val sc = new SparkContext(sparkConf)
    
    // configure hdfs for output
    val hadoopConf = new org.apache.hadoop.conf.Configuration()
    val hdfs = org.apache.hadoop.fs.FileSystem.get(
          new java.net.URI("hdfs://master00.local:8020"), hadoopConf
        )
        
    val out = new StringWriter()
    
    /**************************************************************************
     * Read in data and prepare for sampling
     *************************************************************************/
    
    // load data file from hdfs
    val text =  sc.textFile(inFile)

    // remove header line from input file
    val textNoHdr = text.mapPartitionsWithIndex(
      (i, iterator) => if (i == 0 && iterator.hasNext) { 
        iterator.next 
        iterator 
      } else iterator)
      
    // parse input text from CSV to RDD and convert to KVPRDD
    val kvprdd = transformCSV2KVPs(textNoHdr.map(line => line.split(",")))
   
    /**************************************************************************
     * Create training and testing sets.
     *************************************************************************/
    
    // Split the data into training and test sets (30% held out for testing)
    val startTimeSplit = System.nanoTime
    val (trainingKVP, testingKVP) = stratifiedRandomSplit(kvprdd, 0.7)
    
    // Create labeled and unlabeled sets.
    val (labeledKVP, unlabeledKVP) = stratifiedRandomSplit(trainingKVP, percentLabeled)
    val splitTime = (System.nanoTime - startTimeSplit) / 1e9d
    
    var labeledLP = transformKVPs2LabeledPoints(labeledKVP)
    var unlabeledLP = transformKVPs2UnlabeledPoints(unlabeledKVP)
    
    /**************************************************************************
     * General Self-Learning Algorithm:
     * 	while (unlabeled set is not empty and max iterations not reached)
     * 		Train the base classifier on the labeled set
     * 		for each element of unlabeled set
     * 			label with confidence value
     * 		sort newly labeled instances by confidence
     * 		add the highest confidence instances to the labeled set
     * 
     **************************************************************************/
    
    val categoricalFeaturesInfo = Map[Int, Int]()
    val featureSubsetStrategy = "auto"     
    var iteration = 1
    var noGoodPredictionsCount = 0
    // Train the initial Spark mllib RandomForest model on the labeled training data.
    var model = RandomForest.trainClassifier(labeledLP, numClasses,
        categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity,
        maxDepth, maxBins)
    val startTimeTrain = System.nanoTime
    while (noGoodPredictionsCount < 3 && iteration < MAX_ITERATIONS && unlabeledLP.count > 0)
    {     
      // Label the highest confidence predictions
      var predictions = unlabeledLP.map { point =>
        var (prediction, confidence) = model.predict(point.features)
        if (confidence >= 0.98)
          new LabeledPoint(prediction, point.features)
        else
          point
      }
      // If there are any good predictions, add them to the labeled set, remove
      //   them from the unlabeled set
      var goodPredictions = predictions.filter(_.label != -1)
      
      out.write("****************************************************\n")
      out.write("*Iteration: " + iteration + "\n")
      out.write("*Size of Labeled Set: " + labeledLP.count + "\n")
      out.write("*Size of Unlabeled Set: " + unlabeledLP.count + "\n")
      out.write("*Number of Good Preditions: " + goodPredictions.count + "\n")
      
      if (goodPredictions.count > 0)
      {
        noGoodPredictionsCount = 0
        labeledLP = labeledLP ++ goodPredictions
        unlabeledLP = predictions.filter(_.label == -1)
        model = RandomForest.trainClassifier(labeledLP, numClasses,
          categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity,
          maxDepth, maxBins)
      }
      else
        noGoodPredictionsCount += 1
      iteration += 1
    }
    val trainTime = (System.nanoTime - startTimeTrain) / 1e9d
    
    /**************************************************************************
     * Test the model on the held-out testing data.
     *************************************************************************/
    
    val startTimeTest = System.nanoTime
    val testingLP = transformKVPs2LabeledPoints(testingKVP)
    val labelAndPreds = testingLP.map { point =>
      val (prediction, confidence) = model.predict(point.features)
      (point.label, prediction)
    }
    val testTime = (System.nanoTime - startTimeTest) / 1e9d
    
    /**************************************************************************
     * Metrics calculation for classification and execution performance
     *  evaluations.
     *************************************************************************/    
    
    val metrics = new MulticlassMetrics(labelAndPreds)    
    out.write("EXECUTION PERFORMANCE:\n")
    out.write("SplittingTime=" + splitTime + "\n")
    out.write("TrainingTime=" + trainTime + "\n")
    out.write("TestingTime=" + testTime + "\n\n")
    
    out.write("CLASSIFICATION PERFORMANCE:\n")
    // Confusion matrix
    out.write("Confusion matrix (predicted classes are in columns):\n")
    out.write(metrics.confusionMatrix + "\n")
    
    // Overall Statistics
    val accuracy = metrics.accuracy
    out.write("\nSummary Statistics:\n")
    out.write(s"Accuracy = $accuracy\n")
    
    // Precision by label
    val labels = metrics.labels
    labels.foreach { l =>
      out.write(s"Precision($l) = " + metrics.precision(l) + "\n")
    }
    
    // Recall by label
    labels.foreach { l =>
      out.write(s"Recall($l) = " + metrics.recall(l) + "\n")
    }
    
    // False positive rate by label
    labels.foreach { l =>
      out.write(s"FPR($l) = " + metrics.falsePositiveRate(l) + "\n")
    }
    
    // F-measure by label
    labels.foreach { l =>
      out.write(s"F1-Score($l) = " + metrics.fMeasure(l) + "\n")
    }
    
    // Weighted stats
    out.write(s"\nWeighted precision: ${metrics.weightedPrecision}\n")
    out.write(s"Weighted recall: ${metrics.weightedRecall}\n")
    out.write(s"Weighted F1 score: ${metrics.weightedFMeasure}\n")
    out.write(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}\n")
    
    // output trees
    out.write(s"\nLearned classification forest model:\n ${model.toDebugString}\n")
    
    // output training and testing data
    //    println("TRAINING DATA:\n")
    //    trainingData.collect().map(println)
    //    println("TESTING DATA:\n")
    //    testingData.collect().map(println)
        
    // delete current existing file for this model
    try {
        hdfs.delete(new org.apache.hadoop.fs.Path(outFile), true)
    } catch { 
        case _ : Throwable => { println("ERROR: Unable to delete " + outFile)} 
    }
    
    // write string to file
    val outRDD= sc.parallelize(Seq(out.toString()))
    outRDD.saveAsTextFile(outFile)
    
    sc.stop()
  }
  /**************************************************************************
   * Converts an RDD of string arrays into an RDD of key-value pairs.
   * rdd: the dataset to convert, read in from a CSV
   * Returns: RDD of key-value pairs, with the key as the last value in a row
   * 		and the value as everything before the key
   **************************************************************************/
  def transformCSV2KVPs(
      rdd: RDD[Array[String]]):
      RDD[(Int, scala.collection.immutable.IndexedSeq[Double])] = {
    rdd.map(row => (
        row.last.toInt, 
        (row.take(row.length - 1).map(str => str.toDouble)).toIndexedSeq
      )
    )    
  }
  /**************************************************************************
   * Converts an RDD of key-value pairs into an RDD of LabeledPoints.
   * @param rdd the dataset to convert
   * @return RDD of LabeledPoints, with the label as the key
   **************************************************************************/
  def transformKVPs2LabeledPoints(
      kvPairs: RDD[(Int, scala.collection.immutable.IndexedSeq[Double])]):
      RDD[LabeledPoint] = {        
      kvPairs.map(pair => new LabeledPoint(pair._1, 
         Vectors.dense(pair._2.toArray)))
  }
  /**************************************************************************
   * Converts an RDD of key-value pairs into an RDD of LabeledPoints with all
   * 	labels equal to -1.
   * @param rdd the dataset to convert
   * @return RDD of LabeledPoints, with the label as the key
   **************************************************************************/
  def transformKVPs2UnlabeledPoints(
      kvPairs: RDD[(Int, scala.collection.immutable.IndexedSeq[Double])]):
      RDD[LabeledPoint] = {        
      kvPairs.map(pair => new LabeledPoint(-1, 
         Vectors.dense(pair._2.toArray)))
  }
  /**************************************************************************
   * Splits a key-value pair dataset into two stratified sets. The size of
   * the sets are user-determined. 
   * 	@param rdd the dataset to split
   *  @param trainPercent the size in percent of the training set
   *  @return two key-value paired RDDs of LabeledPoints
   *************************************************************************/
  def stratifiedRandomSplit(
      kvPairs: RDD[(Int, scala.collection.immutable.IndexedSeq[Double])],
      trainPercent: Double):
      (RDD[(Int, scala.collection.immutable.IndexedSeq[Double])],
       RDD[(Int, scala.collection.immutable.IndexedSeq[Double])]) = {    
    // set the size of the training set    
    val fractions = Map(1 -> trainPercent, 0 -> trainPercent)
    // get a stratified random subsample from the full set
    val train = kvPairs.sampleByKeyExact(false, fractions, System.nanoTime())
    // remove the elements of the training set from the full set
    val test = kvPairs.subtract(train)
    (train, test)
  }
}
