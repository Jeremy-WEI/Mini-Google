package cis555.database;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CounterObject {

	private CounterObject(){}
	
	@PrimaryKey
	private String key;
	
	private long docID;
	
	public CounterObject(long docID){
		this.key = DBConstants.DB_COUNTER_KEY;
		this.docID = docID;
	}
	
	public long getDocID(){
		return this.docID;
	}
	
}
