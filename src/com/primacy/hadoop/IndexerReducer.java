package com.primacy.hadoop;

import cis555.indexer.DocHit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

// TODO: need to write idf value to somewhere --> DynamoDB?
public class IndexerReducer extends Reducer<Text, Text, Text, Text> {
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        Map<Long, DocHit> map = new HashMap<Long, DocHit>();
        Iterator<Text> iter = values.iterator();
        while (iter.hasNext()) {
            DocHit docHit = new DocHit(iter.next().toString());
            if (map.containsKey(docHit.getDocID())) {
                map.get(docHit.getDocID()).merge(docHit);
            } else
                map.put(docHit.getDocID(), docHit);
        }
        for (Entry<Long, DocHit> entry : map.entrySet()) {
            context.write(key, new Text(entry.getValue().toString()));
        }
    }
}
