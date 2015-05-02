package cis555.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.crawler.Response.ContentType;
import cis555.utils.Utils;

public class GETWorker implements Runnable {
	
	private static final Logger logger = Logger.getLogger(GETWorker.class);
	private static final String CLASSNAME = GETWorker.class.getName();
	
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> crawlQueue;
	private BlockingQueue<URL> newUrlQueue;
	private int id;
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	
	public GETWorker(ConcurrentHashMap<String, SiteInfo> siteInfoMap, BlockingQueue<URL> crawlQueue, 
			BlockingQueue<URL> newUrlQueue, int id, BlockingQueue<RawCrawledItem> contentForLinkExtractor){
		this.siteInfoMap = siteInfoMap;
		this.crawlQueue = crawlQueue;
		this.newUrlQueue = newUrlQueue;
		this.id = id;
		this.contentForLinkExtractor = contentForLinkExtractor;
	}
	
	
	@Override
	public void run() {
		while (Crawler.active){

			URL url = null;
			
			try {
				
				url = crawlQueue.take();
				
				URL filteredURL = CrawlerUtils.filterURL(url.toString());

				if (null == filteredURL){
					continue;
				}
				
				String domain = filteredURL.getHost();
				
				if (!siteInfoMap.containsKey(domain)){
					// Missing Site info ... return to the new URL queue
					this.newUrlQueue.add(filteredURL);
					continue;
				}
				
				SiteInfo info = siteInfoMap.get(domain);		
				String agentName = CrawlerUtils.extractAgent(info);
				
				if (info.canCrawl(agentName)){
					crawl(info, domain, filteredURL);
				} else {
					// Need to wait 	
					this.crawlQueue.add(filteredURL);	
				
				}
				
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": Crawl queue is full, dropping " + url);				
			} catch (InterruptedException e) {
				logger.error(CLASSNAME + ": Unabble to get URL");
				logger.error(CLASSNAME + e.getMessage());
			} catch (IOException e){
				logger.error(CLASSNAME + ": Unable to crawl" + url + " because of " + e.getMessage() + ", skipping");
			} catch (Exception e){
				logger.debug(CLASSNAME + " THROWING EXCEPTION");
				Utils.logStackTrace(e);
			}

		}
		logger.info(CLASSNAME + ": GETWorker " + this.id + " has shut down");
	}
		
	/**
	 * Does a crawl, extracts links, updates with new links, and updates the site info map
	 * @param info
	 * @param domain
	 * @param url
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void crawl(SiteInfo info, String domain, URL url) throws MalformedURLException, IOException {
		
		try {
			Response response = new Response();
			String ifModifiedSinceString = ""; // Not relevant for GET requests
			
			logger.debug(CLASSNAME + ": About to crawl " + url);				
			
			if (url.toString().startsWith("https")){
				response = CrawlerUtils.retrieveHttpsResource(url, CrawlerUtils.Method.GET, ifModifiedSinceString);
			} else {
				response = CrawlerUtils.retrieveHttpResource(url, CrawlerUtils.Method.GET, ifModifiedSinceString);
			} 
			
			if (null == response){
				logger.debug(CLASSNAME + " Non-2xx or 3xx response received from " + url + ", skipping");
				// returns null if the result is non-2xx or 3xx
				return;
			}

			
			updateSiteInfo(info, domain);
			
			byte[] rawContents = response.getResponseBody();
			
			if (null == rawContents){
				logger.error(CLASSNAME + ": Unable to crawl" + url + " because of no content was received, skipping");
				return;
			}
			
			logger.debug(CLASSNAME + ": Crawled " + url);				
			ContentType contentType = response.getContentType();
			
			if (contentType == Response.ContentType.OTHERS){
				// Ignore if content type is not recognised
				return;
			}
			
			RawCrawledItem  forLinkExtractor = new RawCrawledItem(url, rawContents, contentType, true);
			try {
				this.contentForLinkExtractor.add(forLinkExtractor);
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": Link Extractor queue is full, dropping " + url);				
			}
				
		} catch (CrawlerException e){
			logger.debug("Unable to crawl " + url + " because of " + e.getMessage() + ", skipping." );
		} catch (Exception e){
			logger.debug(CLASSNAME + " THROWING EXCEPTION");
			Utils.logStackTrace(e);
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
