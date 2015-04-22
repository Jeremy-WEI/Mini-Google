package cis555.database;


import java.io.File;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	
	private static final Logger logger = Logger.getLogger(DBWrapper.class);
	private static final String CLASSNAME = DBWrapper.class.getName();
	
	/* Environment fields */
	private static String envDirectory = null;
	private static Environment myEnv;
	private static EntityStore store;
	
	public static boolean isRunning = false;
	
	private Dao dao;
	
	public DBWrapper(String directory, boolean readOnly){
		
		if (null == directory || directory.isEmpty()){
			String error = "Directory name is empty or null";
			logger.error(CLASSNAME + ": " + error);
			throw new DBException(error);
		}
		
		if (!isRunning){
			
			envDirectory = directory;
			
			logger.info(CLASSNAME + ": Opening database in " + envDirectory);
			
			setupEnvironment(readOnly);
			setupStore(readOnly);
			isRunning = true;
			
		}

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
	
	
	/**
	 * Set up the database environment
	 */
	private void setupEnvironment(boolean readOnly){
		EnvironmentConfig envConfig = new EnvironmentConfig();

		if (readOnly){
			envConfig = envConfig.setReadOnly(readOnly);
		} 
		envConfig.setConfigParam(EnvironmentConfig.LOCK_N_LOCK_TABLES, "5");
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		createDirectory();			
		myEnv = new Environment(new File(envDirectory), envConfig);
	}
	
	
	
	/**
	 * Creates a directory to store the database and environment settings
	 */
	private void createDirectory(){
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
	private void setupStore(boolean readOnly){
		StoreConfig storeConfig = new StoreConfig();
		if (readOnly){
			storeConfig.setReadOnly(readOnly);			
		} else {
			storeConfig.setAllowCreate(true);
			storeConfig.setTransactional(true);			
		}
		store = new EntityStore(myEnv, CrawlerConstants.DB_NAME, storeConfig);
		dao = new Dao(store);
	}
		
	/**
	 * Returns the Database Access Object, which allows access to the entities of the database
	 * @return
	 */
	public Dao getDao(){
		return this.dao;
	}
	
}
