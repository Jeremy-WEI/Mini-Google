package cis555.aws.utils;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers to all URLs encountered, regardless of whether they've been crawled or not
 *
 */
@DynamoDBTable(tableName=AWSConstants.DOCUMENT_META_TABLE)
@Entity
public class DocumentMeta {

	private DocumentMeta(){}
	
	@DynamoDBHashKey(attributeName=AWSConstants.DOCUMENT_META_HASH_KEY)
	@PrimaryKey
	private String url;

	@DynamoDBAttribute(attributeName="docID")
	private long docID;
	
	@DynamoDBAttribute(attributeName="lastCrawledDate")
	private Date lastCrawledDate;

	@DynamoDBAttribute(attributeName="isCrawled")
	private boolean isCrawled;
	
	public DocumentMeta(String url, long docID, Date lastCrawledDate, boolean isCrawled){
		this.docID = docID;
		this.url = url;
		this.lastCrawledDate = lastCrawledDate;
		this.isCrawled = isCrawled;
	}
	
	/**
	 * Get the URL of the document
	 * @return
	 */
	public String getURL(){
		return this.url;
	}

	/**
	 * Get the id of this document
	 * @return
	 */
	public long getDocID(){
		return this.docID;
	}
	
	/**
	 * Return the last crawl date of the document
	 * @return
	 */
	public Date getLastCrawledDate(){
		return this.lastCrawledDate;
	}
		
	/**
	 * Indicates whether this document has been crawled or not
	 * @return
	 */
	public boolean isCrawled(){
		return this.isCrawled();
	}
	
}
