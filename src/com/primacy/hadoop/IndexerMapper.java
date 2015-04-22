package com.primacy.hadoop;

import indexer.Indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * InputFormat: key --> filename(docID.html/txt/xml/pdf)
 *              value --> filecontent(read in as BytesWritable)
 * 
 */
public class IndexerMapper extends Mapper<Text, BytesWritable, Text, Text> {
    protected void map(LongWritable key, BytesWritable value, Context context)
            throws IOException, InterruptedException {
        String fileName = key.toString();
        long docID = Long
                .parseLong(fileName.substring(0, fileName.indexOf('.')));
        String type = fileName.substring(fileName.indexOf('.') + 1);
        Indexer indexer = Indexer.getInstance(
                new ByteArrayInputStream(value.getBytes()), "www.google.com",
                docID, type.toLowerCase());
        indexer.parse();
        // indexer.displayResult();
    }
}
