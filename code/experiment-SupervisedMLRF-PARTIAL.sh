#! /bin/bash

driver_memory="2048m"
executor_memory="2048m"
executor_cores=2
mem_overhead=972
num_classes=2
count=1
for repetition in {1..5}; do
  for num_trees in {25..200..25}; do
    num_executors=1
    percent_labeled=1
    echo       Run $count
    echo       SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/palfa/palfa_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    ((count++))
    percent_labeled=10
    echo       Run $count
    echo       SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/palfa/palfa_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    ((count++))
    for num_executors in {5..25..5}; do
      echo       Run $count
      percent_labeled=1
      echo       Run $count
      echo       SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/palfa/palfa_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
      ((count++))
      percent_labeled=10
      echo       Run $count
      echo       SupervisedMLRF-classes.$num_classes-exec.1-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    spark-submit --class edu.fsuhpc.driver.SupervisedMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --queue default /home/hduser/workspace-scala/SupervisedMLRF/target/SupervisedMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 /data/palfa/palfa_2class_labeled.csv SupervisedMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    done
  done
done
