package cis555.PageRank;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class PageRankInitMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text url = new Text();
    private String linksStr = null;
    private Text links = new Text();
    
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
	
	    String line = value.toString();
	    System.out.println(line);
	    Pattern urlFromTo = Pattern.compile("^([^\t]+)\t(.*)");
	    Matcher urlMatcher = urlFromTo.matcher(line);
	    if (urlMatcher.matches()) {
		System.out.println("urlMatcher.group(1): " + urlMatcher.group(1));
		// System.out.println("urlMatcher.group(2): " + urlMatcher.group(2));
		url.set(urlMatcher.group(1));
		linksStr = urlMatcher.group(2);
	    }
	    
	    int numLinks = 0;
	    if (!linksStr.equals("")) {
		String[] linkCollection = linksStr.split(";");
		numLinks = linkCollection.length;
	    }
	    
	    double initialPageRank = 1.0;
	    links.set(initialPageRank + ";" + numLinks + ";" + linksStr);
	    context.write(url, links);
    }
}