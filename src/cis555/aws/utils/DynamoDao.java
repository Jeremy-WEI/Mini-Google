package cis555.aws.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class DynamoDao {
	
	private static final Logger logger = Logger.getLogger(DynamoDao.class);
	private static final String CLASSNAME = DynamoDao.class.getName();
	
	private AmazonDynamoDBClient client;
	private DynamoDBMapper mapper;
	private DynamoDB dynamoDB;

	public DynamoDao(){
		connect();

	}
	
	private void connect(){
		this.client = AWSClientAdapters.getDynamoClient();
		mapper = AWSClientAdapters.getDynamoDBMapper();
		dynamoDB = AWSClientAdapters.getDynamoDB();
	}
	
	// For more on batch writing: http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaQueryScanORMBatchWriteExample.html
	
	/**
	 * Crawled Document methods
	 */
	
	/**
	 * Batch-save a list of crawled documents
	 * @param documents
	 */
	public void batchSaveCrawledDocuments(List<CrawledDocument> documents){
		this.mapper.batchSave(documents);
	}
	
	/**
	 * Return all crawled documents stored in DynamoDB
	 * For more info please refer to http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ScanJavaDocumentAPI.html
	 * @return
	 */
	public List<CrawledDocument> batchGetCrawledDocuments(){
		ScanResult result = null;
		List<CrawledDocument> crawledDocuments = new ArrayList<CrawledDocument>();
		do {
			ScanRequest scanRequest = new ScanRequest().withTableName(AWSConstants.CRAWLED_DOCUMENT_TABLE);
            if (result != null) {
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
            }
            result = this.client.scan(scanRequest);
    		for (Map<String, AttributeValue> item : result.getItems()){
    			long docID = Long.parseLong(item.get(AWSConstants.CRAWLED_DOCUMENT_DOCID_FIELD).getN());
    			String url = item.get(AWSConstants.CRAWLED_DOCUMENT_URL_FIELD).getS();
    			String contentType = item.get(AWSConstants.CRAWLED_DOCUMENT_CONTENT_TYPE_FIELD).getS();
    			CrawledDocument document = new CrawledDocument(docID, url, contentType);
    			crawledDocuments.add(document);
    		}
		} while (result.getLastEvaluatedKey() != null);
		
		return crawledDocuments;
	}
	
	/**
	 * Get the url associated with a docID. Returns null if item does not exist in the database, or if an exception is thrown
	 * If you are looking get a significant number of urls, consider using cis555.utils.UrlDocIDMapper.java
	 * @param docID
	 * @return
	 */
	public String getUrlFromDocID(long docID) {
		TableKeysAndAttributes crawledDocumentTable = new TableKeysAndAttributes(AWSConstants.CRAWLED_DOCUMENT_TABLE);
		crawledDocumentTable.addHashOnlyPrimaryKey(AWSConstants.CRAWLED_DOCUMENT_DOCID_FIELD, docID);
		Map<String, TableKeysAndAttributes> requestItems = new HashMap<String, TableKeysAndAttributes>();
		requestItems.put(AWSConstants.CRAWLED_DOCUMENT_TABLE, crawledDocumentTable);
		
		try {
			BatchGetItemOutcome outcome = this.dynamoDB.batchGetItem(crawledDocumentTable);
			List<Item> items = outcome.getTableItems().get(AWSConstants.CRAWLED_DOCUMENT_TABLE);

			if (items.size() == 0){
				logger.error(CLASSNAME + ": No items returned");
				return null;				
			}
			// We assume that there is only one item

			Item item = items.get(0);
			return item.getString(AWSConstants.CRAWLED_DOCUMENT_URL_FIELD);
			
		} catch (AmazonClientException e){
			logger.error(CLASSNAME + ": " + e.getMessage());
			return null;
		}		
	}
	
	
//	// WORK IN PROGRESS
//	public List<String> getUrlFromDocIDs(long...docIDs){
//		TableKeysAndAttributes crawledDocumentTable = new TableKeysAndAttributes(AWSConstants.CRAWLED_DOCUMENT_TABLE);
//		for (long docID : docIDs){
//			crawledDocumentTable.addHashOnlyPrimaryKey(AWSConstants.CRAWLED_DOCUMENT_DOCID_FIELD, docID);
//			logger.info("added " + docID);
//		}
//		Map<String, TableKeysAndAttributes> requestItems = new HashMap<String, TableKeysAndAttributes>();
//		requestItems.put(AWSConstants.CRAWLED_DOCUMENT_TABLE, crawledDocumentTable);
//		
//		List<String> urls = new ArrayList<String>();
//		
//		try {
//			BatchGetItemOutcome outcome = this.dynamoDB.batchGetItem(crawledDocumentTable);
//			logger.info(outcome.getBatchGetItemResult());
//			List<Item> items = outcome.getTableItems().get(AWSConstants.CRAWLED_DOCUMENT_TABLE);
//			
//			if (items.size() == 0){
//				logger.error(CLASSNAME + ": No items returned");
//				return null;				
//			}
//			logger.info(CLASSNAME + ": " + items.size());
//			for (Item item : items){
//				logger.info(CLASSNAME + ": " + item.getString(AWSConstants.CRAWLED_DOCUMENT_URL_FIELD));
//				urls.add(item.getString(AWSConstants.CRAWLED_DOCUMENT_URL_FIELD));
//			}
//			
//			return urls;
//			
//		} catch (AmazonClientException e){
//			logger.error(CLASSNAME + ": " + e.getMessage());
//			return null;
//		}	
//	}
	
	/**
	 * Add a single crawled document to the database
	 * @param docID
	 * @param url
	 * @param contentType
	 * @return
	 * @throws AmazonServiceException
	 * @throws AmazonClientException
	 */
	public PutItemResult addSingleCrawledDocument(long docID, String url, String contentType) throws AmazonServiceException, AmazonClientException {
        Map<String, AttributeValue> item = newItem(docID, url, contentType);
        PutItemRequest putItemRequest = new PutItemRequest(AWSConstants.CRAWLED_DOCUMENT_TABLE, item);
        return client.putItem(putItemRequest);
	}
	
	/**
	 * Generate a new item to put into a table
	 * @param docID
	 * @param url
	 * @param contentType
	 * @return
	 */
    private Map<String, AttributeValue> newItem(long docID, String url, String contentType) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("docID", new AttributeValue().withN(Long.toString(docID)));
        item.put("url", new AttributeValue(url));
        item.put("contentType", new AttributeValue(contentType));
        return item;
    }
    
    
	
	/**
	 * DocumentMeta methods
	 */
	
	/**
	 * Batch-save a list of documentMeta
	 * @param documentMeta
	 */
	public void batchSaveDocumentMeta(List<DocumentMeta> documentMeta){
		this.mapper.batchSave(documentMeta);
	}
	
	
	/**
	 * Return all document meta objects stored in DynamoDB
	 * For more info please refer to http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ScanJavaDocumentAPI.html
	 * @return
	 */
	public List<DocumentMeta> batchGetDocumentMeta(){
		
		ScanResult result = null;
		List<DocumentMeta> documentMetaObjects = new ArrayList<DocumentMeta>();
				
		do {
			ScanRequest scanRequest = new ScanRequest().withTableName(AWSConstants.DOCUMENT_META_TABLE);
			if (null != result){
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
			}
			result = this.client.scan(scanRequest);
			
			for (Map<String, AttributeValue> item : result.getItems()){
				
				String url = item.get(AWSConstants.DOCUMENT_META_URL_FIELD).getS();
				long docID = Long.parseLong(item.get(AWSConstants.DOCUMENT_META_DOCID_FIELD).getN());			
				String isCrawledString = item.get(AWSConstants.DOCUMENT_META_ISCRAWLED_FIELD).getN();
				boolean isCrawled = convert01toBoolean(isCrawledString);
				String dateString = item.get(AWSConstants.DOCUMENT_META_LAST_CRAWLED_DATE_FIELD).getS();
				Date lastCrawledDate = DatatypeConverter.parseDateTime(dateString).getTime();
				DocumentMeta metaObject = new DocumentMeta(url, docID, lastCrawledDate, isCrawled);
				documentMetaObjects.add(metaObject);
				
			}
		} while (result.getLastEvaluatedKey() != null);

		return documentMetaObjects;
	}
		
	/**
	 * Convert "0" or "1" to false or true	
	 * @param zeroOne
	 * @return
	 */
	private boolean convert01toBoolean(String zeroOne){
		if (zeroOne.equals("0")){
			return false;
		} else if (zeroOne.equals("1")){
			return true;
		}
		throw new RuntimeException("String returned neither 0 nor 1" + zeroOne);
	}
}
