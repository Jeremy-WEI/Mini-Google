package cis555.PageRank.utils;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// this counts the number of duplicates per docID in the links file
public class CountDriver {
    
    private void initialize(String inputPath, String outputPath) {
   	Configuration conf = new Configuration();

   	try {
   	    Job job = Job.getInstance(conf, "count number of instances per docID");
   	    job.setJarByClass(CountDriver.class);
   	    job.setMapperClass(CountMapper.class);
   	    job.setReducerClass(CountReducer.class);

   	    job.setMapOutputKeyClass(Text.class);
   	    job.setMapOutputValueClass(IntWritable.class); // changed
   	    job.setOutputKeyClass(Text.class);
   	    job.setOutputValueClass(IntWritable.class);

   	    FileInputFormat.addInputPath(job, new Path(inputPath));
   	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
   	    job.waitForCompletion(true);
   	} catch (Exception e) {
   	    e.printStackTrace();
   	}
       }
    
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	CountDriver cd = new CountDriver();
	
	// the below needs to be fixed, it is old code
	
	boolean aws = false;
	if (args[0].equals("1")) {
	    aws = true;
	}
	
	String crawlInput = "pr/crawlinput"; // from crawl (rawest input)
	String output = "pr/counted";
	if (aws) {
	    crawlInput = "s3://wcbucket555/crawlinput";
	    output = "s3://wcbucket555/output";
	}
	
	cd.initialize(crawlInput, output);

    }
}
