package cis555.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


/**
 * Responsible for a) grabbing Robots.txt files and b) managing queueing for HEAD requests
 *
 */
public class RobotsMatcher implements Runnable {
	
	private static final Logger logger = Logger.getLogger(RobotsMatcher.class);
	private static final String CLASSNAME = RobotsMatcher.class.getName();

	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> newUrlQueue;
	private BlockingQueue<URL> headCrawlQueue;
	
	public RobotsMatcher(ConcurrentHashMap<String, SiteInfo> siteInfoMap, BlockingQueue<URL> newUrlQueue, BlockingQueue<URL> headCrawlQueue){
		this.siteInfoMap = siteInfoMap;
		this.newUrlQueue = newUrlQueue;
		this.headCrawlQueue = headCrawlQueue;
	}
	
	@Override
	public void run() {
		while(GETWorker.active){
			URL url = null;

			try {
				url = newUrlQueue.take();
				String domain = url.getHost();
				if (this.siteInfoMap.containsKey(domain)){
					
					// Queue the file to head if valid
					
					SiteInfo info = this.siteInfoMap.get(domain);	
					String agent = CrawlerUtils.extractAgent(info);
					populateHeadCrawlerQueue(info, url, agent);
				} else {
					
					// Get the robots.txt file
					
					populateSiteInfo(url);
					// Re-queue the URL
					logger.debug(CLASSNAME + ": Re-queuing " + url + " post Robots.txt extraction");
					newUrlQueue.add(url);
				}
				
				
				
			}  catch (InterruptedException e) {
				logger.error(CLASSNAME + ": Unabble to get URL");
				logger.error(CLASSNAME + e.getMessage());
			} 
		}
		
		logger.info(CLASSNAME + ": Robots Matcher has shut down");
	}
	
	/**
	 * Obtain new robots.txt file
	 * @param url
	 */
	private void populateSiteInfo(URL url){
		URL requestURL = null;
		String requestURLString = url.getProtocol() + "://" +url.getHost() + "/robots.txt";
		try {
			requestURL = new URL(requestURLString);
			Response response = new Response();
			String ifModifiedSinceString = ""; // Not relevant for robots.txt
			if (url.getProtocol().startsWith("https")){
				response = CrawlerUtils.retrieveHttpsResource(requestURL, CrawlerUtils.Method.GET, ifModifiedSinceString);
				
			} else {
				response = CrawlerUtils.retrieveHttpResource(requestURL, CrawlerUtils.Method.GET, ifModifiedSinceString);
			} 
			
			// TO DO: deal with errors
			
			String contents = response.getResponseBody();
			SiteInfo info = new SiteInfo();
			info.parseRobotsTxt(contents);
			
			this.siteInfoMap.put(url.getHost(), info);
			
//			logger.debug(CLASSNAME + ": Robots.txt added for " + url.getHost());
			
		} catch (CrawlerException e){
			logger.error(CLASSNAME + ": Unable to get Robots.txt for " + url + "because of " + e.getMessage() + ", adding dummy site info");
			SiteInfo info = new SiteInfo();
			this.siteInfoMap.put(url.getHost(), info);
			
		}
		catch (MalformedURLException e) {
			System.out.println("URL syntax malformed, skipping :" + requestURL);
		} catch (IOException e) {
			logger.error(CLASSNAME + ": Unable to get URL");
			logger.error(CLASSNAME + e.getMessage());
		}
	}
	

	
	/**
	 * Populate the head crawler queue if enough time has passed, otherwise return to the newURLqueue
	 * @param info
	 * @param urlObject
	 */
	private void populateHeadCrawlerQueue(SiteInfo info, URL url, String agentName){
		
		URL filteredURL = CrawlerUtils.filterURL(info, url, agentName);
		if (null == filteredURL){
			// Not to be crawled - no further work to be done
			return;
		} 
		
		// Check if we can crawl
		
		if (!info.canCrawl(agentName)){
			// need to wait - return to the newUrlQueue
			
//			logger.debug(CLASSNAME + ": Crawler delay imposed on " + url);			
			this.newUrlQueue.add(url);

		} else {

//			logger.debug(CLASSNAME + ": " + url + " added to HeadCrawlQueue");

			// No need to wait - can add to headCrawlQueue 
			this.headCrawlQueue.add(url);
			updateSiteInfo(info, url.getHost());			
		}
	}
	
	
	/**
	 * Updates the site info with the newest crawl date, and puts it back into the siteInfoMap
	 * @param info
	 * @param crawlDate
	 * @param domain
	 */
	private void updateSiteInfo(SiteInfo info, String domain){
		info.setLastCrawledDate(new Date());
		this.siteInfoMap.put(domain, info);
	}

}
