package cis555.contentTransferer;

import java.util.List;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.aws.utils.DocumentMeta;
import cis555.aws.utils.DynamoDao;
import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;

import com.sleepycat.persist.EntityStore;

public class PopulateDynamo {

	private static final Logger logger = Logger.getLogger(PopulateDynamo.class);
	private static final String CLASSNAME = PopulateDynamo.class.getName();
	
	private CrawlerDao dao;
	
	public PopulateDynamo(){}
	
	/**
	 * Copies all CrawledDocument and DocumentMeta tables from the local database to Dynamo
	 */
	public void populateDynamo(){
		initialiseDb();
		copy();
	}

	/**
	 * Initialise the database
	 */
	private void initialiseDb(){
		EntityStore store = DBWrapper.setupDatabase(CrawlerConstants.DB_DIRECTORY, true);
		this.dao = new CrawlerDao(store);
	}

	/**
	 * Copy files over
	 */
	private void copy(){
		DynamoDao dynamoDao = new DynamoDao();
		copyCrawledDocuments(dynamoDao);
		copyDocumentMeta(dynamoDao);
	}
	
	private void copyCrawledDocuments(DynamoDao dynamoDao){
		List<CrawledDocument> crawledDocuments = this.dao.getAllCrawledDocuments();
		logger.info(CLASSNAME + ": About to load " + crawledDocuments.size() + " crawled document objects");
		dynamoDao.batchSaveCrawledDocuments(crawledDocuments);		
		logger.info(CLASSNAME + ": Loaded " + crawledDocuments.size() + " crawled document objects to Dynamo");
	}
	
	private void copyDocumentMeta(DynamoDao dynamoDao){
		List<DocumentMeta> documentMeta = this.dao.getAllDocumentMetaObjects();
		logger.info(CLASSNAME + ": About to load " + documentMeta.size() + " document meta objects");
		dynamoDao.batchSaveDocumentMeta(documentMeta);
		logger.info(CLASSNAME + ": Loaded " + documentMeta.size() + " document meta objects to Dynamo");
	}
}