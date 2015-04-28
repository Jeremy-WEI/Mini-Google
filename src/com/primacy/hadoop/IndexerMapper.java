package com.primacy.hadoop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import cis555.indexer.DocHit;
import cis555.indexer.Indexer;
import cis555.utils.Utils;

/*
 * InputFormat: key --> filename(docID.html/txt/xml/pdf)
 *              value --> filecontent(read in as BytesWritable)
 * 
 */
public class IndexerMapper extends Mapper<Text, BytesWritable, Text, Text> {

    protected void map(Text key, BytesWritable value, Context context)
            throws IOException, InterruptedException {

        // System.out.println("Mapper Job Starting...");
        String fileName = key.toString();
        int index = fileName.lastIndexOf('.');
        String url = fileName.substring(0, index);
        String type = fileName.substring(index + 1);
        String docID = Utils.hashUrlToHexStringArray(url);
        Indexer indexer = Indexer.getInstance(
                new ByteArrayInputStream(value.getBytes()), url, docID,
                type.toLowerCase());

        if (indexer == null)
            return;
        indexer.parse();
        Text word = new Text();
        Text val = new Text();

        // indexer.displayResult();
        for (Entry<String, Map<String, DocHit>> entry : indexer.getMap()
                .entrySet()) {
            word.set(entry.getKey());
            for (DocHit docHit : entry.getValue().values()) {
                val.set(docHit.toString());
                context.write(word, val);
            }
        }
    }
}
