package cis555.crawler;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.database.CrawlerDao;
import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherUtils;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;
import cis555.utils.FromToUrls;
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
	private int numThreads;
	
	private List<Thread> getThreadPool;
	private List<Thread> matcherThreadPool;

	private BlockingQueue<URL> newUrlQueue;
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> getCrawlQueue;
	private Set<String> sitesCrawledThisSession; // A list of sites crawled in this session, to prevent repeated crawl
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
			List<String> excludedPatterns, int maxDocSize, int numThreads){
		this.newUrlQueue = newUrlQueue;
		this.crawlerNumber = crawlerNumber;
		this.otherWorkerIPPort = otherWorkerIPs;
		this.excludedPatterns = excludedPatterns;
		this.maxDocSize = maxDocSize;
		this.numThreads = numThreads;
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
		
		this.getCrawlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.contentForLinkExtractor = new ArrayBlockingQueue<RawCrawledItem>(CrawlerConstants.QUEUE_CAPACITY);
		this.preRedistributionNewURLQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.NEW_URL_QUEUE_CAPACITY);
		
		this.siteInfoMap = new ConcurrentHashMap<String, SiteInfo>();
		this.sitesCrawledThisSession = new HashSet<String>();

		this.urlsForOtherCrawlers = new ConcurrentHashMap<Integer, BlockingQueue<URL>>();
		for (int i = 0; i < this.otherWorkerIPPort.size(); i++){
			this.urlsForOtherCrawlers.put(i, new ArrayBlockingQueue<URL>(CrawlerConstants.SMALL_QUEUE_CAPACITY));
		}
		
		linkQueuerThreadPool();
		initialiseLinkExtractorThreadPool();
		initialiseGetThreadPool();
		initialiseMatcherPool();
		loadInStartingURLS();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new RedistributeURLsTask(), 0, DispatcherConstants.REDISTRIBUTE_URLS_FREQUENCY_MS);

	}
	
	/**
	 * Load in urls from the stored list
	 */
	private void loadInStartingURLS(){
		List<FromToUrls> fromToUrls = dao.getAllFromToDocuments();
		
		if (fromToUrls.size() < 1){
			logger.info("No urls to load");
			return;
		}
		
		Random rand = new Random();
		
		for (int i = 0; i < CrawlerConstants.URL_SEED_SIZE; i++){
			
			int randNumber = rand.nextInt(fromToUrls.size());
			
			FromToUrls url = fromToUrls.get(randNumber);
			try {
				this.newUrlQueue.put(new URL(url.getFromUrl()));
				logger.info(CLASSNAME + " loaded in " + url.getFromUrl());
			} catch (Exception e) {
				// ignore
			}
		}
		
	}
	
	/**
	 * Initialise the database
	 */
	private void initialiseDb(){
		
		EntityStore store = DBWrapper.setupDatabase(this.dbEnvDir, false);
		this.dao = new CrawlerDao(store);
		logger.info(CLASSNAME + " Database initialised");
	}
	
	/**
	 * Initialise the matcherthreads
	 */
	private void initialiseMatcherPool(){
		this.matcherThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_MATCHER_THREADS);
		for (int i = 0; i < CrawlerConstants.NUM_MATCHER_THREADS; i++){
			RobotsMatcher matcher = new RobotsMatcher(siteInfoMap, newUrlQueue, getCrawlQueue, sitesCrawledThisSession);
			Thread robotsMatcherThread = new Thread(matcher);
			robotsMatcherThread.start();
			this.matcherThreadPool.add(robotsMatcherThread);
		}	
	}
	
	/**
	 * Start up a pool of threads for GET workers
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	private void initialiseGetThreadPool() throws NoSuchAlgorithmException {
		getThreadPool = new ArrayList<Thread>(this.numThreads);
		for (int i = 0; i < this.numThreads; i++) {
			GETWorker crawler = new GETWorker(this.siteInfoMap, this.getCrawlQueue, this.newUrlQueue, 
					i, this.contentForLinkExtractor, this.dao, this.storageDirectory, this.maxDocSize);
			Thread workerThread = new Thread(crawler);
			workerThread.start();
			getThreadPool.add(workerThread);
		}
		
		logger.info(CLASSNAME + " Get pool size: " + getThreadPool.size());
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
					i, this.dao, this.sitesCrawledThisSession);
			extractor.start();
			linkExtractorPool.add(extractor);
		}
	}
	
	/**
	 * Initialise the linkQueuer with the IP addresses of all crawlers (including this one)
	 */
	private void linkQueuerThreadPool(){
		
		this.linkQueuerThreadPool = new ArrayList<Thread>(CrawlerConstants.NUM_QUEUER_THREADS);
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
