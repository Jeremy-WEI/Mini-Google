package cis555.database;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers to all URLs encountered, regardless of whether they've been crawled or not
 *
 */
@Entity
public class DocumentMeta {

	private DocumentMeta(){}
	
	@PrimaryKey
	private String url;

	private long id;
	private Date lastCrawledDate;
	private boolean isCrawled;
	
	public DocumentMeta(String url, long id, Date lastCrawledDate, boolean isCrawled){
		this.id = id;
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
	public long getID(){
		return this.id;
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
