# IF DOING LOCALLY

#You need Hadoop's runtime.  I have placed my hadoop-2.6.0 dir in /home/cis455/workspace/MRExample

#Arguments:
#1: MapReduce JAR (created from createpagerankJAR.sh)
#2: main class in JAR
#3: input directory to MapReduce.  This obviously must exist already (there should be files there)
#4: temporary directory to MapReduce.  This must exist already as each iteration will create
#   a directory within this one called tempinputi where i is the iteration number.  These tempinputi directories
#   must not exist before this is run
#5: final output directory to MapReduce.  Cannot exist already.

export JAVA_HOME=/usr

/home/cis455/workspace/MRExample/hadoop-2.6.0/bin/hadoop jar /home/cis455/workspace/prstuff/pagerank.jar cis555.PageRank.PageRankDriver /home/cis455/workspace/prstuff/prdata/input /home/cis455/workspace/prstuff/prdata/intermediate /home/cis455/workspace/prstuff/prdata/output


# IF DOING ON AWS
# 1) upload the JAR to S3
# 2) In the EMR cluster, add a step (custom JAR)
# 3) Choose your JAR, and for the arguments: main class, input dir, temporary dir, output dir
# note same rules apply: input dir must exist and have files, temporary dir must exist but nothing can be inside, and output dir cannot exist.  All these directories obviously need to be on S3
