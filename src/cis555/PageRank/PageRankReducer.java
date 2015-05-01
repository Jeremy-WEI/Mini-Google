package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class PageRankReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
	
	Iterator<Text> iter = values.iterator();
	String links = "";
	
	boolean crawled = false;
	double newRank = 0;
	
	while (iter.hasNext()) {
	    String urlData = iter.next().toString();
	    if (urlData.startsWith("~")) {
		crawled = true;
		Pattern linksPat = Pattern.compile("~(\\d+\\.\\d+)(E-?\\d+)?;(.*)");
		Matcher linksMatcher = linksPat.matcher(urlData);
		if (linksMatcher.matches()) {
		    links = linksMatcher.group(3);
		}
		else {
		    System.out.println("\n\n\ndoes not match, this is value: " + urlData + "\n\n\n");
		}
	    }
	    else {
		System.out.println("\n\nvalue to sum into rank: " + urlData + "\n\n");
		newRank += Double.parseDouble(urlData);
	    }
	}
	if (crawled) {
	    context.write(key, new Text(newRank + ";" + links));
	}
    }
}