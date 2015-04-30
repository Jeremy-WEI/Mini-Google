package com.primacy.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class WholeFileRecordReader extends RecordReader<Text, BytesWritable> {

    private FileSplit split;
    private Configuration conf;
    private Text key;
    private String type;

    private final BytesWritable currValue = new BytesWritable();
    private boolean fileProcessed = false;

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        this.split = (FileSplit) split;
        String fileName = this.split.getPath().getName();
        // this.key = new Text(fileName);
        int startIndex = fileName.indexOf('.') + 1;
        type = fileName.substring(startIndex, fileName.lastIndexOf('.'));
        this.conf = context.getConfiguration();
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (fileProcessed) {
            return false;
        }

        int fileLength = (int) split.getLength();
        byte[] result = new byte[fileLength];

        FileSystem fs = FileSystem.get(split.getPath().toUri(), conf);
        FSDataInputStream in = null;
        try {
            in = fs.open(split.getPath());
            IOUtils.readFully(in, result, 0, fileLength);
            byte[] content = Utils.unzip(result);
            key = new Text(Utils.getURL(content) + "." + type);
            currValue.set(content, CrawlerConstants.MAX_URL_LENGTH * 2,
                    content.length - CrawlerConstants.MAX_URL_LENGTH * 2);

        } finally {
            IOUtils.closeStream(in);
        }
        this.fileProcessed = true;
        return true;
    }

    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    @Override
    public BytesWritable getCurrentValue() throws IOException,
            InterruptedException {
        return currValue;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return 0;
    }

    @Override
    public void close() throws IOException {
    }

}