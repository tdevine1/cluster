/******************************************************************************
* CoDRIFt.scala
* @author zennisarix
* This is the implementation of CoDRIFt, a novel algorithm to create inductive
* Co-Training, Distributed, Random Incremental Forest (CoDRIFt) models for 
* classification on distributed systems. CoDRIFt iteratively creates an 
* inductive CoDRIFt model. The CoDrift model is a collection of trees that 
* operates much like a RandomForest, with two key differences:
* 1) each tree is created by a different RandomForest model during training 
* and selected as the ``best tree'', and 
* 2) when making a prediction, a CoDRIFt model weights predictions for the 
* positive class three times more than predictions for the negative class. 
* This form of weighted voting allows CoDRIFt models dealing with highly 
* imbalanced data to better identify the minority class (i.e., achieve higher
* recall) at the expense of lower precision. This trade-off is acceptable for 
* extremely rare instance classification , as the cost of missing a rare 
* instance (false negative) is much higher than that of having an extra false 
* positive.
******************************************************************************/
package edu.fsu.driver

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import java.io.StringWriter
import scala.collection.Seq
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.immutable.Map
import org.apache.spark.HashPartitioner
import org.apache.hadoop.mapreduce.lib.chain.Chain.KeyValuePair

object CoDRIFt {
  def main(args: Array[String]) {
    if (args.length < 7) {
      System.err.println("Must supply valid arguments: [numClasses] [numTrees] " +
        "[impurity] [maxDepth] [maxBins] [input filename] [output filename] " +
        "[percent labeled] [numExecutors]")
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
    val partitions = args(8).toInt * 2
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
      (i, iterator) => if (i == 0) iterator.drop(1) else iterator)
      
    // parse input text from CSV to RDD and convert to KVPRDD
    val kvprdd = transformCSV2KVPs(textNoHdr.map(line => line.split(",")))
   
    /**************************************************************************
     * Create training and testing sets.
     *************************************************************************/
    
    // Split the data into training and test sets (30% held out for testing)
    val startTimeSplit = System.nanoTime
    val (modelbuildingKVP, testingKVP) = stratifiedRandomSplit(kvprdd, 0.7)
    // Pull out another 30% for evaluation of models during training
    val (trainingKVP, evaluationKVP) = stratifiedRandomSplit(modelbuildingKVP, 0.7)
    // Create labeled and unlabeled sets.
    val (labeledKVP, unlabeledKVP) = stratifiedRandomSplit(trainingKVP, percentLabeled)
    val splitTime = (System.nanoTime - startTimeSplit) / 1e9d
    val labeledLP = transformKVPs2LabeledPoints(labeledKVP)
    val unlabeledLP = transformKVPs2UnlabeledPoints(unlabeledKVP)
    val evaluationLP = transformKVPs2LabeledPoints(evaluationKVP)
    labeledLP.repartition(partitions)
    labeledLP.persist()
    unlabeledLP.repartition(partitions)
    unlabeledLP.persist()
    evaluationLP.persist()
//    try {
//      hdfs.delete(new org.apache.hadoop.fs.Path("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "evaluationLP"), true)
//      evaluationLP.saveAsTextFile("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "evaluationLP")
//      hdfs.delete(new org.apache.hadoop.fs.Path("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "labeledLP"), true)
//      labeledLP.saveAsTextFile("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "labeledLP")
//      hdfs.delete(new org.apache.hadoop.fs.Path("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "unlabeledLP"), true)
//      unlabeledLP.saveAsTextFile("hdfs://master00.local:8020/data/results/" + survey + "/" + outName + "unlabeledLP")
//    } catch { 
//        case _ : Throwable => { println("ERROR: Unable to delete a file.")} 
//    }
    /**************************************************************************
     * 
     * 
     **************************************************************************/
    
    val startTimeTrain = System.nanoTime
    
    var codrift = new ListBuffer[(String, DecisionTreeModel, Double, Double)]()

    while(codrift.size < 75)
    {
      // Train supervised models on the labeled data
      val rfModelsLabeled = trainRFs(labeledLP, partitions, numTrees, impurity, maxDepth, maxBins)
      codrift = evaluateModels(codrift, rfModelsLabeled, evaluationLP, "labeled")
      // use models to label unlabeled points by partition and add them to the augmented set
      var augmented = createAugmentedSet(labeledLP, unlabeledLP, rfModelsLabeled, partitions)
      augmented.persist()
      // train new models on the augmented data
      val rfModelsAugmented = trainRFs(augmented, partitions, numTrees, impurity, maxDepth, maxBins)
      augmented.unpersist()
      codrift = evaluateModels(codrift, rfModelsAugmented, evaluationLP, "augmented")
    }
    val trainTime = (System.nanoTime - startTimeTrain) / 1e9d
    
    /**************************************************************************
     * Test the model on the held-out testing data.
     *************************************************************************/
    
    val startTimeTest = System.nanoTime
    val testingLP = transformKVPs2LabeledPoints(testingKVP)
    val labelAndPreds = codriftVoting(codrift, testingLP)
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
    out.write(s"\nLearned classification forest model:\n")
    // output trees
    var count = 1
    for ((source, tree, score, label) <- codrift)
    {
      out.write("\nTREE " + count + ": SOURCE=" + source + " LABEL=" + label + " RECALL=" + score  + "\n")
      out.write(tree.toDebugString)
      count += 1
    }
    
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
    val outRDD = sc.parallelize(Seq(out.toString()))
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
    val fractions = scala.collection.immutable.Map(1 -> trainPercent, 0 -> trainPercent)
    // get a stratified random subsample from the full set
    val train = kvPairs.sampleByKeyExact(false, fractions, System.nanoTime())
    // remove the elements of the training set from the full set
    val test = kvPairs.subtract(train)
    (train, test)
  }  
  /**************************************************************************
   *  
   * 	@param labeled data set to 
   *  @param numModels the number of models to create
   *  @return two key-value paired RDDs of LabeledPoints
   *************************************************************************/
  def trainRFs(
      labeled: RDD[LabeledPoint],
      numModels: Integer,
      numTrees: Integer,
      impurity: String,
      maxDepth: Integer,
      maxBins: Integer):
    ListBuffer[RandomForestModel] = {
    var rfModels = new ListBuffer[RandomForestModel]()
    for (i <- 1 to numModels)
    {
      val sample = labeled.sample(false, 1.0 / numModels)      
      if (sample.count() > 0)
      {
        val model = RandomForest.trainClassifier(sample, 2,
          scala.collection.immutable.Map[Int, Int](), numTrees, "auto", impurity,
          maxDepth, maxBins)
        rfModels += model
      }
    }
    rfModels
  }
  def createAugmentedSet(
      labeled: RDD[LabeledPoint],
      unlabeled: RDD[LabeledPoint],
      rfModels: ListBuffer[RandomForestModel],
      partitions: Int):
    RDD[LabeledPoint] = {  
    val rand = scala.util.Random
    var augmented = unlabeled.mapPartitionsWithIndex{(index, iter) =>
       val points = iter.toList
       val model = rfModels(rand.nextInt(rfModels.size))
       points.map{point =>
         if (model != null)
         {
           val (prediction, confidence) = model.predict(point.features)
           if (confidence >= 0.95)
             new LabeledPoint(prediction, point.features) 
           else
             point
         }
         else
           point
      }.iterator
    }
    augmented = augmented.filter(_.label != -1)
    // combine labeled and augmented sets
    augmented.union(labeled)
    augmented.repartition(partitions)
  }    
  def codriftVoting(
      codrift: ListBuffer[(String, DecisionTreeModel, Double, Double)],
      evalSet: RDD[LabeledPoint]):
    RDD[(Double, Double)] = {
    evalSet.map{ point =>
      val votes = scala.collection.mutable.Map.empty[Int, Double]
      for ((source, tree, score, label) <- codrift)
      {
        val prediction = tree.predict(point.features).toInt
        votes(prediction) = votes.getOrElse(prediction, 0.0) + 3 * label
      }
      (point.label, votes.maxBy(_._2)._1.toDouble)
    }
  }

  def evaluateModels(
      codrift: ListBuffer[(String, DecisionTreeModel, Double, Double)],
      models: ListBuffer[RandomForestModel],
      evalSet: RDD[LabeledPoint],
      source: String):
    ListBuffer[(String, DecisionTreeModel, Double, Double)] = {        
    for (model <- models)
    {
      var bestPosScore = 0.0
      var bestNegScore = 0.0
      var bestPosTree = model.trees(0)
      var bestNegTree = model.trees(0)
      val trees = model.trees
      for (tree <- trees)
      {
        val labelAndPreds = evalSet.map { point =>
          val prediction = tree.predict(point.features)
          (point.label, prediction)
        }
        labelAndPreds.persist()
        val metrics = new MulticlassMetrics(labelAndPreds)
        val negScore = metrics.recall(0.0)
        if (negScore > bestNegScore)
        {
          bestNegTree = tree
          bestNegScore = negScore
        }
        if (metrics.labels.size > 1)
        {
          val posScore = metrics.recall(1.0)
          if (posScore > bestPosScore)
          {
            bestPosTree = tree
            bestPosScore = posScore
          }
        } 
        labelAndPreds.unpersist()
      }
      if (bestPosScore > 0.9)
        codrift += ((source, bestPosTree, bestPosScore, 1.0))
      if (bestNegScore > 0.99)
        codrift += ((source, bestNegTree, bestNegScore, 0.0))
      }
      codrift
  }
    
}
    
    
    
    
    