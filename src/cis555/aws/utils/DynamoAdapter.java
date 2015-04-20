package cis555.aws.utils;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DynamoAdapter {

	protected DynamoAdapter(){}
	
	public static AmazonDynamoDBClient getClient(){
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new InstanceProfileCredentialsProvider());
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		client.setRegion(usEast1);
		return client;
	}
}
