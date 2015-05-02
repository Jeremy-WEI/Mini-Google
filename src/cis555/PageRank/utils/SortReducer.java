package cis555.PageRank.utils;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SortReducer extends Reducer<DoubleWritable, Text, DoubleWritable, Text> {
    protected void reduce(DoubleWritable key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

	Iterator<Text> iter = values.iterator();
	
	while (iter.hasNext()) {
	    double negRank = key.get();
	    context.write(new DoubleWritable(negRank*-1), iter.next());
	}
    }
}