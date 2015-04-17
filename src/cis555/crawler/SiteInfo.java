package cis555.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class SiteInfo {
	
	private static final Logger logger = Logger.getLogger(SiteInfo.class);
	private static final String CLASSNAME = SiteInfo.class.getName();
	
	private Date lastCrawledDate;
	private HashMap<String,ArrayList<String>> disallowedLinks;
	private HashMap<String,ArrayList<String>> allowedLinks;
	
	private HashMap<String,Integer> crawlDelays;
	private ArrayList<String> sitemapLinks;
	private ArrayList<String> userAgents;

	
	
	public SiteInfo(){
		disallowedLinks = new HashMap<String,ArrayList<String>>();
		allowedLinks = new HashMap<String,ArrayList<String>>();
		crawlDelays = new HashMap<String,Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
		lastCrawledDate = new Date();
		
	}
	
	/**
	 * Parse the robots.txt file
	 * @param contents
	 */
	public void parseRobotsTxt(String contents){
		if (null == contents || contents.isEmpty()){
			logger.error(CLASSNAME + ": No content to parse!");
			return;
		}
		String[] lines = contents.split("\n");
		
		String userAgent = null;
		
		for (String line : lines){
			
			if (line.startsWith("#")){
				continue;
			}
			
			if (line.isEmpty()){
				userAgent = null;
			} else if (line.toLowerCase().contains("user-agent")){
				userAgent = extractValueFromKeyValuePair(line)[1];
				userAgents.add(userAgent);
			} else if (line.toLowerCase().contains("disallow")){				
				String disallow = extractValueFromKeyValuePair(line)[1];
				disallow = filterAllowDisallowString(disallow);
				if (null != userAgent && !disallow.isEmpty()){
					addDisallowedLink(userAgent, disallow);
				}
			} else if (line.toLowerCase().contains("allow")){
				String allow = extractValueFromKeyValuePair(line)[1];
				allow = filterAllowDisallowString(allow);
				if (null != userAgent && !allow.isEmpty()){
					addAllowedLink(userAgent, allow);
				}
			} else if (line.toLowerCase().contains("crawl-delay")){
				String crawlDelay = extractValueFromKeyValuePair(line)[1];
				if (crawlDelay.matches("\\d+")){
					int delay = Integer.parseInt(crawlDelay);
					if (null != userAgent){
						addCrawlDelay(userAgent, delay);
					}
				}
			}	

		}
	}
	
	/**
	 * Add a disallowed path for a particular user agent
	 * @param key
	 * @param value
	 */
	public void addDisallowedLink(String key, String value){
		if(!disallowedLinks.containsKey(key)){
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
		else{
			ArrayList<String> temp = disallowedLinks.get(key);
			if(temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
	}
	
	/**
	 * Add an allowed path for a particular user agent
	 * @param key
	 * @param value
	 */
	public void addAllowedLink(String key, String value){
		if(!allowedLinks.containsKey(key)){
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
		else{
			ArrayList<String> temp = allowedLinks.get(key);
			if(temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
	}
	
	/**
	 * Add the crawl delay for a particular user agent
	 * @param key
	 * @param value
	 */
	public void addCrawlDelay(String key, Integer value){
		crawlDelays.put(key, value);
	}
	
	/**
	 * Add a user agent for the domain
	 * @param key
	 */
	public void addUserAgent(String key){
		userAgents.add(key);
	}
	
	/**
	 * Determines if user agent exists for this domain's robots.txt
	 * @param key
	 * @return
	 */
	public boolean containsUserAgent(String key){
		return userAgents.contains(key);
	}
	
	/**
	 * returns the disallowed links for a particular user agent
	 * @param key
	 * @return
	 */
	public ArrayList<String> getDisallowedLinks(String key){
		return disallowedLinks.get(key);
	}
	
	/**
	 * Returns the allowed links for a particular user agent
	 * @param key
	 * @return
	 */
	public ArrayList<String> getAllowedLinks(String key){
		return allowedLinks.get(key);
	}
	
	/**
	 * Gets the crawl delay for the domain. If none exists, returns -1
	 * @param key
	 * @return
	 */
	public int getCrawlDelay(String key){
		if (!crawlDelays.containsKey(key)){
			return -1;
		}
		return crawlDelays.get(key);
	}
	
	/**
	 * Updates the last time this domain was crawled
	 * @param newCrawlDate
	 */
	public void setLastCrawledDate(Date newCrawlDate){
		synchronized(this.lastCrawledDate){
			this.lastCrawledDate = newCrawlDate;
		}
	}
	/**
	 * Lets the workers know whether it can crawl or not, based on the delay time.
	 * If worker can crawl, the internal clock is reset
	 * @param agentName
	 * @return
	 */
	public boolean canCrawl(String agentName){
		int crawlDelay = getCrawlDelay(agentName);
		long crawlDelayMS;
		if (crawlDelay < 0){ // if not defined, use the default 
			crawlDelayMS = CrawlerConstants.DEFAULT_CRAWLER_DELAY_MS;
		} else {
			crawlDelayMS = (long) crawlDelay * 1000;
		}
		
		synchronized(this.lastCrawledDate){
			long diff = new Date().getTime() - this.lastCrawledDate.getTime();
			if (crawlDelayMS < diff){
				
				// Update the last crawled date to stop others from crawling at the same time
				this.lastCrawledDate = new Date();
				return true;
			} else {
				return false;
			}
		}
		
	}
	
	/**
	 * Returns the date at which this particular domain was crawleds
	 * @return
	 */
	public Date getLastCrawledDate(){
		synchronized(this.lastCrawledDate){
			return this.lastCrawledDate;			
		}
	}
	
	/**
	 * Utility method to print the contents of the SiteInfo object
	 */
	public void print(){
		for(String userAgent:userAgents){
			System.out.println("User-Agent: "+userAgent);
			ArrayList<String> dlinks = disallowedLinks.get(userAgent);
			if(dlinks != null)
				for(String dl:dlinks)
					System.out.println("Disallow: "+dl);
			ArrayList<String> alinks = allowedLinks.get(userAgent);
			if(alinks != null)
					for(String al:alinks)
						System.out.println("Allow: "+al);
			if(crawlDelays.containsKey(userAgent))
				System.out.println("Crawl-Delay: "+crawlDelays.get(userAgent));
			System.out.println();
		}
		if(sitemapLinks.size() > 0){
			System.out.println("# SiteMap Links");
			for(String sitemap:sitemapLinks)
				System.out.println(sitemap);
		}
	}
//	
//	public boolean crawlContainAgent(String key){
//		return crawlDelays.containsKey(key);
//	}
	
	
	
	/***
	 * From a key: value string, extract the value
	 * @param header
	 * @return
	 */
	private String[] extractValueFromKeyValuePair(String header) throws ResponseException {
		int colonIndex = header.indexOf(":");
		if (colonIndex < 1){
			logger.error(CLASSNAME + ": Missing colon in header line " + header);
			throw new ResponseException("Missing colon in header line: " + header);
			
		}
		String key = header.substring(0, colonIndex).trim();
		String value = header.substring(colonIndex + 1).trim();
		String[] keyValuePair = {key, value};
		return keyValuePair;
	}
	
	
	/**
	 * Remove any items that contain ? and filter out stuff with '*'
	 * @param allowDisallowstring
	 * @return
	 */
	private String filterAllowDisallowString(String allowDisallowString){
		if (allowDisallowString.contains("?")){
			return "";
		} else if (allowDisallowString.contains("$")){
			return "";
		} else if (allowDisallowString.contains("*")){
			int starIndex = allowDisallowString.indexOf("*");
			if (starIndex < 1){
				return "";
			} else if (allowDisallowString.charAt(starIndex - 1) == '/'){
				// Character before * is a / - return everything up to the star
				return allowDisallowString.substring(0, starIndex - 1);
			} else {
				return "";
			}
		} else {
			return allowDisallowString;
		}
		
	}
	

}
