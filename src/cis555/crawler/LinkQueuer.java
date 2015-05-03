package cis555.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;
	
public class LinkQueuer implements Runnable {
	
	private static final Logger logger = Logger.getLogger(LinkQueuer.class);
	private static final String CLASSNAME = LinkQueuer.class.getName();

	
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private BlockingQueue<URL> newUrlQueue;
	private int crawlerID;
	private int numberOfCrawlers;
	private List<String> excludedPatterns;
	private Map<Integer, BlockingQueue<URL>> urlsForOtherCrawlers;
	
	public LinkQueuer(BlockingQueue<URL> preRedistributionNewURLQueue, BlockingQueue<URL> newUrlQueue,
			int crawlerID, int numberOfCrawlers, List<String> excludedPatterns, 
			Map<Integer, BlockingQueue<URL>> urlsForOtherCrawlers){
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.newUrlQueue = newUrlQueue;
		this.crawlerID = crawlerID;
		this.numberOfCrawlers = numberOfCrawlers;
		this.excludedPatterns = excludedPatterns;
		this.urlsForOtherCrawlers = urlsForOtherCrawlers;
	}

	@Override
	public void run() {
		while(Crawler.active){
			try {
				URL url = preRedistributionNewURLQueue.take();
				URL filteredURL = filter(url);
				if (null == filteredURL){
					continue;
				}
				URL redistributedURL = redistributeURL(filteredURL);
				if (null == redistributedURL){
					continue;
				}
				
				try {
					
					if (this.newUrlQueue.remainingCapacity() > 10){						
						this.newUrlQueue.put(filteredURL);
					} 
					
					// if queue is full, we simply drop the url
					
				} catch (IllegalStateException e){
					logger.info(CLASSNAME + " New url queue is full, dropping " + filteredURL);					
				}
				
			} catch (Exception e) {
				Utils.logStackTrace(e);
			} 
		}
	}
	
	/**
	 * Filter out urls with particular characteristics
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	private URL filter(URL url) throws MalformedURLException {
		String urlString = url.toString();
		if (urlString.length() > CrawlerConstants.MAX_URL_LENGTH){
			return null;
		} else if (urlString.contains("#")){
			String newUrlString = urlString.substring(0, urlString.indexOf("#"));
			return new URL(newUrlString);
		}  else {

			return ignoreBlackListDomain(url);
		}
		
	}
	
	/**
	 * Removes any sites that are in a black listed domain
	 * @param url
	 * @return
	 */
	private URL ignoreBlackListDomain(URL url){

		for (String excludedPattern : this.excludedPatterns){
			if (url.toString().contains(excludedPattern)){
				return null;
			}
		}
		return url;
	}
	
	/**
	 * Redistribute URLs to other crawlers based on the hashcode of the url
	 * @param url
	 * @return
	 */
	private URL redistributeURL(URL url){
		int bucket = Utils.determineBucketForURL(url, numberOfCrawlers);
		
		if (bucket == this.crawlerID){
			
			// URL is for this crawler
			
			return url;
		} else {
			try {
				
				// Send URL to other crawlers
				
				if (this.urlsForOtherCrawlers.get(bucket).remainingCapacity() > 10){						
					this.urlsForOtherCrawlers.get(bucket).add(url);
				} 
				
				return null;
				
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": Redistribution queue for crawler " + bucket + " is full, dropping " + url);
				return null;
			}
		}
	}
}
