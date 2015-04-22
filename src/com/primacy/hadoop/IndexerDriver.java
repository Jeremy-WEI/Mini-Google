package com.primacy.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class IndexerDriver {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();

        // conf.set("key.value.separator.in.input.line", "\t");
        Job job = Job.getInstance(conf, "JobName");
        job.setJarByClass(IndexerDriver.class);
        job.setMapperClass(IndexerMapper.class);
        job.setReducerClass(IndexerReducer.class);

        job.setInputFormatClass(WholeFileInputFormat.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(WholeFileInputFormat.class);

        FileInputFormat.addInputPath(job, new Path("input"));
        FileOutputFormat.setOutputPath(job, new Path("output"));

        try {
            if (!job.waitForCompletion(true))
                System.out.println("Not Running...");
            else {
                System.out.println("Running...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
