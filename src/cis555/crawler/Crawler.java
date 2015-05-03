package cis555.crawler;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.database.CrawlerDao;
import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherUtils;
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
	
	protected static boolean active;
	private Map<Integer, String> otherWorkerIPPort;
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
	private List<LinkExtractorWorker> linkExtractorPool;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	
	/* Link Queuer related */
	private List<Thread> linkQueuerThreadPool;
	private Map<Integer, BlockingQueue<URL>> urlsForOtherCrawlers;
	
	protected Map<Integer, BlockingQueue<URL>> getUrlsForOtherCrawlers(){
		return this.urlsForOtherCrawlers;
	}

	public Crawler(BlockingQueue<URL> newUrlQueue, int crawlerNumber, Map<Integer, String> otherWorkerIPs,
			List<String> excludedPatterns, int maxDocSize){
		this.newUrlQueue = newUrlQueue;
		this.crawlerNumber = crawlerNumber;
		this.otherWorkerIPPort = otherWorkerIPs;
		this.excludedPatterns = excludedPatterns;
		this.maxDocSize = maxDocSize;
		Crawler.active = true;
	}
	

	/**
	 * Start the crawler
	 */
	public void startCrawler(){
		try {
			logger.info(CLASSNAME + ": Starting crawler");
			initialise();			
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	}
	
	/**
	 * Stop the crawler
	 */
	public void stopCrawler(){
		logger.info(CLASSNAME + ": Shutting down crawler");
		Crawler.active = false;
		shutdown();
	}
	
	/**
	 * Set up the entire crawler framework
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialise() throws NoSuchAlgorithmException {
		
		this.dbEnvDir = CrawlerConstants.DB_DIRECTORY;
		this.storageDirectory = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.STORAGE_DIRECTORY;
		Utils.createDirectory(this.storageDirectory);
		this.urlStorageDirectory = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.URL_STORAGE_DIRECTORY;
		Utils.createDirectory(this.urlStorageDirectory);
		
		initialiseDb();
		
		this.headCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		this.getCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.contentForLinkExtractor = new ArrayBlockingQueue<RawCrawledItem>(CrawlerConstants.QUEUE_CAPACITY);
		this.preRedistributionNewURLQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.siteInfoMap = new ConcurrentHashMap<String, SiteInfo>();
		this.sitesCrawledThisSession = new Vector<URL>();
		
		this.urlsForOtherCrawlers = new ConcurrentHashMap<Integer, BlockingQueue<URL>>();
		for (int i = 0; i < this.otherWorkerIPPort.size(); i++){
			this.urlsForOtherCrawlers.put(i, new ArrayBlockingQueue<URL>(CrawlerConstants.SMALL_QUEUE_CAPACITY));
		}
		
		linkQueuerThreadPool();
		initialiseLinkExtractorThreadPool();
		initialiseHeadThreadPool();
		initialiseGetThreadPool();
		initialiseMatcherPool();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new RedistributeURLsTask(), 0, DispatcherConstants.REDISTRIBUTE_URLS_FREQUENCY_MS);

	}
	
	/**
	 * Initialise the database
	 */
	private void initialiseDb(){
		EntityStore store = DBWrapper.setupDatabase(this.dbEnvDir, false);
		this.dao = new CrawlerDao(store);
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
		headThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_HEAD_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_GET_THREADS; i++) {
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
		getThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_GET_THREADS; i++) {
			GETWorker crawler = new GETWorker(this.siteInfoMap, this.getCrawlQueue, this.newUrlQueue, i, this.contentForLinkExtractor);
			Thread workerThread = new Thread(crawler);
			workerThread.start();
			getThreadPool.add(workerThread);
		}
	}
	
	/**
	 * Start up a pool of threads for GET workers
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialiseLinkExtractorThreadPool() throws NoSuchAlgorithmException {
		linkExtractorPool = new ArrayList<LinkExtractorWorker>(CrawlerConstants.NUM_EXTRACTOR_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_EXTRACTOR_THREADS; i++) {
			LinkExtractorWorker extractor = new LinkExtractorWorker(this.contentForLinkExtractor, 
					this.preRedistributionNewURLQueue, 
					i, this.dao, this.storageDirectory, this.urlStorageDirectory);
			extractor.start();
			linkExtractorPool.add(extractor);
		}
	}
	
	/**
	 * Initialise the linkQueuer with the IP addresses of all crawlers (including this one)
	 */
	private void linkQueuerThreadPool(){
		
		this.linkQueuerThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_GET_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_QUEUER_THREADS; i++){
			LinkQueuer linkQueuer = new LinkQueuer(this.preRedistributionNewURLQueue, 
					this.newUrlQueue, this.crawlerNumber, this.otherWorkerIPPort.size(), 
					this.excludedPatterns, this.urlsForOtherCrawlers);
			Thread linkQueuerThread = new Thread(linkQueuer);
			linkQueuerThread.start();
			this.linkQueuerThreadPool.add(linkQueuerThread);
			
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

			for (Thread t : this.linkExtractorPool){
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
	
	/**
	 * Returns the number of documents that have been crawled and saved
	 * @return
	 */
	public int getNumCrawledDocuments(){
		try {
			File storageDirectory = new File(CrawlerConstants.DB_DIRECTORY + CrawlerConstants.STORAGE_DIRECTORY);
			return storageDirectory.list().length;			
		} catch (Exception e){
			Utils.logStackTrace(e);
			return -1;
		}
	}

	
	
	/**
	 * Private timer task class to redistribute URLs
	 *
	 */
	
	class RedistributeURLsTask extends TimerTask {
		@Override
		public void run(){
			
			try {
				for (int i = 0; i < otherWorkerIPPort.size(); i++){
					
					BlockingQueue<URL> queue = getUrlsForOtherCrawlers().get(i);
					
					if (queue.isEmpty()){
						continue;
					}
					
					StringBuilder contentBuilder = new StringBuilder();
					contentBuilder.append(DispatcherConstants.NEW_URLS_PARAM + "=");
					
					int items = DispatcherConstants.URLS_TO_SEND;
					
					while (!queue.isEmpty() || items < 0){
						URL url = queue.take();
						String cleansedURL = URLEncoder.encode(url.toString(), CrawlerConstants.CHARSET);			
						contentBuilder.append(cleansedURL + ";");
					}

					String urlString = "http://" + otherWorkerIPPort.get(i)+ "/worker/" + DispatcherConstants.ADD_URLS_URL;
					URL url = new URL(urlString);		
					DispatcherUtils.sendHttpRequest(url, contentBuilder.toString(), DispatcherUtils.Method.POST, true);
				}
				
			} catch (Exception e){
				Utils.logStackTrace(e);
			}
			
		}
	}
	

}
