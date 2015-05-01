package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PageRankCleanReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

	Iterator<Text> iter = values.iterator();
	
	while (iter.hasNext()) {
	    context.write(key, iter.next());
	}
	
	
	
    }
}