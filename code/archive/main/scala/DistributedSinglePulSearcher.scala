/* DistributedSinglePulSearcher.scala:
   This is the driver for the Java implementation of SinglePulSearcher in a distributed environment.
*/
package scaladrivers

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

import datahandlers.DistributedPulSearcher
 
object DistributedSinglePulSearcher {
  
  private def singlepulsearch(cluster: String, data: RDD[String]): Array[String] = {
    val dps: DistributedPulSearcher = new DistributedPulSearcher(cluster, data.collect().toArray)
    dps.run()
    dps.getCandidates()
  }
  
  private def filterData(cluster: String, datum: String): Boolean = {
    val clusterFields = cluster.split(",")
    val clusterID = clusterFields(0) + "," + clusterFields(1) + "," + clusterFields(2) + "," + clusterFields(3)
    val startDM = clusterFields(13).toDouble
    val stopDM = clusterFields(14).toDouble
    val startTime = clusterFields(15).toDouble
    val stopTime = clusterFields(16).toDouble
    
    val dataFields = datum.split(",")
    val dataDM = dataFields(4).toDouble
    val dataTime = dataFields(6).toDouble

    datum.contains(clusterID) && (startDM <= dataDM) && (dataDM <= stopDM) && (startTime <= dataTime) && (dataTime <= stopTime)
  }  
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Must supply valid input files: [cluster_file] [data_file] [output_file]")
      System.exit(1)
    }
    val sparkConf = new SparkConf().setAppName("DistributedSinglePulSearcher")
    val sc = new SparkContext(sparkConf)
    val clusterFile = sc.textFile(args(0))
    val dataFile = sc.textFile(args(1))
    val results = clusterFile.flatMap ( cluster => singlepulsearch(cluster, dataFile.filter { datum => filterData(cluster, datum)}))
    results.saveAsTextFile(args(2))
    sc.stop()
  }
}

