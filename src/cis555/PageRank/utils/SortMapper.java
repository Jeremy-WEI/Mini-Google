package cis555.PageRank.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SortMapper extends Mapper<LongWritable, Text, DoubleWritable, Text> {

    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
	String docID = null;
	String rawRank = null;
	String url = null;

	String line = value.toString();
	// docID	rank	URL
	Pattern urlFromToPat = Pattern.compile("^([^\t]+)\t([^\t]+)\t(.*)");
	Matcher urlMatcher = urlFromToPat.matcher(line);
	if (urlMatcher.matches()) {
	    docID = urlMatcher.group(1);
	    rawRank = urlMatcher.group(2);
	    url = urlMatcher.group(3);
	}

	Pattern urlDataPat = Pattern.compile("(\\d+\\.\\d+)(E-?\\d+)?");
	Matcher dataMatcher = urlDataPat.matcher(rawRank);
	if (dataMatcher.matches()) {
	    double pageRank = 0;
	    if (dataMatcher.group(2) == null) {
		System.out.println(rawRank + ": not scientific notation");
		pageRank = Double.parseDouble(dataMatcher.group(1));
	    }
	    else {
		System.out.println("very much scientific notation");
		pageRank = Double.valueOf(dataMatcher.group(1) + dataMatcher.group(2));
		System.out.println("\n\n\nValue of PG (sci): " + pageRank + "\n\n\n");
	    }
	    double negativePageRank = -1*pageRank;
	    context.write( new DoubleWritable(negativePageRank), new Text(docID + "\t" + url));
	}

    }
}