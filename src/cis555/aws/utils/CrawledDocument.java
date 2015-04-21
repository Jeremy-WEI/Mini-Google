package cis555.aws.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers only to documents that have been crawled
 */
@DynamoDBTable(tableName=AWSConstants.CRAWLED_DOCUMENT_TABLE)
@Entity
public class CrawledDocument {

	private CrawledDocument(){}
	
	@DynamoDBHashKey(attributeName=AWSConstants.CRAWLED_DOCUMENT_HASH_KEY)
	@PrimaryKey
	private long docID;
	
	@DynamoDBAttribute(attributeName="url")
	private String url;

	@DynamoDBAttribute(attributeName="contentType")
	private String contentType;
	
	public CrawledDocument(long id, String url, String contentType){
		this.docID = id;
		this.url = url;
		this.contentType = contentType;
	}
		
	public long getDocID(){
		return this.docID;
	}
	
	/**
	 * Return the content type of the document
	 * @return
	 */
	public String getContentType(){
		return this.contentType;
	}
	
	/**
	 * Get the URL of the document
	 * @return
	 */
	public String getURL(){
		return this.url;
	}
	
}
