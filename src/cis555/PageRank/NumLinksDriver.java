package cis555.PageRank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class NumLinksDriver {
    
    private void countOutgoingLinks(String inputPath, String outputPath) {
   	Configuration conf = new Configuration();
   	try {
   	    Job job = Job.getInstance(conf, "initialize pagerank");
   	    job.setJarByClass(NumLinksDriver.class);
   	    job.setMapperClass(NumLinksMapper.class);
   	    job.setReducerClass(NumLinksReducer.class);

   	    job.setMapOutputKeyClass(IntWritable.class);
   	    job.setMapOutputValueClass(Text.class); // changed
   	    job.setOutputKeyClass(IntWritable.class);
   	    job.setOutputValueClass(Text.class);

   	    FileInputFormat.addInputPath(job, new Path(inputPath));
   	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
   	    
   	    job.waitForCompletion(true);
   	} catch (Exception e) {
   	    e.printStackTrace();
   	}
       }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	String crawlInput = args[0];
	String output = args[1];

	NumLinksDriver cd = new NumLinksDriver();
	cd.countOutgoingLinks(crawlInput, output);
    }
}
