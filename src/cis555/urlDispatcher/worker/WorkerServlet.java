package cis555.urlDispatcher.worker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.crawler.Crawler;
import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherException;
import cis555.urlDispatcher.utils.DispatcherUtils;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

@SuppressWarnings("serial")
public class WorkerServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(WorkerServlet.class);
	private static final String CLASSNAME = WorkerServlet.class.getName();

	private String masterIP;
	private int port;
	private BlockingQueue<URL> newUrlQueue;
	private Map<Integer, String> otherWorkerIPPort;
	private Crawler crawler;
	private int crawlerNumber;
	private List<String> excludedPatterns;
	private int maxDocSize;

	/**
	 * Get the address that the master is on
	 * @return
	 */
	protected String getMasterIP(){
		return this.masterIP;
	}

	/**
	 * Get the port number of this worker
	 * @return
	 */
	protected int getPort(){
		return this.port;
	}
	
	/**
	 * Get the number of urls read
	 * @return
	 */
	protected int getUrlsCrawled(){
		if (null == this.crawler){
			return 0;
		} else {
			return this.crawler.getNumCrawledDocuments();
		}
	}
	
	public WorkerServlet(){}
	
	@Override
	public void init(){
		
		this.masterIP = getInitParameter(DispatcherConstants.MASTER_KEY_XML);
		
		String databaseDirectory = getInitParameter(DispatcherConstants.STORAGE_DIRECTORY_KEY_XML);		
		CrawlerConstants.DB_DIRECTORY = databaseDirectory;
		
		String portString = getInitParameter(DispatcherConstants.PORT_KEY_XML);
		this.port = Integer.parseInt(portString); 
		
		this.newUrlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);

		String[] excludedPatternsArray = getInitParameter(DispatcherConstants.EXCLUDED_PATTERNS_KEY_XML).split(";");
		populateExcludedPatterns(excludedPatternsArray);
		
		this.maxDocSize = Integer.parseInt(getInitParameter(DispatcherConstants.MAX_SIZE_KEY_XML));
		
		this.otherWorkerIPPort = new HashMap<Integer, String>();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new PingMasterTask(), 0, DispatcherConstants.PING_MASTER_FREQUENCY_MS);
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
	
	/***
	 * GET related methods
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			case DispatcherConstants.STOP_URL:
				stopCrawler();
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	  
	}
	
	private void stopCrawler(){
		if (null != this.crawler){
			this.crawler.stopCrawler();
			this.crawler = null;
		}
	}
	
	
	/***
	 * POST related methods
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			
			case DispatcherConstants.START_URL:
				startCrawler(request);
				break;
				
			case DispatcherConstants.ADD_URLS_URL:	
				addNewUrls(request);
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	}
	
	/**
	 * Deals with request to start the crawler
	 * @param request
	 * @throws MalformedURLException 
	 */
	private void startCrawler(HttpServletRequest request) throws MalformedURLException{
		
		// Populate the relevant details for the crawler
		String crawlerNumString = request.getParameter(DispatcherConstants.CRAWLER_NAME_PARAM);
		this.crawlerNumber = parseCrawlerNumber(crawlerNumString);
		addOtherCrawlersInfo(request);
		addNewUrls(request);
		
		// And start crawling
		
		this.crawler = new Crawler(this.newUrlQueue, this.crawlerNumber, this.otherWorkerIPPort, this.excludedPatterns, this.maxDocSize);
		this.crawler.startCrawler();
		
	}
	
	
	/**
	 * Parse out this crawler's number
	 * @param crawlerNumString
	 */
	private int parseCrawlerNumber(String crawlerNumString) throws DispatcherException {
		String numString = crawlerNumString.substring(6);
		if (numString.matches("\\d+")){
			return Integer.parseInt(numString);
		}
		throw new DispatcherException("Invalid crawler num string: " + crawlerNumString);
	}
	
	/**
	 * Add other crawlers' info. Looks for worker<n> in the request URI, and stores the value for the parameter (IP:port combination)
	 * @param request
	 */
	private void addOtherCrawlersInfo(HttpServletRequest request){
		Set<String> keys = request.getParameterMap().keySet();
		for (String key : keys){
			if (key.matches("worker\\d+")){
				int crawlerNum = parseCrawlerNumber(key);
				otherWorkerIPPort.put(crawlerNum, request.getParameter(key));
			}
			
		}
	}
	
	/**
	 * Adds urls to the queue
	 * @param request
	 * @throws MalformedURLException 
	 */
	private void addNewUrls(HttpServletRequest request) {
		String newUrlString = request.getParameter(DispatcherConstants.NEW_URLS_PARAM);
		if (null == newUrlString){
			logger.error(CLASSNAME + " Null urls received");
			return;
		}
		String[] newUrls = request.getParameter(DispatcherConstants.NEW_URLS_PARAM).split(";");
		
		
		for (String url : newUrls){
			try {
				this.newUrlQueue.add(new URL(url));
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": New URL queue is full, dropping " + url);				
			} catch (MalformedURLException e){
				logger.info(CLASSNAME + ": "+ url + " is invalid, skipping");								
			}
		}
	}

	@Override
	public void destroy(){
		stopCrawler();
	}
	
	/**
	 * Private timer task class to ping the master
	 *
	 */
	
	class PingMasterTask extends TimerTask {
		@Override
		public void run(){
			StringBuilder str = new StringBuilder();
			str.append("http://" + getMasterIP() + "/master/" + DispatcherConstants.WORKER_STATUS_URL + "?");
			str.append(DispatcherConstants.PORT_PARAM + "=" + getPort() + "&");
			str.append(DispatcherConstants.PAGES_CRAWLED_PARAM + "=" + getUrlsCrawled());
			
			try {
				URL url = new URL(str.toString());		
				String content = "";
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.GET, false);
			} catch (Exception e){
				Utils.logStackTrace(e);
			}
			
		}
	}

}
  
