package cis555.crawler;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.database.DBWrapper;
import cis555.database.Dao;

public class Crawler {
	
	private static final Logger logger = Logger.getLogger(Crawler.class);
	private static final String CLASSNAME = Crawler.class.getName();
	
	
	/* Database related */
	private String dbEnvDir;
	private Dao dao;
	private int crawlerID = 0;

	/* Crawler related */
	
	private URL startingUrl;
	private int maxDocSize;
	private int crawlLimit;
	private boolean isCrawlLimitSet;
	private List<Thread> getThreadPool;
	private List<Thread> headThreadPool;
	private Thread robotsMatcherThread;

	private BlockingQueue<URL> newUrlQueue;
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> headCrawlQueue;
	private BlockingQueue<URL> getCrawlQueue;
	private Vector<URL> sitesCrawledThisSession; // A list of sites crawled in this session, to prevent repeated crawl
	
	/* Link Extractor related */
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private List<Thread> listExtractorThreadPool;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private Thread linkQueuerThread;
	private DocIDGenerator counterGenerator;
	
	
	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		Crawler crawler = new Crawler();
		crawler.validateArgsAndPopulate(args);
		crawler.initialise();
		while(GETWorker.active){
			Thread.sleep(1000);
		}
		crawler.shutdown();
	}
	
	
	private void initialise() throws MalformedURLException{
		initialiseDb();
		this.newUrlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.URL_QUEUE_CAPACITY);
		this.newUrlQueue.add(this.startingUrl);
//		this.newUrlQueue.add(new URL("http://www.robotstxt.org/faq.html"));
//		this.newUrlQueue.add(new URL("http://www.w3schools.com/html/default.asp"));
		
		this.headCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.URL_QUEUE_CAPACITY);
		this.getCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.URL_QUEUE_CAPACITY);
		
		this.contentForLinkExtractor = new ArrayBlockingQueue<RawCrawledItem>(CrawlerConstants.URL_QUEUE_CAPACITY);
		this.preRedistributionNewURLQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.URL_QUEUE_CAPACITY);
		
		if (isCrawlLimitSet){
			CrawlLimitCounter.setCrawlLimit(this.crawlLimit);
		}
		this.siteInfoMap = new ConcurrentHashMap<String, SiteInfo>();
		this.sitesCrawledThisSession = new Vector<URL>();
		this.counterGenerator = new DocIDGenerator(this.crawlerID, this.dao);
		linkQueuerThread();
		initialiseLinkExtractorThreadPool();
		initialiseHeadThreadPool();
		initialiseGetThreadPool();
		initialiseMatcher();
	}
	
	/**
	 * Initialise the database
	 */
	private void initialiseDb(){
		DBWrapper wrapper = new DBWrapper(this.dbEnvDir);
		this.dao = wrapper.getDao();
	}
	
	/**
	 * Initialise the linkQueuer
	 */
	private void linkQueuerThread(){
		
		// TEMPORARY
		
		Map<String, String> tempListOfOtherWorkers = new HashMap<String, String>();
		tempListOfOtherWorkers.put("1", "192.0.0.1:8080");
		///
		
		LinkQueuer linkQueuer = new LinkQueuer(this.preRedistributionNewURLQueue, 
				this.newUrlQueue, this.crawlerID, tempListOfOtherWorkers);
		linkQueuerThread = new Thread(linkQueuer);
		linkQueuerThread.start();
	}
	
	/**
	 * Initialise the matcherthreads
	 */
	private void initialiseMatcher(){
		RobotsMatcher matcher = new RobotsMatcher(siteInfoMap, newUrlQueue, headCrawlQueue);
		robotsMatcherThread = new Thread(matcher);
		robotsMatcherThread.start();
		
	}
	
	/**
	 * Start up a pool of threads for HEAD workers
	 * 
	 * @return
	 */
	private void initialiseHeadThreadPool() {
		headThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_THREADS; i++) {
			HEADWorker crawler = new HEADWorker(this.siteInfoMap, this.headCrawlQueue, 
					this.dao, this.getCrawlQueue, i, this.maxDocSize, this.newUrlQueue, 
					this.contentForLinkExtractor, this.sitesCrawledThisSession);
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
		listExtractorThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_THREADS; i++) {
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
		getThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_THREADS; i++) {
			LinkExtractorWorker extractor = new LinkExtractorWorker(this.contentForLinkExtractor, this.preRedistributionNewURLQueue, 
					i, this.dao, this.counterGenerator);
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

			if (robotsMatcherThread.isAlive()){
				robotsMatcherThread.join(CrawlerConstants.THREAD_JOIN_WAIT_TIME);
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
	
	/**
	 * 
	 * @param args
	 */
	private void validateArgsAndPopulate(String[] args){
		if (args.length != 3 && args.length != 4){
			logger.info("Invalid number of arguments");
			System.exit(1);
		}
		
		validateAndPopulateStartingUrl(args[0]);
		this.dbEnvDir = (args[1]);
		this.maxDocSize = validateIntString(args[2]);
		
		if (args.length == 4){
			this.crawlLimit = validateIntString(args[3]);
			this.isCrawlLimitSet = true;
		} 
	}

	/**
	 * Validates and populates the starting url string
	 * @param urlString
	 */
	private void validateAndPopulateStartingUrl(String urlString){
		
		try {
			urlString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
			
			if (!urlString.startsWith("http")){
				urlString = "http://" + urlString;
			}
			this.startingUrl = new URL(urlString);			
			
		} catch (MalformedURLException | UnsupportedEncodingException e){
			logger.info("URL syntax invalid");
			System.exit(1);			
		}
	}	
	
	/**
	 * Validates an int string
	 * @param sizeString
	 * @return
	 */
	private int validateIntString(String sizeString){
		try {
			int size = Integer.parseInt(sizeString);
			if (size > 0){
				return size;
			} else {
				logger.info("Integer must be greater than 0");
				System.exit(1);				
			}
		} catch (NumberFormatException e){
			logger.info("Invalid value for integer: " + sizeString);
			System.exit(1);		
		}
		// should never get here...!
		return -1;
	}
}
