package cis555.database;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.aws.utils.DocumentMeta;
import cis555.aws.utils.DynamoDao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;

public class PopulateDynamo extends TimerTask {

	private static final Logger logger = Logger.getLogger(PopulateDynamo.class);
	private static final String CLASSNAME = PopulateDynamo.class.getName();
	
	private Dao dao;
    
	
	public PopulateDynamo(Dao dao){
		this.dao = dao;
	}

	@Override
	public void run() {
		try {
			List<CrawledDocument> crawledDocuments = this.dao.getAllCrawledDocuments();
			List<DocumentMeta> documentMeta = this.dao.getAllDocumentMetaObjects();
			DynamoDao dao = new DynamoDao();
			dao.batchSaveCrawledDocuments(crawledDocuments);
			dao.batchSaveDocumentMeta(documentMeta);
			logger.info(CLASSNAME + ": Added " + crawledDocuments.size() + " crawled document objects and " + documentMeta.size() + " document meta objects to DynamoDB");
			
		} catch (DynamoDBMappingException e){
			logger.error(e.getMessage());
			System.exit(1);
		}
		
	}
}
