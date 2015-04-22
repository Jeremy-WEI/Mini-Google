package cis555.contentTransferer;

import java.io.File;

import org.apache.log4j.Logger;

import cis555.aws.utils.AWSConstants;
import cis555.aws.utils.S3Adapter;
import cis555.utils.CrawlerConstants;

public class PopulateS3 {

	private static final Logger logger = Logger.getLogger(PopulateS3.class);
	private static final String CLASSNAME = PopulateS3.class.getName();
	
	public PopulateS3(){}
	
	/**
	 * Copies all crawled documents and URL links to the relevant S3 buckets
	 */
	public void populateS3(){
		S3Adapter adapter = new S3Adapter();
		
		// Upload crawled documents
		File storageDirectory = new File(CrawlerConstants.STORAGE_DIRECTORY);
		adapter.uploadDirectory(storageDirectory, AWSConstants.DOCUMENT_BUCKET);
		
		logger.info(CLASSNAME + " Uploaded " + storageDirectory.list().length + " crawled documents to S3");
		
		// Upload links

		File urlStorageDirectory = new File(CrawlerConstants.URL_STORAGE_DIRECTORY);
		adapter.uploadDirectory(urlStorageDirectory, AWSConstants.DOCUMENT_BUCKET);

		logger.info(CLASSNAME + " Uploaded URL file to S3");
		
	}
	
	
}
