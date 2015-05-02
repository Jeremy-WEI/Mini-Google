package cis555.indexer.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class URLTypeWritable implements WritableComparable<URLTypeWritable> {

    public String url;
    public String type;

    public void readFields(DataInput in) throws IOException {
        this.url = in.readLine();
        this.type = Text.readString(in);
    }

    public void write(DataOutput out) throws IOException {
        Text.writeString(out, url + "\n");
        Text.writeString(out, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof URLTypeWritable)
            return this.compareTo((URLTypeWritable) obj) == 0;
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public int compareTo(URLTypeWritable that) {
        return url.compareTo(that.url);
    }

}
