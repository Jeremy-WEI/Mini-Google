package cis555.database;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocumentMeta {

	private DocumentMeta(){}
	
	@PrimaryKey
	private String url;

	private long id;
	private Date lastCrawledDate;
	
	public DocumentMeta(String url, long id, Date lastCrawledDate){
		this.id = id;
		this.url = url;
		this.lastCrawledDate = lastCrawledDate;
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
		
	
}
