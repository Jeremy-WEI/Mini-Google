package cis555.PageRank;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class RemoveApparentSinksStepOneReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	// key here is a target, value is all sources pointing to the target
	Iterator<Text> iter = values.iterator();

	boolean notSink = false;
	String sources = "";

	while (iter.hasNext()) {
	    String sourceOrTargets = iter.next().toString();
	    if (sourceOrTargets.startsWith("~")) {
		notSink = true;
	    } else {
		System.out.println("\n\nvalue to sum into rank: " + sourceOrTargets + "\n\n");
		sources += sourceOrTargets + ";";
	    }
	}
	if (notSink) {
	    String[] sourcesArr = sources.split(";");
	    for (String source : sourcesArr) {
		context.write(key, new Text(source));
	    }
	}
    }
}