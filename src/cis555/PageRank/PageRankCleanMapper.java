package cis555.PageRank;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class PageRankCleanMapper extends Mapper<LongWritable, Text, Text, Text> {

    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
	Text url = new Text();
	Text rank = new Text();
	String urlStr = null;
	String urlData = null;
	

	String line = value.toString();
	Pattern urlFromToPat = Pattern.compile("^([^\t]+)\t(.*)");
	Matcher urlMatcher = urlFromToPat.matcher(line);
	// the structure is docID_from TAB
	// page_rank;numlinks;docID_to1;docID_to2;docID_to3...docID_ton;
	
	if (urlMatcher.matches()) {
	    urlStr = urlMatcher.group(1);
	    urlData = urlMatcher.group(2);
	}

	Pattern urlDataPat = Pattern.compile("(\\d+\\.\\d+)(E-?\\d+)?;(\\d+);(.*)");
	Matcher dataMatcher = urlDataPat.matcher(urlData);
	if (dataMatcher.matches()) {
	    
	    String rankStr = dataMatcher.group(1);
	    if (dataMatcher.group(2) != null) {
		rankStr += dataMatcher.group(2);
	    }
	    url.set(urlStr);
	    rank.set(rankStr);
	    context.write(url, rank);
	}

    }
}