package cis555.PageRank.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SortCombineMapper extends Mapper<LongWritable, Text, Text, Text> {

    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
	Text docID = new Text();
	String urlData = null;

	String line = value.toString();
	Pattern urlFromToPat = Pattern.compile("^([^\t]+)\t(.*)");
	Matcher urlMatcher = urlFromToPat.matcher(line);
	if (urlMatcher.matches()) {
	    docID.set(urlMatcher.group(1));
	    urlData = urlMatcher.group(2);
	    // System.out.println("urlData: " + urlData);
	    if (urlData.indexOf("\t") == -1) {
		Text rank = new Text(urlData);
		context.write(docID, rank);
	    } else {
		Pattern docIDURLPat = Pattern.compile("([^\t]+)\t.*");
		Matcher docIDURLMat = docIDURLPat.matcher(urlData);
		if (docIDURLMat.matches()) {
		    Text URL = new Text(docIDURLMat.group(1));
		    context.write(docID, URL);
		}
	    }
	    
	}
	else {
	    System.out.println("SortCombineMapper, urlData doesn't match: " + line);
	}

	


    }
}