package cis555.aws.utils;

import java.io.File;

import org.apache.log4j.Logger;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;

public class S3Adapter {

	private static final Logger logger = Logger.getLogger(DynamoDao.class);
	private static final String CLASSNAME = DynamoDao.class.getName();

	private AmazonS3 client;
	private TransferManager manager;
	
	public S3Adapter(){
		connect();
	}
	
	private void connect(){
		this.client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        this.client.setRegion(usEast1);
        this.manager = new TransferManager(this.client);
	}
	
	public void uploadDirectory(File directoryName){
		if (directoryName.isDirectory()){
			this.manager.uploadDirectory(AWSConstants.DOCUMENT_BUCKET, "", directoryName, false);
		} else {
			logger.error(CLASSNAME + " : Unable to upload to s3 as " + directoryName + " is not a directory");
		}
	}
}
