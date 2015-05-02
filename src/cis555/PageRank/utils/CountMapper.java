package cis555.PageRank.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private Text url = new Text();
    
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
	
	    String line = value.toString();
	    System.out.println(line);
	    Pattern urlFromTo = Pattern.compile("^([^\t]+)\t(.*)");
	    Matcher urlMatcher = urlFromTo.matcher(line);
	    if (urlMatcher.matches()) {
		System.out.println("urlMatcher.group(1): " + urlMatcher.group(1));
		url.set(urlMatcher.group(1));
		context.write(url, new IntWritable(1));
	    }
    }
}