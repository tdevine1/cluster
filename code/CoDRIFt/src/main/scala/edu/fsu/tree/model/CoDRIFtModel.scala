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

package edu.fsu.tree.model

import scala.collection.mutable.ListBuffer
import org.apache.spark.mllib.tree.model.DecisionTreeModel

class CoDRIFtModel( 
  protected var codrift: ListBuffer[(String, DecisionTreeModel, Double, Double)]
  )
  {
  
    codrift = new ListBuffer[(String, DecisionTreeModel, Double, Double)]()  
}