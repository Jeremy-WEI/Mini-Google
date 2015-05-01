package cis555.PageRank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

//import cis555.indexer.DocHit;
//import cis555.indexer.Indexer;
//import cis555.utils.Utils;

public class PageRankCleanMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text url = new Text();
    private Text urlData = new Text();
    
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
	// page_rank;numlinks;docID_to1;docID_to2;docID_to3...docID_ton;
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