/* DSPS.scala:
   This is the driver for the Java implementation of SinglePulSearcher in a distributed environment.
*/
package edu.fsuhpc.dsps.drivers

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.HashPartitioner
import scala.collection.mutable.ArrayBuffer
import edu.fsuhpc.dsps.datahandlers.DistributedPulSearcher
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
 
object DSPS {  
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Must supply valid input files: [cluster_file] [data_file] [output_file]")
      System.exit(1)
    }

    val sparkConf = new SparkConf().setAppName("DistributedSinglePulSearcher")
    val sc = new SparkContext(sparkConf)
    
    //  create the hash partitioner with 32 partitions for each core. 7 nodes x 4 cores x 32 = 896 partitions    
    val partitioner = new HashPartitioner(896)

    // create aggregation functions
    val initialSet = ArrayBuffer.empty[String]
    val addToSet = (s: ArrayBuffer[String], v: String) => s += v
    val mergePartitionSets = (p1: ArrayBuffer[String], p2: ArrayBuffer[String]) => p1 ++ p2

    // convert both files to key-value pairs identified by ([survey]_[mjd]_[pointing]_[beam], [rem of string])
    // then aggregate them by key to (id1, (val1, val2, ..., valN))
  
    val clusterFileRaw = sc.textFile(args(0))
    // remove header
    val clusterFile = clusterFileRaw.mapPartitionsWithIndex(
      (i, iterator) => if (i == 0 && iterator.hasNext) { 
        iterator.next 
        iterator 
      } else iterator)
      
    val clusterPairs = clusterFile.map(cluster => makeKVP(cluster)).partitionBy(partitioner)
    clusterFile.unpersist(true)
    val clusterAggr = clusterPairs.aggregateByKey(initialSet)(addToSet, mergePartitionSets).partitionBy(partitioner).cache()
    clusterPairs.unpersist(true)
        
    val dataFileRaw = sc.textFile(args(1))
    
    // remove header
    val dataFile = dataFileRaw.mapPartitionsWithIndex(
      (i, iterator) => if (i == 0 && iterator.hasNext) { 
        iterator.next 
        iterator 
      } else iterator) 
        
    val dataPairs = dataFile.map(data => (makeKVP(data))).partitionBy(partitioner)
    dataFile.unpersist(true)
    val dataAggr = dataPairs.aggregateByKey(initialSet)(addToSet, mergePartitionSets).partitionBy(partitioner).cache()
    dataPairs.unpersist(true)    
    
    // left outer join the 2 RDDs by the keys to make:
    //   (id_1, (([cluster1], [cluster2], ..., [clusterN]), Some([data1],[data2],...,[dataN])))
    val joinedPairs = clusterAggr.leftOuterJoin(dataAggr).cache()
    clusterAggr.unpersist(true)
    dataAggr.unpersist(true)
    
    val results = joinedPairs.flatMap {case(key, value) => search(key, value)}
    joinedPairs.unpersist(true)
    
//    println("Results: ")
//    results.collect.map(x => println(x))
    
    val hadoopConf = new org.apache.hadoop.conf.Configuration()
    val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://master00.local:8020"), hadoopConf)
    val output = "hdfs://master00.local:8020" + args(2)
    try {
        hdfs.delete(new org.apache.hadoop.fs.Path(output), true)
      } catch { 
          case _ : Throwable => { println("ERROR: Unable to delete " + output)} 
      }
    results.saveAsTextFile(args(2))
    
    sc.stop()
  }// end main
  
  private def makeKVP(str: String): (String, String) = {
    val strAry = str.split(",")
    val key = strAry(0) + "," + strAry(1) + "," + strAry(2).replaceAll("G", "") + "," + strAry(3).replaceAll("b", "")
    val splitValue = "," + strAry(3) + ","
    val value = str.substring(str.indexOf(splitValue) + splitValue.length)
    (key, value)
  }// end makeKVP
    
  private def matchFilter(key: String, cluster: String, data: Array[String]): Array[String] = {
    val matches = ArrayBuffer.empty[String]
    val clusterFields = cluster.split(",")
    val startDM = clusterFields(9).toDouble
    val stopDM = clusterFields(10).toDouble
    val startTime = clusterFields(11).toDouble
    val stopTime = clusterFields(12).toDouble
    
    var matchFound = false
    for (datum <- data)
    {
      val dataFields = datum.split(",")
      if (dataFields.length > 2)
      {
        try {
          val dataDM = dataFields(0).toDouble
          val dataTime = dataFields(2).toDouble
          if ((startDM <= dataDM) && (dataDM <= stopDM) && (startTime <= dataTime) && (dataTime <= stopTime))
          {
            matches += datum
            matchFound = true
          }
        } catch
        {
          case nfe: NumberFormatException => println("ERROR: NumberFormatException for " + dataFields(0) + " or " + dataFields(2))
                    nfe.printStackTrace()
          case _:   Throwable => println("ERROR: Unknown Exception Thrown!")
        }        
      }
    }
    if (!matchFound)
    	println("\tNo matches found for " + key + "," + cluster)
    matches.toArray
  }// end matchFilter
      
  private def search(key: String, value: (ArrayBuffer[String], Option[ArrayBuffer[String]])): Array[String] = {
    val candidates = ArrayBuffer.empty[String]
    val cluster = value._1
    val data = value._2.getOrElse[ArrayBuffer[String]](ArrayBuffer.empty[String])
    for (cl <- cluster.toArray)
    {
      val subArray = matchFilter(key, cl, data.toArray)      
      val dps: DistributedPulSearcher = new DistributedPulSearcher(key + "," + cl, subArray)
      dps.run()
      for (c <- dps.getCandidates())
        if (c != "") candidates += c.toString();
    }
    candidates.toArray
  }// end search
}// end DSPS
