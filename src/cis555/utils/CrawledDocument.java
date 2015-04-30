package cis555.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers only to documents that have been crawled
 */
@Entity
public class CrawledDocument {

	private CrawledDocument(){}
	
	@PrimaryKey
	private String docID;
	
	private String url;

	private String contentType;
	
	public CrawledDocument(String docID, String url, String contentType){
		this.docID = docID;
		this.url = url;
		this.contentType = contentType;
	}
		
	public String getDocID(){
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
