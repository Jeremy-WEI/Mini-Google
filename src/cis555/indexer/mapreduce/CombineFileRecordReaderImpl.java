package cis555.indexer.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class CombineFileRecordReaderImpl extends
        RecordReader<URLTypeWritable, BytesWritable> {

    private int index;
    private CombineFileSplit split;
    private Configuration conf;
    private URLTypeWritable key;
    private BytesWritable value;
    private String fileName;
    private boolean fileProcessed = false;

    public CombineFileRecordReaderImpl(CombineFileSplit split,
            TaskAttemptContext context, Integer index) throws IOException {
        this.index = index;
        this.split = split;
        this.conf = context.getConfiguration();
        this.fileName = this.split.getPath(index).getName();
        System.out.println("FileName : " + fileName);
        this.key = new URLTypeWritable();
        this.value = new BytesWritable();
    }

    @Override
    public void initialize(InputSplit arg0, TaskAttemptContext arg1)
            throws IOException, InterruptedException {
        // Won't be called, use custom Constructor
        // `CFRecordReader(CombineFileSplit split, TaskAttemptContext context,
        // Integer index)`
        // instead
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public float getProgress() throws IOException {
        return 0;
    }

    @Override
    public URLTypeWritable getCurrentKey() throws IOException,
            InterruptedException {
        return key;
    }

    @Override
    public BytesWritable getCurrentValue() throws IOException,
            InterruptedException {
        return value;
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (fileProcessed) {
            return false;
        }

        int fileLength = (int) split.getLength(index);
        byte[] result = new byte[fileLength];

        FileSystem fs = FileSystem.get(split.getPath(index).toUri(), conf);
        FSDataInputStream in = null;
        try {
            in = fs.open(split.getPath(index));
            IOUtils.readFully(in, result, 0, fileLength);
            byte[] content = Utils.unzip(result);
            key.url = Utils.getURL(content);
            key.type = fileName.substring(fileName.indexOf('.') + 1,
                    fileName.lastIndexOf('.'));
            value.set(content, CrawlerConstants.MAX_URL_LENGTH * 2,
                    content.length - CrawlerConstants.MAX_URL_LENGTH * 2);

        } finally {
            IOUtils.closeStream(in);
        }
        this.fileProcessed = true;
        return true;
    }

}
