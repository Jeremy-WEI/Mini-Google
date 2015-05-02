package cis555.PageRank.utils;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

//import cis555.indexer.DocHit;

public class SortCombineReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

	String rank = null;
	String URL = null;
	
	Iterator<Text> iter = values.iterator();
	
	while (iter.hasNext()) {
	    String docIDData = iter.next().toString();
	    if (docIDData.startsWith("http")) {
		URL = docIDData;
	    }
	    else {
		rank = docIDData;
	    }
	}
	if (rank != null && URL != null) {
	    context.write(key, new Text(rank + "\t" + URL));
	}
//	if (URL != null) {
//	    context.write(key, new Text(URL));
//	}
	
	
    }
}