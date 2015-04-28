package cis555.aws.utils;

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
	
	public String getUrl(){
		return this.url;
	}
	
	public String docID;
	
	public String getDocID(){
		return this.docID;
	}
	
	private boolean isCrawled;
	
	public boolean isCrawled(){
		return this.isCrawled;
	}
	
	private Date date;
	
	public Date getLastCrawledDate(){
		return this.date;
	}

	public DocumentMeta(String url, String docID, Date date, boolean isCrawled) {
		this.url = url;
		this.docID = docID;
		this.isCrawled = isCrawled;
		this.date = date;
	}
	
}
