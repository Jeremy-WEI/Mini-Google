package com.primacy.hadoop;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

public class CombineFileInputFormatImpl extends
        CombineFileInputFormat<URLTypeWritable, BytesWritable> {
    public CombineFileInputFormatImpl() {
        super();
        setMaxSplitSize(10 * 1024 * 1024); // 10 MB, default block size on
                                           // hadoop
    }

    public RecordReader<URLTypeWritable, BytesWritable> createRecordReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<URLTypeWritable, BytesWritable>(
                (CombineFileSplit) split, context,
                CombineFileRecordReaderImpl.class);
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }
}
