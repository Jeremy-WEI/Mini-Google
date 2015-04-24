package cis555.utils;

import java.io.File;

import org.apache.log4j.Logger;


import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {

	private static final Logger logger = Logger.getLogger(DBWrapper.class);
	private static final String CLASSNAME = DBWrapper.class.getName();

	/* Environment fields */
	
	private static boolean isRunning = false;
	private static Environment myEnv;
	private static EntityStore store;

	/**
	 * Generates a database store
	 * @param directory
	 * @param readOnly
	 * @return
	 */
	public static EntityStore setupDatabase(String directory, boolean readOnly){
		if (null == directory || directory.isEmpty()){
			String error = "Directory name is empty or null";
			logger.error(CLASSNAME + ": " + error);
			throw new DBException(error);
		}
		
		if (!isRunning){
			
			logger.info(CLASSNAME + ": Opening database in " + directory);
			
			setupEnvironment(directory, readOnly);
			setupStore(readOnly);
			isRunning = true;	
		}
		
		return store;
	}
	
	
	/**
	 * Set up the database environment
	 */
	private static void setupEnvironment(String directory, boolean readOnly){
		EnvironmentConfig envConfig = new EnvironmentConfig();

		if (readOnly){
			envConfig = envConfig.setReadOnly(readOnly);
		} 
		envConfig.setConfigParam(EnvironmentConfig.LOCK_N_LOCK_TABLES, "5");
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		createDirectory(directory);			
		myEnv = new Environment(new File(directory), envConfig);
	}
	
	
	
	/**
	 * Creates a directory to store the database and environment settings
	 */
	private static void createDirectory(String envDirectory){
		File directory = new File(envDirectory);
		if (!directory.exists()){
			try {
				directory.mkdirs();
				logger.info(CLASSNAME + ": New directory created " + envDirectory);
			} catch (SecurityException e){
				String error = "Unable to create directory";
				logger.error(CLASSNAME + ": " + error);
				throw new DBException(error);
			}
		}
		
	}
	
	
	/**
	 * Set up the store, and initiate the DAO
	 */
	private static void setupStore(boolean readOnly){
		StoreConfig storeConfig = new StoreConfig();
		if (readOnly){
			storeConfig.setReadOnly(readOnly);			
		} else {
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);			
		}
		store = new EntityStore(myEnv, CrawlerConstants.DB_NAME, storeConfig);
	}
	
	/**
	 * Synchronises the database
	 */
    public static void sync() {
        if (store != null)
            store.sync();
        if (myEnv != null)
            myEnv.sync();
    }
	
	/**
	 * Shuts down the database
	 */
	public static void shutdown(){
		if (isRunning){
			store.close();
			myEnv.close();
			isRunning = false;
		}
	}
	
}
