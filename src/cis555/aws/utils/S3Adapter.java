package cis555.aws.utils;

import java.io.File;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;

/**
 * Allows upload and download of files from S3
 * c/f http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/transfer/TransferManager.html
 *
 */
public class S3Adapter {

	private static final Logger logger = Logger.getLogger(S3Adapter.class);
	private static final String CLASSNAME = S3Adapter.class.getName();

	private AmazonS3 client;
	private TransferManager manager;
	
	public S3Adapter(){
		connect();
	}
	
	private void connect(){
		this.client = AWSClientAdapters.getS3Client();
        this.manager = new TransferManager(this.client);
	}
	
	/**
	 * Upload a directory to S3
	 * @param directoryName
	 * @param bucketName
	 */
	public void uploadDirectory(File directoryName, String bucketName){
		if (directoryName.isDirectory()){
			this.manager.uploadDirectory(bucketName, "", directoryName, false);
		} else {
			logger.error(CLASSNAME + " : Unable to upload to s3 as " + directoryName + " is not a directory");
		}
	}
	
	// WORK IN PROGRESS
//	/**
//	 * Download a directory from S3
//	 * @param directoryName
//	 * @param bucketName
//	 */
//	public void downloadDirectory(File directoryName, String bucketName){
//		if (directoryName.exists() || directoryName.isDirectory()){
//			this.manager.download(bucketName, "", directoryName);
//		} else {
//			logger.error(CLASSNAME + " : Unable to download to s3 as " + directoryName + " is not a directory or does not exist");
//		}
//	}
	
	/**
	 * Download all the files from a bucket in S3
	 * @param bucketName
	 * @param OutDirectory
	 */
	public void downloadAllFilesInBucket(String bucketName, String OutDirectory){	
   	 	ObjectListing objectListing = client.listObjects(new ListObjectsRequest().withBucketName(bucketName));
     
   	 	File dir = new File(OutDirectory, bucketName);
	    if (dir.mkdirs()) {
	    	System.out.println("Created " + dir.getName() + " directory.");
	    }
	    
	    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
	    	if (objectSummary.getSize() < 1) continue;
	    	
	    	String k = objectSummary.getKey();
	    	File f = new File(k);
         
	    	manager.download(new GetObjectRequest(bucketName, k), new File(String.format("%s/%s", dir.getPath(), f.getName())));
	    }
	}
	
	
}
