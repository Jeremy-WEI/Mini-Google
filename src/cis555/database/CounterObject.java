package cis555.database;

import cis555.utils.CrawlerConstants;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CounterObject {

	private CounterObject(){}
	
	@PrimaryKey
	private String key;
	
	private long docID;
	
	public CounterObject(long docID){
		this.key = CrawlerConstants.DB_COUNTER_KEY;
		this.docID = docID;
	}
	
	public long getDocID(){
		return this.docID;
	}
	
}
