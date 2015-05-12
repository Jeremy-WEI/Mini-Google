package cis555.PageRank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankOneIterDriver {
    
    private void rank(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "recompute pagerank");
	    job.setJarByClass(PageRankOneIterDriver.class);
	    job.setMapperClass(PageRankMapper.class);
	    job.setReducerClass(PageRankReducer.class);

	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(Text.class); // changed
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);

	    FileInputFormat.addInputPath(job, new Path(inputPath));
	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
	    job.waitForCompletion(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    
    private void initialize(String inputPath, String outputPath) {
   	Configuration conf = new Configuration();
   	try {
   	    Job job = Job.getInstance(conf, "initialize pagerank");
   	    job.setJarByClass(PageRankOneIterDriver.class);
   	    job.setMapperClass(PageRankInitMapper.class);
   	    job.setReducerClass(PageRankInitReducer.class);

   	    job.setMapOutputKeyClass(Text.class);
   	    job.setMapOutputValueClass(Text.class); // changed
   	    job.setOutputKeyClass(Text.class);
   	    job.setOutputValueClass(Text.class);

   	    FileInputFormat.addInputPath(job, new Path(inputPath));
   	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
   	    job.waitForCompletion(true);
   	} catch (Exception e) {
   	    e.printStackTrace();
   	}
       }
    
    private void clean(String inputPath, String outputPath) {
   	Configuration conf = new Configuration();
   	try {
   	    Job job = Job.getInstance(conf, "initialize pagerank");
   	    job.setJarByClass(PageRankOneIterDriver.class);
   	    job.setMapperClass(PageRankCleanMapper.class);
   	    job.setReducerClass(PageRankCleanReducer.class);

   	    job.setMapOutputKeyClass(Text.class);
   	    job.setMapOutputValueClass(Text.class); // changed
   	    job.setOutputKeyClass(Text.class);
   	    job.setOutputValueClass(Text.class);

   	    FileInputFormat.addInputPath(job, new Path(inputPath));
   	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
   	    job.waitForCompletion(true);
   	} catch (Exception e) {
   	    e.printStackTrace();
   	}
       }
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	PageRankOneIterDriver cd = new PageRankOneIterDriver();
	
	String crawlInput = args[0];
	String output = args[1];
	
	// cd.initialize(crawlInput, inputBase + "0");
	
	cd.rank(crawlInput, output);
	
        //cd.clean(inputBase + numIter, output);

    }
}
