package cis555.indexer.mapreduce;

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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

/*
 * InputFormat: key --> filename(docID.html/txt/xml/pdf)
 *              value --> filecontent(read in as BytesWritable)
 * 
 */
public class IndexerMapper extends
        Mapper<URLTypeWritable, BytesWritable, Text, Text> {

    protected void map(URLTypeWritable key, BytesWritable value, Context context)
            throws IOException, InterruptedException {

        // System.out.println("Mapper Job Starting...");
        // String fileName = key.toString();
        // int index = fileName.lastIndexOf('.');
        // String url = fileName.substring(0, index);
        // String type = fileName.substring(index + 1);
        String url = key.url;
        String type = key.type;
        // System.out.println("type" + type + "url: " + url);
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
        AmazonDynamoDBClient client = AWSClientAdapters.getDynamoClient();

        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("DocumentWordCount");
        Item item = new Item().withPrimaryKey("docID", docID).withNumber(
                "length", indexer.getLength());
        table.putItem(item);
    }
}
