package cis555.aws.utils;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.s3.AmazonS3Client;

public class AWSClientAdapters {

	private static AmazonDynamoDBClient dynamoClient;
	private static AmazonS3Client s3Client;
	
	/**
	 * Generates a client for DynamoDB (for table scans). This is used when connecting from an EC2 instance (ie not on your local machine)
	 * @return
	 */
	public static AmazonDynamoDBClient getDynamoClient(){
		dynamoClient = new AmazonDynamoDBClient(new InstanceProfileCredentialsProvider());
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		dynamoClient.setRegion(usEast1);
		return dynamoClient;
	}

	
	/**
	 * Generates a client for DynamoDB. This is used when connection from your local machine
	 * The ProfileCredentialsProvider will return your [default]
     * credential profile by reading from the credentials file located at
     * ($HOME/.aws/credentials).
	 * @return
	 */
	public static AmazonDynamoDBClient getDynamoClientFromLocalCredentials(){
		dynamoClient = new AmazonDynamoDBClient(new ProfileCredentialsProvider("default").getCredentials());
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		dynamoClient.setRegion(usEast1);
		return dynamoClient;

	}
	
	/**
	 * Generates a mapper for DynamoDB (for batch saving objects)
	 * @return
	 */
	public static DynamoDBMapper getDynamoDBMapper(){
		if (null == dynamoClient){
			dynamoClient = getDynamoClient();
		}
		return new DynamoDBMapper(dynamoClient);
	}
	
	/**
	 * Generates a DynamoDB object
	 * @return
	 */
	public static DynamoDB getDynamoDB(){
		if (null == dynamoClient){
			dynamoClient = getDynamoClient();
		}
		return new DynamoDB(dynamoClient);
	}
	
	/**
	 * Generate an Amazon S3 client
	 */
	public static AmazonS3Client getS3Client(){
		s3Client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        s3Client.setRegion(usEast1);
        return s3Client;
	}
}
