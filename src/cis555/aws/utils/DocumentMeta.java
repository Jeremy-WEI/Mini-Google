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
	
	@DynamoDBHashKey(attributeName=AWSConstants.DOCUMENT_META_URL_FIELD)
	@PrimaryKey
	private String url;
	
	public String getUrl(){
		return this.url;
	}
	
	@DynamoDBAttribute(attributeName=AWSConstants.DOCUMENT_META_DOCID_FIELD)
	public long docID;
	
	public long getDocID(){
		return this.docID;
	}
	
	@DynamoDBAttribute(attributeName=AWSConstants.DOCUMENT_META_ISCRAWLED_FIELD)
	private boolean isCrawled;
	
	public boolean isCrawled(){
		return this.isCrawled;
	}
	
	@DynamoDBAttribute(attributeName=AWSConstants.DOCUMENT_META_LAST_CRAWLED_DATE_FIELD)
	private Date date;
	
	public Date getLastCrawledDate(){
		return this.date;
	}

	public DocumentMeta(String url, long docID, Date date, boolean isCrawled) {
		this.url = url;
		this.docID = docID;
		this.isCrawled = isCrawled;
		this.date = date;
	}
	
}
