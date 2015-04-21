package cis555.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.aws.utils.DynamoAdapter;
import cis555.database.DBWrapper;
import cis555.database.Dao;
import cis555.database.PopulateDynamo;
import cis555.utils.CrawlerConstants;

public class Crawler {
	
	private static final Logger logger = Logger.getLogger(Crawler.class);
	private static final String CLASSNAME = Crawler.class.getName();
	
	
	/* Database related */
	private String dbEnvDir;
	private Dao dao;
	private int crawlerID;

	/* Crawler settings related */
	
	private List<URL> startingUrls;
	private List<String> allCrawlerIPs;
	private int maxDocSize;
	private String storageDirectory;
	private String urlStorageDirectory;
	
	private List<Thread> getThreadPool;
	private List<Thread> headThreadPool;
	private List<Thread> matcherThreadPool;

	private BlockingQueue<URL> newUrlQueue;
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> headCrawlQueue;
	private BlockingQueue<URL> getCrawlQueue;
	private Vector<URL> sitesCrawledThisSession; // A list of sites crawled in this session, to prevent repeated crawl
	
	/* Link Extractor related */
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private List<Thread> listExtractorThreadPool;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	
	private List<Thread> linkQueuerThreadPool;
	private DocIDGenerator counterGenerator;
	
	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		Crawler crawler = new Crawler();
//		crawler.testDynamo();
		crawler.setConfig();
		crawler.initialise();
//		while(GETWorker.active){
//			Thread.sleep(1000);
//		}
//		crawler.shutdown();
	}
	
	private void testDynamo(){
		DynamoAdapter adapter = new DynamoAdapter();
		CrawledDocument c1 = new CrawledDocument(1, "url1", "contenttype1");
		CrawledDocument c2 = new CrawledDocument(2, "url2", "contenttype2");
		List<CrawledDocument> documents = new ArrayList<CrawledDocument>();
		documents.add(c1);
		documents.add(c2);
		adapter.batchSaveCrawledDocuments(documents);
	}

	
	private void initialise() throws MalformedURLException{
		initialiseDb();
		this.newUrlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		for (URL url : this.startingUrls){
			this.newUrlQueue.add(url);
		}
		
		this.headCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		this.getCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.contentForLinkExtractor = new ArrayBlockingQueue<RawCrawledItem>(CrawlerConstants.QUEUE_CAPACITY);
		this.preRedistributionNewURLQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.siteInfoMap = new ConcurrentHashMap<String, SiteInfo>();
		this.sitesCrawledThisSession = new Vector<URL>();
		this.counterGenerator = new DocIDGenerator(this.crawlerID, this.dao);
		linkQueuerThreadPool();
		initialiseLinkExtractorThreadPool();
		initialiseHeadThreadPool();
		initialiseGetThreadPool();
		initialiseMatcherPool();
	}
	
	/**
	 * Initialise the database
	 */
	private void initialiseDb(){
		DBWrapper wrapper = new DBWrapper(this.dbEnvDir, false);
		this.dao = wrapper.getDao();
	}
	
	/**
	 * Initialise the linkQueuer with the IP addresses of all crawlers (including this one)
	 */
	private void linkQueuerThreadPool(){
		
		this.linkQueuerThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_HEAD_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_HEAD_GET_THREADS; i++){
			LinkQueuer linkQueuer = new LinkQueuer(this.preRedistributionNewURLQueue, 
					this.newUrlQueue, this.crawlerID, this.allCrawlerIPs);
			Thread linkQueuerThread = new Thread(linkQueuer);
			linkQueuerThread.start();
			this.linkQueuerThreadPool.add(linkQueuerThread);
			
		}
	}
	
	/**
	 * Initialise the matcherthreads
	 */
	private void initialiseMatcherPool(){
		this.matcherThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_MATCHER_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_MATCHER_THREADS; i++){
			RobotsMatcher matcher = new RobotsMatcher(siteInfoMap, newUrlQueue, headCrawlQueue);
			Thread robotsMatcherThread = new Thread(matcher);
			robotsMatcherThread.start();
			this.matcherThreadPool.add(robotsMatcherThread);
		}
		
	}
	
	/**
	 * Start up a pool of threads for HEAD workers
	 * 
	 * @return
	 */
	private void initialiseHeadThreadPool() {
		headThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_HEAD_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_HEAD_GET_THREADS; i++) {
			HEADWorker crawler = new HEADWorker(this.siteInfoMap, this.headCrawlQueue, 
					this.dao, this.getCrawlQueue, i, this.maxDocSize, this.newUrlQueue, 
					this.contentForLinkExtractor, this.sitesCrawledThisSession, this.storageDirectory);
			Thread workerThread = new Thread(crawler);
			workerThread.start();
			headThreadPool.add(workerThread);
		}
	}
	
	/**
	 * Start up a pool of threads for GET workers
	 * 
	 * @return
	 */
	private void initialiseGetThreadPool() {
		listExtractorThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_HEAD_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_HEAD_GET_THREADS; i++) {
			GETWorker crawler = new GETWorker(this.siteInfoMap, this.getCrawlQueue, this.newUrlQueue, i, this.contentForLinkExtractor);
			Thread workerThread = new Thread(crawler);
			workerThread.start();
			listExtractorThreadPool.add(workerThread);
		}
	}
	
	/**
	 * Start up a pool of threads for GET workers
	 * 
	 * @return
	 */
	private void initialiseLinkExtractorThreadPool() {
		getThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_EXTRACTOR_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_EXTRACTOR_THREADS; i++) {
			LinkExtractorWorker extractor = new LinkExtractorWorker(this.contentForLinkExtractor, 
					this.preRedistributionNewURLQueue, 
					i, this.dao, this.counterGenerator, this.storageDirectory, this.urlStorageDirectory);
			Thread thread = new Thread(extractor);
			thread.start();
			getThreadPool.add(thread);
		}
	}
	
	
	private void shutdown(){
		// shut down all threads
		try {
			
			for (Thread t : this.headThreadPool) {
				if (t.isAlive()) {
					t.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);
				}
			}

			for (Thread t : this.linkQueuerThreadPool) {
				if (t.isAlive()) {
					t.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);
				}				
			}
			
			for (Thread t : this.matcherThreadPool){
				if (t.isAlive()){
					t.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);					
				}
			}

			for (Thread t : this.listExtractorThreadPool){
				if (t.isAlive()){
					t.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);					
				}
			}

			
			for (Thread t : this.getThreadPool) {
				if (t.isAlive()) {
					t.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);
				}
			}

			logger.info(CLASSNAME + ": All threads shut down");
			
			DBWrapper.shutdown();
			logger.info(CLASSNAME + ": Database has shut down");
			System.exit(0);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/******
	 * CONFIGURATION RELATED METHODS
	 * *****
	 */
	
	/**
	 * Set the crawler configs from properties/settings.properties
	 * @param args
	 */
	private void setConfig(){

		Properties properties = new Properties();
		
		try {
//			properties.load(ClassLoader.class.getResourceAsStream(CrawlerConstants.PROPERTIES_FILE));
			properties.load(new FileInputStream(CrawlerConstants.PROPERTIES_FILE));

			this.crawlerID = validateIntString(properties.getProperty("crawler_id"));
			this.maxDocSize = validateIntString(properties.getProperty("max_doc_size"));
			this.dbEnvDir = CrawlerConstants.DB_DIRECTORY;
			
			String[] startingUrlArray = properties.getProperty("starting_urls").split(";");
			populateStartingUrls(startingUrlArray);
			
			String[] allCrawlersArray = properties.getProperty("other_crawlers").split(";");
			populateAllCrawlerDetails(allCrawlersArray);
			
			this.storageDirectory = CrawlerConstants.STORAGE_DIRECTORY;
			createDirectory(this.storageDirectory);
			this.urlStorageDirectory = CrawlerConstants.URL_STORAGE_DIRECTORY;
			createDirectory(this.urlStorageDirectory);
			
			
		} catch (ValidationException e){
			logger.error(e.getMessage());
			System.exit(1);
		} catch (Exception e){
			String file = CrawlerConstants.PROPERTIES_FILE;
			File f = new File(file);
			
			logger.error(CLASSNAME + " Unable to load properties file, exiting " + f.getAbsoluteFile());
			System.exit(1);
		}
		

	}
	
	/**
	 * Populates the starting urls from a url array
	 * @param startingUrlArray
	 */
	private void populateStartingUrls(String[] startingUrlArray){
		this.startingUrls = new ArrayList<URL>();
		for (String url : startingUrlArray){
			this.startingUrls.add(validateUrl(url));
		}
	}

	/**
	 * Converts a URL string to a URL
	 * @param urlString
	 */
	private URL validateUrl(String urlString){
		
		try {
			urlString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
			
			if (!urlString.startsWith("http")){
				urlString = "http://" + urlString;
			}
			return new URL(urlString);			
			
		} catch (MalformedURLException | UnsupportedEncodingException e){
			throw new ValidationException("URL syntax invalid: " + urlString);
		}
	}	
	
	/**
	 * Populates all the IP addresses of all other crawlers
	 * @param allCrawlersArray
	 */
	private void populateAllCrawlerDetails(String[] allCrawlersArray){
		this.allCrawlerIPs = new ArrayList<String>();
		for (String iPPortString : allCrawlersArray){
			this.allCrawlerIPs.add(validateIPPortString(iPPortString));
		}
		Collections.sort(this.allCrawlerIPs);
	}
	
	/**
	 * Validates an int string
	 * @param sizeString
	 * @return
	 */
	private int validateIntString(String intString){
		if (null != intString && !intString.isEmpty()){
			if (intString.matches("\\d+")){
				return Integer.parseInt(intString);				
			}
		}
		throw new ValidationException("Invalid or missing integer: " + intString);
	}
	
	/**
	 * Validates IP-port strings
	 * @param ipPortString
	 * @return
	 */
	public String validateIPPortString(String iPPortString){
		if (null != iPPortString && !iPPortString.isEmpty()){
			if (iPPortString.matches(CrawlerConstants.IP_PORT_FORMAT)){
				return iPPortString;				
			}
		}
		throw new ValidationException("Invalid port number: " + iPPortString);
	}
	
	
	/****
	 * Other helper methods
	 */
	
	/**
	 * Creates a directory to store the database and environment settings
	 */
	private void createDirectory(String path){
		File directory = new File(path);
		if (!directory.exists()){
			try {
				directory.mkdirs();
				logger.info(CLASSNAME + ": New directory created " + path);
			} catch (SecurityException e){
				throw new ValidationException("Unable to create directory");
			}
		}
		
	}
}
