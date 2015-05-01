/**
 * 
 */
package cis555.searchEngine;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;

/**
 * @author cis455
 *
 */
public class ShutdownHook extends Thread{
	/** The environment instance to be closed upon shutdown. */
	private Environment env;
	/** The entity store to be closed upon shutdown. */
	private EntityStore store;
	
	/**
	 * Called from within the DAO to pass the Environment and EntityStore to be closed.
	 * Note there can only be one of each of these in this application.
	 * @param env The Environment.
	 * @param store The EntityStore. 
	 */
	public ShutdownHook(Environment env, EntityStore store) {
		this.env = env;
		this.store = store;
	}

	/**
	 * Runs at system shutdown time and closes the environment and entity store. 
	 */
	public void run() {
		try {
			if (env != null) {
				store.close();
				env.cleanLog();
				env.close();
				
				System.out.println(String.format("%s closed.", store.getStoreName()));
			} 
		} catch (DatabaseException dbe) {
	      System.err.println("Failed to close the database.");
	    } 
	  }
}
