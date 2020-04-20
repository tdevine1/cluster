#! /bin/bash

driver_memory="2048m"
executor_memory="2048m"
executor_cores=2
mem_overhead=972
num_classes=2
count=1

for percent_labeled in {5..95..10}; do
  for num_trees in {10..200..10}; do
    for repetition in {1..10}; do
      echo Run $count
      echo SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
      spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors 1 --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/gbt350drift/gbt350drift_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
      ((count++))
    done
    for num_executors in {5..25..5}; do
      for repetition in {1..10}; do
        echo Run $count
	echo SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/gbt350drift/gbt350drift_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        ((count++))
      done
    done
  done
done
