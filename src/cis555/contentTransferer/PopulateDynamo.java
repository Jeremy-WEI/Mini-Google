package cis555.contentTransferer;

import java.util.List;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.aws.utils.DocumentMeta;
import cis555.aws.utils.DynamoDao;
import cis555.database.DBWrapper;
import cis555.database.Dao;
import cis555.utils.CrawlerConstants;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;

public class PopulateDynamo {

	private static final Logger logger = Logger.getLogger(PopulateDynamo.class);
	private static final String CLASSNAME = PopulateDynamo.class.getName();
	
	private Dao dao;
	
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
		DBWrapper wrapper = new DBWrapper(CrawlerConstants.DB_DIRECTORY, true);
		this.dao = wrapper.getDao();
	}

	/**
	 * Copy files over
	 */
	private void copy(){
		List<CrawledDocument> crawledDocuments = this.dao.getAllCrawledDocuments();
		List<DocumentMeta> documentMeta = this.dao.getAllDocumentMetaObjects();
		DynamoDao dao = new DynamoDao();
		dao.batchSaveCrawledDocuments(crawledDocuments);
		dao.batchSaveDocumentMeta(documentMeta);
		logger.info(CLASSNAME + ": Added " + crawledDocuments.size() + " crawled document objects and " + documentMeta.size() + " document meta objects to DynamoDB");					
	}
}