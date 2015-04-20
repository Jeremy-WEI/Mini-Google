package cis555.database;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers only to documents that have been crawled
 */
@Entity
public class CrawledDocument {

	private CrawledDocument(){}
	
	@PrimaryKey
	private long id;
	
	private String url;
	private String contentType;
	
	public CrawledDocument(long id, String url, String contentType){
		this.id = id;
		this.url = url;
		this.contentType = contentType;
	}
	
	public long getID(){
		return this.id;
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
