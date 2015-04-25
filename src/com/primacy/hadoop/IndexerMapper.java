package com.primacy.hadoop;

import indexer.DBWrapper;
import indexer.DocHit;
import indexer.Indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * InputFormat: key --> filename(docID.html/txt/xml/pdf)
 *              value --> filecontent(read in as BytesWritable)
 * 
 */
public class IndexerMapper extends Mapper<Text, BytesWritable, Text, Text> {

    protected void map(Text key, BytesWritable value, Context context)
            throws IOException, InterruptedException {
        String fileName = key.toString();
        long docID = Long
                .parseLong(fileName.substring(0, fileName.indexOf('.')));

        String type = fileName.substring(fileName.indexOf('.') + 1,
                fileName.indexOf('.', fileName.indexOf('.') + 1));

        DBWrapper db = new DBWrapper("test");
        db.start();
        String url = db.getUrl(docID);
        // System.out.println("DOCID: " + docID + " URL: " + url);
        // Indexer indexer = Indexer.getInstance(
        // new ByteArrayInputStream(value.getBytes()), "www.google.com",
        // docID, type.toLowerCase());
        Indexer indexer = Indexer.getInstance(
                new ByteArrayInputStream(value.getBytes()), url, docID,
                type.toLowerCase(), db);

        indexer.parse();
        Text word = new Text();
        Text val = new Text();

        // indexer.displayResult();
        for (Entry<String, Map<Long, DocHit>> entry : indexer.getMap()
                .entrySet()) {
            word.set(entry.getKey());
            for (DocHit docHit : entry.getValue().values()) {
                val.set(docHit.toString());
                context.write(word, val);
            }
        }
    }
}
