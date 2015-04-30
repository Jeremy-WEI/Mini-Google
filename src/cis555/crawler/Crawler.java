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
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;
import cis555.utils.Utils;

import com.sleepycat.persist.EntityStore;

public class Crawler {
	
	private static final Logger logger = Logger.getLogger(Crawler.class);
	private static final String CLASSNAME = Crawler.class.getName();
	
	
	/* Database related */
	private String dbEnvDir;
	private CrawlerDao dao;
	private int crawlerNumber;

	/* Crawler settings related */
	
	private Map<Integer, String> otherWorkerIPs;
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
	private List<String> excludedPatterns;
	
	/* Link Extractor related */
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private List<Thread> listExtractorThreadPool;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	
	private List<Thread> linkQueuerThreadPool;
	

	public Crawler(BlockingQueue<URL> newUrlQueue, int crawlerNumber, Map<Integer, String> otherWorkerIPs){
		this.newUrlQueue = newUrlQueue;
		this.crawlerNumber = crawlerNumber;
		this.otherWorkerIPs = otherWorkerIPs;
	}
	

	/**
	 * Start the crawler
	 */
	public void startCrawler(){
		logger.info(CLASSNAME + ": Starting crawler");
		setConfig();
		initialise();
	}
	
	/**
	 * Stop the crawler
	 */
	public void stopCrawler(){
		logger.info(CLASSNAME + ": Shutting down crawler");
		shutdown();
	}
	
	/**
	 * Set up the entire crawler framework
	 */
	private void initialise() {
		initialiseDb();
		
		this.headCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		this.getCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.contentForLinkExtractor = new ArrayBlockingQueue<RawCrawledItem>(CrawlerConstants.QUEUE_CAPACITY);
		this.preRedistributionNewURLQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.siteInfoMap = new ConcurrentHashMap<String, SiteInfo>();
		this.sitesCrawledThisSession = new Vector<URL>();
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
		EntityStore store = DBWrapper.setupDatabase(this.dbEnvDir, false);
		this.dao = new CrawlerDao(store);
	}
	
	/**
	 * Initialise the linkQueuer with the IP addresses of all crawlers (including this one)
	 */
	private void linkQueuerThreadPool(){
		
		this.linkQueuerThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_HEAD_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_HEAD_GET_THREADS; i++){
			LinkQueuer linkQueuer = new LinkQueuer(this.preRedistributionNewURLQueue, 
					this.newUrlQueue, this.crawlerNumber, this.otherWorkerIPs, this.excludedPatterns);
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
					i, this.dao, this.storageDirectory, this.urlStorageDirectory);
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

			this.maxDocSize = validateIntString(properties.getProperty("max_doc_size"));
			this.dbEnvDir = CrawlerConstants.DB_DIRECTORY;
			
			String[] excludedPatternsArray = properties.getProperty("excluded_patterns").split(";");
			populateExcludedPatterns(excludedPatternsArray);
			
			this.storageDirectory = CrawlerConstants.STORAGE_DIRECTORY;
			Utils.createDirectory(this.storageDirectory);
			this.urlStorageDirectory = CrawlerConstants.URL_STORAGE_DIRECTORY;
			Utils.createDirectory(this.urlStorageDirectory);
			
			
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
	private String validateIPPortString(String iPPortString){
		if (null != iPPortString && !iPPortString.isEmpty()){
			if (iPPortString.matches(CrawlerConstants.IP_PORT_FORMAT)){
				return iPPortString;				
			}
		}
		throw new ValidationException("Invalid port number: " + iPPortString);
	}
	
	/**
	 * Populates the excluded patterns array
	 * @param excludedPatternsArray
	 */
	private void populateExcludedPatterns(String[] excludedPatternsArray){
		this.excludedPatterns = new ArrayList<String>();
		for (String pattern : excludedPatternsArray){
			this.excludedPatterns.add(pattern);
		}
	}
	
	

}
