package cis555.PageRank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankDriver {
    
    private void rank(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "recompute pagerank");
	    job.setJarByClass(PageRankDriver.class);
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
   	    job.setJarByClass(PageRankDriver.class);
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
   	    job.setJarByClass(PageRankDriver.class);
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
	PageRankDriver cd = new PageRankDriver();
	
	String crawlInput = "";
	String inputBase = "";
	String output = "";
	
	if (args.length == 1) {
	    boolean aws = false;
	    if (args[0].equals("1")) {
		aws = true;
	    }
	    if (aws) {
		crawlInput = "s3://wcbucket555/crawlinput";
		inputBase = "s3://wcbucket555/tempinput";
		output = "s3://wcbucket555/output";
	    }
	    else {
		crawlInput = "pr/crawlinput"; // from crawl (rawest input)
		inputBase = "pr/input"; // after initializing pageranks
		output = "pr/output";
	    }
	}
	else {
	    crawlInput = args[0];
	    inputBase = args[1];
	    output = args[2];
	}
	
	if (inputBase.endsWith("/")) {
	    inputBase += "tempinput";
	}
	else {
	    inputBase += "/tempinput";
	}
	cd.initialize(crawlInput, inputBase + "0");
	
	int numIter = 14;
        for (int run = 0; run < numIter; run += 1) {
            cd.rank(inputBase+run, inputBase+(run + 1));
        }
        
        cd.clean(inputBase + numIter, output);

    }
}
