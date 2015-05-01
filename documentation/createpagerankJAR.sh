rm -rf /home/cis455/workspace/prstuff/pagerank_classes
mkdir /home/cis455/workspace/prstuff/pagerank_classes
#need hadoop-common-2.6.0.jar and hadoop-mapreduce-client-core-2.6.0.jar
javac -classpath ../lib/\* -d /home/cis455/workspace/prstuff/pagerank_classes ../src/cis555/PageRank/*
jar -cvf /home/cis455/workspace/prstuff/pagerank.jar -C /home/cis455/workspace/prstuff/pagerank_classes .
