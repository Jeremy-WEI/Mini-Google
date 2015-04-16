package cis555.crawler;

import cis555.database.Dao;

public class DocIDGenerator {
	
	private long docID;
	private Dao dao;
	
	public DocIDGenerator(int crawlerNumber, Dao dao){
		this.dao = dao;
		long lastSavedDocID = this.dao.getLatestCounterValue();
		if (lastSavedDocID == -1){
			// This means no value has been previously stored
			this.docID = (long) crawlerNumber << 59;
		} else {
			this.docID = lastSavedDocID;
		}
	}
	
	/**
	 * Gets the current docID number, and stores the latest value in the database
	 * @return
	 */
	public synchronized long getDocIDAndIncrement(){
		long currentCount = this.docID;
		this.dao.writeCounterValue(currentCount);
		docID++;
		return currentCount;
	}

}
