MEM=1000m
BASE=/home/tomanek/.m2/repository
CP=$BASE/log4j/log4j/1.2.14/log4j-1.2.14.jar:target/classes:.

java -Xmx$MEM -cp $CP de.julielab.segmentationEvaluator.EvaluationApplication $*