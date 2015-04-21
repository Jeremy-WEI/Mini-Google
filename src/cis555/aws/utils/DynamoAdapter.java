package cis555.aws.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

public class DynamoAdapter {
	
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
	
	public void getAllCrawledDocuments(){
		TableKeysAndAttributes crawledDocumentTable = new TableKeysAndAttributes(AWSConstants.CRAWLED_DOCUMENT_TABLE);
		crawledDocumentTable.addHashOnlyPrimaryKeys(AWSConstants.CRAWLED_DOCUMENT_HASH_KEY);
		Map<String, TableKeysAndAttributes> requestItems = new HashMap<String, TableKeysAndAttributes>();
		requestItems.put(AWSConstants.CRAWLED_DOCUMENT_TABLE, crawledDocumentTable);
		BatchGetItemOutcome outcome = this.dynamoDB.batchGetItem(crawledDocumentTable);

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
