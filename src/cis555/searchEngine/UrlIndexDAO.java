package cis555.searchEngine;

import java.io.File;

import cis555.searchengine.DocHitEntity;
import cis555.utils.DocIdUrlInfo;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class UrlIndexDAO {
	private static EntityStore store;
	private static PrimaryIndex<String, DocIdUrlInfo> urlIndex;
	
	/** Initialize all the static variables
	 *	@param dbPath The file path of the db  
	 */
	public static void setup(String dbPath) {
		// Create the directory in which this store will live.
		System.out.println("Setting up UrlIndexDB.");
	    File dir = new File(dbPath, "UrlIndexDB");
	    if (dir.mkdirs()) {
	    	System.out.println("Created UrlIndexDB directory.");
	    }
	    
	    EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		
	    Environment env = new Environment(dir,  envConfig);
	    store = new EntityStore(env, "UrlIndexStore", storeConfig);
	    urlIndex = store.getPrimaryIndex(String.class, DocIdUrlInfo.class);
	    
	    ShutdownHook hook = new ShutdownHook(env, store);
	    Runtime.getRuntime().addShutdownHook(hook);
		
	}
	
	/**
	 * Store the given UrlIndex in the database.
	 * @param UrlIndex 
	 */
	public static void putUrlInfo(DocIdUrlInfo docIdUrlInfo) {
		urlIndex.put(docIdUrlInfo);
	}
	
	/**
	 * Store the given pair of docID and url in the database.
	 * @param docID
	 * @param url
	 */
	public static void putUrlInfo(String docID, String url) {
		 urlIndex.put(new DocIdUrlInfo(url, docID));
	}
	  
	/**
	 * Retrieve a UrlIndex from the database given its docID.
	 * @param docID The primary key for the UrlIndex.
	 * @return The UrlIndex instance. 
	 */
	public static DocIdUrlInfo getDocHitEntity(String docID) {
		return urlIndex.get(docID);	
	}
	
	/**
	 * Returns cursor that iterates through the UrlIndex.
	 * @return A UrlIndex cursor. 
	 */
	public static EntityCursor<String> getUrlInfoCursor() {
		CursorConfig cursorConfig = new CursorConfig();
		cursorConfig.setReadUncommitted(true);
		return urlIndex.keys(null, cursorConfig);
	}
	  
	/**
	 * Removes the UrlIndex instance with the docID.
	 * @param docID
	 */
	public static void deleteUrlInfo(String docID) {
		urlIndex.delete(docID);
	}
	
	/**
	 * Returns urlIndex.
	 * @param urlIndex
	 */
	public static PrimaryIndex<String, DocIdUrlInfo> getUrlInfoIndex() {
		return urlIndex;
	}
	
}
