package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

//import cis555.indexer.DocHit;

public class PageRankInitReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
	Iterator<Text> iter = values.iterator();
	if (iter.hasNext()) {
	    context.write(key, iter.next());
	}
    }
}