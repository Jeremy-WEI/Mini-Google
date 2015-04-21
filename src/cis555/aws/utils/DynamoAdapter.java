package cis555.aws.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class DynamoAdapter {
	
	private static final Logger logger = Logger.getLogger(DynamoAdapter.class);
	private static final String CLASSNAME = DynamoAdapter.class.getName();
	
	private AmazonDynamoDBClient client;
	private DynamoDBMapper mapper;
	private DynamoDB dynamoDB;

	public DynamoAdapter(){
		connect();
	}
	
	private void connect(){
		this.client = new AmazonDynamoDBClient(new InstanceProfileCredentialsProvider());
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		this.client.setRegion(usEast1);
		mapper = new DynamoDBMapper(this.client);
		dynamoDB = new DynamoDB(this.client);
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
		ScanRequest scanRequest = new ScanRequest().withTableName(AWSConstants.CRAWLED_DOCUMENT_TABLE);
		ScanResult result = this.client.scan(scanRequest);
		List<CrawledDocument> crawledDocuments = new ArrayList<CrawledDocument>();
		
		for (Map<String, AttributeValue> item : result.getItems()){
			long docID = Long.parseLong(item.get(AWSConstants.CRAWLED_DOCUMENT_DOCID_FIELD).getN());
			String url = item.get(AWSConstants.CRAWLED_DOCUMENT_URL_FIELD).getS();
			String contentType = item.get(AWSConstants.CRAWLED_DOCUMENT_CONTENT_TYPE_FIELD).getS();
			CrawledDocument document = new CrawledDocument(docID, url, contentType);
			crawledDocuments.add(document);
		}
		return crawledDocuments;
	}
	
	/**
	 * Get the url associated with a docID. Returns null if item does not exist in the database, or if an exception is thrown
	 * @param docID
	 * @return
	 */
	public String getURLFromDocID(long docID) {
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
}
