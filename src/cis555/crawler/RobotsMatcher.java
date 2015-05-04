package cis555.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;


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
	private Set<String> sitesCrawledThisSession;
	
	public RobotsMatcher(ConcurrentHashMap<String, SiteInfo> siteInfoMap, BlockingQueue<URL> newUrlQueue, 
			BlockingQueue<URL> headCrawlQueue, Set<String> sitesCrawledThisSession){
		this.siteInfoMap = siteInfoMap;
		this.newUrlQueue = newUrlQueue;
		this.headCrawlQueue = headCrawlQueue;
		this.sitesCrawledThisSession = sitesCrawledThisSession;
	}
	
	@Override
	public void run() {
		while(Crawler.active){
			URL url = null;

			Date start = new Date();
			Date populateRobots = new Date();
			Date headQueueing = start;
			Date finished = start;			
			
			try {
				url = newUrlQueue.take();
				
				if (this.sitesCrawledThisSession.contains(url.toString())){
					// We've already looked at this site, so can ignore
					continue;
				}
				
				URL filteredURL = CrawlerUtils.filterURL(url.toString());
				
				if (null == filteredURL){
					continue;
				}
				
				if (null == filteredURL.getHost() || filteredURL.getHost().isEmpty()){
					continue;
				}
				
				
				String domain = filteredURL.getHost();
				if (this.siteInfoMap.containsKey(domain)){
					
					// Queue the file to head if valid
					
					SiteInfo info = this.siteInfoMap.get(domain);	
					String agent = CrawlerUtils.extractAgent(info);
					
					headQueueing = new Date();
					
					populateHeadCrawlerQueue(info, filteredURL, agent);
				} else {
					
					// Get the robots.txt file
					
					populateSiteInfo(filteredURL);
					// Re-queue the URL
//					logger.debug(CLASSNAME + ": Re-queuing " + url + " post Robots.txt extraction");
					
					populateRobots = new Date();
					
					try {
						this.newUrlQueue.put(filteredURL);

					} catch (IllegalStateException e){
						logger.info(CLASSNAME + ": New url queue is full, dropping " + url);
					}
				}
				
				finished = new Date();
				
			}  catch (InterruptedException e) {
				logger.error(CLASSNAME + ": Unable to get URL due to interruption");
				logger.error(CLASSNAME + e.getMessage());
			} catch (Exception e){
				logger.debug(CLASSNAME + " THROWING EXCEPTION");
				Utils.logStackTrace(e);
			}
			
//			logger.info(CLASSNAME + " finished populating head queue, using " +
//			(populateRobots.getTime() - start.getTime()) + "ms for robots, " + 
//					(headQueueing.getTime() - populateRobots.getTime()) + "ms for retrieving site info, " +
//			(finished.getTime() - headQueueing.getTime()) + "ms for adding to head queuer (" + 
//					(finished.getTime() - start.getTime()) + "ms total)");

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

			SiteInfo info = new SiteInfo();
			
			if (null == response){
				logger.debug(CLASSNAME + " Non-2xx or 3xx response received from " + url + ". Adding dummy site info. ");
				// returns null if the result is non-2xx or 3xx
				this.siteInfoMap.put(url.getHost(), info);
			} else {
				String contents = new String(response.getResponseBody(), CrawlerConstants.CHARSET);
				if (null == contents || contents.isEmpty()){
					logger.debug(CLASSNAME + ": Unable to get Robots.txt for " + url.getHost() + " because response body was empty. Adding dummy site info. Original URL: " + url.toString());
					
				} else {
					info.parseRobotsTxt(contents);	
				}
				
				this.siteInfoMap.put(url.getHost(), info);
				
			}

			
//			logger.debug(CLASSNAME + ": Robots.txt added for " + url.getHost());
			
		} catch (CrawlerException | ResponseException e){
			logger.debug(CLASSNAME + ": Unable to get Robots.txt for " + url + "because of " + e.getMessage() + ", adding dummy site info");
			SiteInfo info = new SiteInfo();
			this.siteInfoMap.put(url.getHost(), info);
			
		}
		catch (MalformedURLException e) {
			System.out.println("URL syntax malformed, skipping :" + requestURL);
			SiteInfo info = new SiteInfo();
			this.siteInfoMap.put(url.getHost(), info);
		} catch (IOException e) {
			logger.error(CLASSNAME + ": Unable to get URL: " + e.getMessage());
			SiteInfo info = new SiteInfo();
			this.siteInfoMap.put(url.getHost(), info);
		} catch (Exception e){
			logger.debug(CLASSNAME + " THROWING EXCEPTION");
			Utils.logStackTrace(e);

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
			try {
				this.newUrlQueue.put(filteredURL);
				
			} catch (IllegalStateException | InterruptedException e){
				logger.info(CLASSNAME + ": Queue for new url (requeue while waiting) is full, dropping " + filteredURL);
			}

		} else {

			// No need to wait - can add to headCrawlQueue 
			try {
				this.headCrawlQueue.put(filteredURL);
				this.sitesCrawledThisSession.add(filteredURL.toString());

//				logger.info(CLASSNAME + " New url queue size: " + this.newUrlQueue.size());
//
				logger.info(CLASSNAME + " Get crawl queue size: " + this.headCrawlQueue.size());

			} catch (IllegalStateException | InterruptedException e){
				logger.info(CLASSNAME + ": Queue for head crawl queue is full, dropping " + url);
			}
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
