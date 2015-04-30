package com.primacy.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import cis555.indexer.DocHit;

// TODO: need to write idf value to somewhere --> DynamoDB?
public class IndexerReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        Map<String, DocHit> map = new HashMap<String, DocHit>();
        // TestObject object = new TestObject();
        Iterator<Text> iter = values.iterator();
        while (iter.hasNext()) {
            // Text text = iter.next();
            DocHit docHit = new DocHit(iter.next().toString());
            if (map.containsKey(docHit.getDocID())) {
                map.get(docHit.getDocID()).merge(docHit);
            } else
                map.put(docHit.getDocID(), docHit);
        }
        Text val = new Text();
        for (Entry<String, DocHit> entry : map.entrySet()) {
            val.set(entry.getValue().toString());
            context.write(key, val);
        }
    }
}
