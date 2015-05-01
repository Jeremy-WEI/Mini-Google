package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

//import cis555.indexer.DocHit;

public class PageRankReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
	
//	Iterator<Text> iter = values.iterator();
//	while (iter.hasNext()) {
//	    String myValue = iter.next().toString();
//	    context.write(key, new Text(myValue));
//	}
	

	Iterator<Text> iter = values.iterator();
//	System.out.println("reduce seeing url:" + key + "end");
//	String concat = "";
	String links = "";
	
	boolean crawled = false;
	double newRank = 0;
	
	while (iter.hasNext()) {
	    String urlData = iter.next().toString();
	    if (urlData.startsWith("~")) {
		crawled = true;
		// Pattern linksPat = Pattern.compile("~(\\d+)(\\.\\d+)?;(.*)"); // first digits are old PageRank
		Pattern linksPat = Pattern.compile("~(\\d+\\.\\d+)(E-?\\d+)?;(.*)");
		Matcher linksMatcher = linksPat.matcher(urlData);
		if (linksMatcher.matches()) {
		    links = linksMatcher.group(3);
		    // String newRankLinks = urlData2 + ";" +
		    // linksMatcher.group(2);
		    // Text rankLinksText = new Text();
		    // rankLinksText.set(newRankLinks);
		    // context.write(key, rankLinksText);
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
	
//	if (iter.hasNext()) {
//	    String urlData1 = iter.next().toString();
//	    System.out.println("reduce key: " + key + " and first value: " + urlData1);
//	    if (iter.hasNext()) {
//		String urlData2 = iter.next().toString();
//		System.out.println("reduce key: " + key + " and second value: " + urlData2);
//		// pagerank - switch them around
//		if (urlData1.indexOf(";") == -1) {
//		    String temp = urlData1;
//		    urlData1 = urlData2;
//		    urlData2 = temp;
//		}
//		Pattern linksPat = Pattern.compile("(\\d+);(.*)");
//		Matcher linksMatcher = linksPat.matcher(urlData1);
//		if (linksMatcher.matches()) {
//		    String newRankLinks = urlData2 + ";" + linksMatcher.group(2);
//		    Text rankLinksText = new Text();
//		    rankLinksText.set(newRankLinks);
//		    context.write(key, rankLinksText);
//		}
//	    } else {
//		// only have a rank, not links which means it hasn't been
//		// crawled
//	    }
//	}
	
	
    }
}