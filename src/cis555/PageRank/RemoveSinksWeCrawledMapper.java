package cis555.PageRank;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class RemoveSinksWeCrawledMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text url = new Text();
    private String linksStr = null;

    protected void map(LongWritable key, Text value, Context context) throws 
    			IOException, InterruptedException {

	String line = value.toString();
	Pattern urlFromTo = Pattern.compile("^([A-F0-9]{32})\t(.*)");
	Matcher urlMatcher = urlFromTo.matcher(line);
	if (urlMatcher.matches()) {
	    url.set(urlMatcher.group(1));
	    linksStr = urlMatcher.group(2);
	} else {
	    System.out.println("\n\n\nno good: " + line + "\n\n\n");
	}

	int numLinks = 0;
	if (!linksStr.equals("")) {
	    String[] linkCollection = linksStr.split(";");
	    numLinks = linkCollection.length;
	}

	if (numLinks != 0) {
	    context.write(url, new Text(linksStr));
	}
    }
}