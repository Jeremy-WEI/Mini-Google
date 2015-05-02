package cis555.contentTransferer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;
import cis555.utils.DocumentMeta;
import cis555.utils.FromToUrls;
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
		transferer.extractDataFromdDatabase();
		transferer.copyToS3();
	}

	private void copyToS3(){
		PopulateS3 populateS3 = new PopulateS3();
		populateS3.populateS3();
	}
	
	private void extractDataFromdDatabase(){
		CrawlerDao dao = initialiseDb();
		logger.info(CLASSNAME + " extracting document meta");
		saveDocumentMetaToDisk(dao);
		logger.info(CLASSNAME + " extracting urls");
		saveFromToURLsToDisk(dao);
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
		String directoryName = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.DOCUMENT_META_STORAGE_DIRECTORY;
		Utils.createDirectory(directoryName);
		File urlStorageFile = new File(directoryName + "/" + new Date() + "_" + fileName);
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
	
	
	/**
	 * Extract all from to url objects from the database and save to flat file
	 * @param dao
	 */
	private void saveFromToURLsToDisk(CrawlerDao dao){
		List<FromToUrls> fromToUrls = dao.getAllFromToDocuments();
		logger.info(CLASSNAME + " expecting " + fromToUrls.size() + " entries");
		String fileName = CrawlerConstants.URL_STORAGE_FILENAME;
		String directoryName = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.URL_STORAGE_DIRECTORY;
		Utils.createDirectory(directoryName);
		File urlStorageFile = new File(directoryName + "/" + new Date() + "_" + fileName);
		BufferedWriter writer = null;
		try {
			if (!urlStorageFile.exists()){
				urlStorageFile.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(urlStorageFile.getAbsoluteFile(), true));
			if (fromToUrls.size() > 0){
				for (FromToUrls urls : fromToUrls){
					writer.write(urls.getFromUrl() + "\t");
					List<String> toUrls = urls.getToURLs();
					for (String toUrl : toUrls){
						writer.write(toUrl + ";");
					}
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
}
