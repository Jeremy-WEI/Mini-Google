package cis555.database;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CrawledDocument {

	private CrawledDocument(){}
	
	@PrimaryKey
	private long id;
	
	private String url;
	private String contents;
	private Date lastCrawledDate;
	private String contentType;
	
	public CrawledDocument(long id, String url, String contents, Date lastCrawledDate, String contentType){
		this.id = id;
		this.url = url;
		this.contents = contents;
		this.lastCrawledDate = lastCrawledDate;
		this.contentType = contentType;
	}
	
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
	 * Extract the contents of the crawled document
	 * @return
	 */
	public String getContents(){
		return this.contents;
	}
	
	/**
	 * Update the contents of the cralwed document
	 * @param contents
	 * @param lastCrawledDate
	 */
	public void setContents(String contents, Date lastCrawledDate){
		this.contents = contents;
		this.lastCrawledDate = lastCrawledDate;
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
