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
		String storeDir = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.STORAGE_DIRECTORY;
		File storageDirectory = new File(storeDir);
		logger.info(CLASSNAME + " About to upload " + storageDirectory.list().length + " crawled documents to S3 "  + AWSConstants.DOCUMENT_BUCKET);

		adapter.uploadDirectory(storageDirectory, AWSConstants.DOCUMENT_BUCKET);
		
		logger.info(CLASSNAME + " Uploaded " + storageDirectory.list().length + " crawled documents to S3");
		
		// Upload links

		String urlStorageDir = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.URL_STORAGE_DIRECTORY;
		File urlStorageDirectory = new File(urlStorageDir);
		
		logger.info(CLASSNAME + " About to upload " + storageDirectory.list().length + " url file to S3 " + AWSConstants.URL_BUCKET);
		
		adapter.uploadDirectory(urlStorageDirectory, AWSConstants.URL_BUCKET);

		logger.info(CLASSNAME + " Uploaded URL file to S3");
		
		// Upload document meta
		
		String documentMetaDir = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.DOCUMENT_META_STORAGE_DIRECTORY;
		File documentMetaDirectory = new File(documentMetaDir);
		
		logger.info(CLASSNAME + " About to upload " + documentMetaDirectory.list().length + " document meta files to S3 bucket " + AWSConstants.DOCUMENT_META_BUCKET);
		
		adapter.uploadDirectory(documentMetaDirectory, AWSConstants.DOCUMENT_META_BUCKET);

		logger.info(CLASSNAME + " Uploaded document meta file to S3");

		
	}
	
	
}
