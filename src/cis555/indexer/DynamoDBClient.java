package cis555.indexer;


import java.util.Map;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class DynamoDBClient {

    private static AmazonDynamoDBClient dynamoDB;
    private static String ACCESS_TOKEN = "AKIAI5LTOBMLPFVT4IUQ";
    private static String SECRET_TOKEN = "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";
    private static String tableName = "CrawledDocument";

    public static void main(String... args) {
        dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(
                ACCESS_TOKEN, SECRET_TOKEN));
        dynamoDB.setRegion(Region.getRegion(Regions.US_EAST_1));
        ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
        ScanResult result = dynamoDB.scan(scanRequest);
        for (Map<String, AttributeValue> item : result.getItems()) {
            System.out.println(item.get(""));
        }
    }
}
