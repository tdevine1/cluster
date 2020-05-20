spark-submit --class edu.fsu.driver.SelfLearningMLRF --master yarn --deploy-mode cluster --driver-memory 2048m --num-executors 20 --executor-memory 2048m --executor-cores 2 --conf spark.executor.memoryOverhead=972 --queue default /home/hduser/workspace-scala/SelfLearningMLRF/target/SelfLearningMLRF-0.0.1-SNAPSHOT.jar 2 100 gini 4 32 /data/gbt350drift/gbt350drift_2class_labeled.csv SelfLearningMLRF-classes.2-exec.20-trees.100-labeled.10-rep.1 10

