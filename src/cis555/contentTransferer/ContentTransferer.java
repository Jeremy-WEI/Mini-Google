package cis555.contentTransferer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import cis555.aws.utils.DocumentMeta;
import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;
import cis555.utils.Utils;

import com.sleepycat.persist.EntityStore;


/**
 * Transfers documents to S3 and to Dynamo
 *
 */
public class ContentTransferer {
	
	private static final Logger logger = Logger.getLogger(ContentTransferer.class);
	private static final String CLASSNAME = ContentTransferer.class.getName();

	
	public static void main(String[] args){
		ContentTransferer transferer = new ContentTransferer();
		transferer.extractDocumentMeta();
		transferer.copyToS3();
	}

	private void copyToS3(){
		PopulateS3 populateS3 = new PopulateS3();
		populateS3.populateS3();
	}
	
	private void extractDocumentMeta(){
		CrawlerDao dao = initialiseDb();
		saveDocumentMetaToDisk(dao);
	}
	
	/**
	 * Initialise the database
	 */
	private CrawlerDao initialiseDb(){
		EntityStore store = DBWrapper.setupDatabase(CrawlerConstants.DB_DIRECTORY, true);
		return new CrawlerDao(store);
	}
	
	/**
	 * Extract all document meta from the database and save to flat file
	 * @param dao
	 */
	private void saveDocumentMetaToDisk(CrawlerDao dao){
		List<DocumentMeta> documentMeta = dao.getAllDocumentMetaObjects();
		String fileName = CrawlerConstants.DOCUMENT_META_STORAGE_FILENAME;
		Utils.createDirectory(CrawlerConstants.DOCUMENT_META_STORAGE_DIRECTORY);
		File urlStorageFile = new File(CrawlerConstants.DOCUMENT_META_STORAGE_DIRECTORY + "/" + fileName);
		BufferedWriter writer = null;
		try {
			if (!urlStorageFile.exists()){
				urlStorageFile.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(urlStorageFile.getAbsoluteFile(), true));
			if (documentMeta.size() > 0){
				for (DocumentMeta document : documentMeta){
					writer.write(document.getDocID() + '\t' + document.getUrl() + '\t' + document.isCrawled() + "\t" + document.getLastCrawledDate());
					writer.write("\n");
				}
			}
			
		} catch (IOException e){
			logger.error(CLASSNAME + ": Unable to store file " + fileName +  ", skipping");
		} finally {
			if (null != writer){
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

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
