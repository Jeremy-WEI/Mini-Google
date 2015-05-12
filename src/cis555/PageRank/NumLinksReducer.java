package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class NumLinksReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    protected void reduce(IntWritable key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
	Iterator<Text> iter = values.iterator();
	while (iter.hasNext()) {
	    context.write(key, iter.next());
	}
    }
}