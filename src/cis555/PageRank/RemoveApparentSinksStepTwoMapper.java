package cis555.PageRank;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class RemoveApparentSinksStepTwoMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text target = new Text();
    private Text source = new Text();

    protected void map(LongWritable key, Text value, Context context) throws 
    		IOException, InterruptedException {

	String line = value.toString();
	Pattern urlFromTo = Pattern.compile("^([A-F0-9]{32})\t([A-F0-9]{32})$");
	Matcher urlMatcher = urlFromTo.matcher(line);
	if (urlMatcher.matches()) {
	    target.set(urlMatcher.group(1));
	    source.set(urlMatcher.group(2));
	    context.write(source, target);
	} else {
	    System.out.println("\n\n\nno good: " + line + "\n\n\n");
	}
    }
}