package cis555.indexer.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class IndexerDriver {
    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        System.out.println("Start Runing Indexing Job...");
        Configuration conf = new Configuration();
        System.out.println("Set Configuration done...");

        // conf.set("key.value.separator.in.input.line", "\t");
        Job job = Job.getInstance(conf, "Indexer Job");
        System.out.println("Get Job done...");
        job.setJarByClass(IndexerDriver.class);
        job.setMapperClass(IndexerMapper.class);
        job.setReducerClass(IndexerReducer.class);
        System.out.println("Set Job Class Done...");

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        System.out.println("Set Output Class Done...");

        // job.setInputFormatClass(WholeFileInputFormat.class);
        job.setInputFormatClass(CombineFileInputFormatImpl.class);
        System.out.println("Set Input Class Done...");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.out.println("Set Input and Output Directory Done...");
        // FileInputFormat.addInputPath(job, new Path("input"));
        // FileOutputFormat.setOutputPath(job, new Path("output"));

        job.waitForCompletion(true);
        // System.out.println("Finish Runing Indexing Job...");
    }
}
