#! /bin/bash

driver_memory="2048m"
executor_memory="2048m"
executor_cores=2
mem_overhead=972
num_classes=2
count=1
num_executors=1
percent_labeled=1

for num_trees in {10..200..10}; do
  for repetition in {1..10}; do
    echo       Run $count
    echo       SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition
    spark-submit --class edu.fsu.driver.SelfLearningMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --conf spark.driver.userClassPathFirst=true --conf spark.executor.userClassPathFirst=true --queue default --jars /home/hduser/workspace-scala/spark-mllib-patched_2.11/target/spark-mllib-patched_2.11-2.3.2.jar /home/hduser/workspace-scala/SelfLearningMLRF/target/SelfLearningMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 gbt350drift SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
    ((count++))
  done
  for num_executors in {5..25..5}; do
      for repetition in {1..10}; do
        echo       Run $count
	echo       SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        spark-submit --class edu.fsu.driver.SelfLearningMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --conf spark.driver.userClassPathFirst=true --conf spark.executor.userClassPathFirst=true --queue default --jars /home/hduser/workspace-scala/spark-mllib-patched_2.11/target/spark-mllib-patched_2.11-2.3.2.jar /home/hduser/workspace-scala/SelfLearningMLRF/target/SelfLearningMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 gbt350drift SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        ((count++))
      done
  done
  for percent_labeled in {5..15..5}; do
    num_executors=1
    for repetition in {1..10}; do
      echo       Run $count
      echo       SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition
      spark-submit --class edu.fsu.driver.SelfLearningMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --conf spark.driver.userClassPathFirst=true --conf spark.executor.userClassPathFirst=true --queue default --jars /home/hduser/workspace-scala/spark-mllib-patched_2.11/target/spark-mllib-patched_2.11-2.3.2.jar /home/hduser/workspace-scala/SelfLearningMLRF/target/SelfLearningMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 gbt350drift SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
      ((count++))
    done
    for num_executors in {5..25..5}; do
      for repetition in {1..10}; do
        echo       Run $count
        echo       SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        spark-submit --class edu.fsu.driver.SelfLearningMLRF --master yarn --deploy-mode cluster --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.executor.memoryOverhead=$mem_overhead --conf spark.driver.userClassPathFirst=true --conf spark.executor.userClassPathFirst=true --queue default --jars /home/hduser/workspace-scala/spark-mllib-patched_2.11/target/spark-mllib-patched_2.11-2.3.2.jar /home/hduser/workspace-scala/SelfLearningMLRF/target/SelfLearningMLRF-0.0.1-SNAPSHOT.jar $num_classes $num_trees "gini" 4 32 gbt350drift SelfLearningMLRF-classes.$num_classes-exec.$num_executors-trees.$num_trees-labeled.$percent_labeled-rep.$repetition $percent_labeled
        ((count++))
      done
    done
  done
done
