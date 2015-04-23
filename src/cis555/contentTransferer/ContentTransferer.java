package cis555.contentTransferer;

import java.io.File;

import cis555.aws.utils.AWSConstants;
import cis555.aws.utils.S3Adapter;

/**
 * Transfers documents to S3 and to Dynamo
 *
 */
public class ContentTransferer {
	
	public static void main(String[] args){
		ContentTransferer transferer = new ContentTransferer();
		transferer.copyToDynamo();
		transferer.copyToS3();
	}
	
	private void copyToDynamo(){
		PopulateDynamo populateDynamo = new PopulateDynamo();
		populateDynamo.populateDynamo();
	}

	private void copyToS3(){
		PopulateS3 populateS3 = new PopulateS3();
		populateS3.populateS3();
	}
	
//	private void downloadFromS3(){
//		S3Adapter adapter = new S3Adapter();
//		File dir = new File("log");
//		adapter.downloadDirectory(dir, AWSConstants.DOCUMENT_BUCKET);
//	}
	
//	private void testDynamo(){
//		DynamoDao adapter = new DynamoDao();
//		DocumentMeta c1 = new DocumentMeta("hello1", 1, new Date(), true);
//		DocumentMeta c2 = new DocumentMeta("hello2", 2, new Date(), false);
//		DocumentMeta c3 = new DocumentMeta("hello3", 3, new Date(), true);
//		List<DocumentMeta> documents = new ArrayList<DocumentMeta>();
//		documents.add(c1);
//		documents.add(c2);
//		documents.add(c3);
//		
//		
//		try {
//
//			adapter.batchSaveDocumentMeta(documents);	
//			adapter.batchGetDocumentMeta();
//		} catch (Exception e){
//			e.printStackTrace();
//			System.exit(1);
//		}
////		List<String> urls = adapter.getUrlFromDocIDs(1, 2, 3);
////		for (String url : urls){
////			logger.info("URL: " + url);
////		}
////		adapter.batchGetCrawledDocuments();
//	}
//	
//	private void testS3(){
//		S3Adapter adapter = new S3Adapter();
//		File storageDirectory = new File(CrawlerConstants.STORAGE_DIRECTORY);
//		System.out.println(storageDirectory.getAbsolutePath());
//		adapter.uploadDirectory(storageDirectory);
//	}
}
