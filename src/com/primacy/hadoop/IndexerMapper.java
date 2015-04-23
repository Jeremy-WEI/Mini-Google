package com.primacy.hadoop;

import indexer.DocHit;
import indexer.Indexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * InputFormat: key --> filename(docID.html/txt/xml/pdf)
 *              value --> filecontent(read in as BytesWritable)
 * 
 */
public class IndexerMapper extends Mapper<Text, BytesWritable, Text, Text> {

    private static final int BUFFER = 8096;

    public static InputStream unzipInput(BytesWritable input)
            throws IOException {
        GZIPInputStream gs = new GZIPInputStream(new ByteArrayInputStream(
                input.getBytes()));
        byte[] buffer = new byte[BUFFER];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = gs.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        gs.close();
        System.out.println(out.toByteArray().length);
        return new ByteArrayInputStream(out.toByteArray());
    }

    protected void map(Text key, BytesWritable value, Context context)
            throws IOException, InterruptedException {
        String fileName = key.toString();
        long docID = Long
                .parseLong(fileName.substring(0, fileName.indexOf('.')));

        // String type = fileName.substring(fileName.indexOf('.') + 1);
        String type = fileName.substring(fileName.indexOf('.') + 1,
                fileName.indexOf('.', fileName.indexOf('.') + 1));

        // Indexer indexer = Indexer.getInstance(
        // new ByteArrayInputStream(value.getBytes()), "www.google.com",
        // docID, type.toLowerCase());
        Indexer indexer = Indexer.getInstance(unzipInput(value),
                "www.google.com", docID, type.toLowerCase());

        indexer.parse();

        // indexer.displayResult();
        for (Entry<String, Map<Long, DocHit>> entry : indexer.getMap()
                .entrySet()) {
            Text word = new Text(entry.getKey());
            for (DocHit docHit : entry.getValue().values()) {
                context.write(word, new Text(docHit.toString()));
            }
        }
    }
}
