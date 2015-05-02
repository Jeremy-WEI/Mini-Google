package cis555.PageRank.utils;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// this merges the ranks of docIDs with the docID-URL mapping and then sorts by rank
public class SortDriver {
    
    private void sort(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "sort by rank");
	    job.setJarByClass(SortDriver.class);
	    job.setMapperClass(SortMapper.class);
	    job.setReducerClass(SortReducer.class);

	    job.setMapOutputKeyClass(DoubleWritable.class);
	    job.setMapOutputValueClass(Text.class); // changed
	    job.setOutputKeyClass(DoubleWritable.class);
	    job.setOutputValueClass(Text.class);

	    FileInputFormat.addInputPath(job, new Path(inputPath));
	    FileOutputFormat.setOutputPath(job, new Path(outputPath));
	    job.waitForCompletion(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void merge(String inputPath, String outputPath) {
	Configuration conf = new Configuration();

	try {
	    Job job = Job.getInstance(conf, "merge docID-rank and docID-URL");
	    job.setJarByClass(SortDriver.class);
	    job.setMapperClass(SortCombineMapper.class);
	    job.setReducerClass(SortCombineReducer.class);

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
	SortDriver sd = new SortDriver();
	
	String inputPath = args[0];
	String mergedPath = args[1];
	String outputPath = args[2];
	
	sd.merge(inputPath, mergedPath);
	sd.sort(mergedPath, outputPath);
	
    }
}
