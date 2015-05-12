package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class RemoveApparentSinksStepTwoReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	// key here is a source, value is targets
	Iterator<Text> iter = values.iterator();

	String targets = "";
	while (iter.hasNext()) {
	    String newTarget = iter.next().toString();
	    targets += newTarget + ";";
	}
	
	context.write(key, new Text(targets));
    }
}