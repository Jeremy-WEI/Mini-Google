package cis555.PageRank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class RemoveSinksDriver {
    
    private void removeSinksWeCrawled(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "remove sinks we crawled");
	    job.setJarByClass(RemoveSinksDriver.class);
	    job.setMapperClass(RemoveSinksWeCrawledMapper.class);
	    job.setReducerClass(RemoveSinksWeCrawledReducer.class);

	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(Text.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);

	    FileInputFormat.addInputPath(job, new Path(inputPath));
	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
	    job.waitForCompletion(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void removeApparentSinksStepOne(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "remove apparent sinks step one");
	    job.setJarByClass(RemoveSinksDriver.class);
	    job.setMapperClass(RemoveApparentSinksStepOneMapper.class);
	    job.setReducerClass(RemoveApparentSinksStepOneReducer.class);

	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(Text.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);

	    FileInputFormat.addInputPath(job, new Path(inputPath));
	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
	    job.waitForCompletion(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void removeApparentSinksStepTwo(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "remove apparent sinks part two");
	    job.setJarByClass(RemoveSinksDriver.class);
	    job.setMapperClass(RemoveApparentSinksStepTwoMapper.class);
	    job.setReducerClass(RemoveApparentSinksStepTwoReducer.class);

	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(Text.class);
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
	RemoveSinksDriver cd = new RemoveSinksDriver();
	
	String crawlInput = args[0];
	String weCrawled = args[1];
	String didntCrawlStepOne = args[2];
	String output = args[3];
	
	// String weCrawled = "s3://pageranktest/weCrawled";
	// String didntCrawlStepOne = "s3://pageranktest/didntCrawlStepOne";
	
	
	cd.removeSinksWeCrawled(crawlInput, weCrawled);
	cd.removeApparentSinksStepOne(weCrawled, didntCrawlStepOne);
	cd.removeApparentSinksStepTwo(didntCrawlStepOne, output);
	
    }
}
