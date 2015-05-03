package cis555.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cis555.crawler.Response.ContentType;
import cis555.database.CrawlerDao;
import cis555.utils.CrawledDocument;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;


/**
 * Responsible for doing HEAD requests to determine whether URL satisfies criteria
 * @author cis455
 *
 */
public class HEADWorker implements Runnable {
	
	private static final Logger logger = Logger.getLogger(HEADWorker.class);
	private static final String CLASSNAME = HEADWorker.class.getName();
	
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> headCrawlQueue;
	private BlockingQueue<URL> getCrawlQueue;
	private BlockingQueue<URL> newUrlQueue; // Only used for re-directs
	private int id;
	private int maxDocSize;
	private CrawlerDao dao;
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private Vector<URL> sitesCrawledThisSession;
	private String storageDirectory;
	
	public HEADWorker(ConcurrentHashMap<String, SiteInfo> siteInfoMap, BlockingQueue<URL> headCrawlQueue, CrawlerDao dao, BlockingQueue<URL> getCrawlQueue, 
			int id, int maxDocSize, BlockingQueue<URL> newUrlQueue, BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			Vector<URL> sitesCrawledThisSession, String storageDirectory){
		this.siteInfoMap = siteInfoMap;
		this.headCrawlQueue = headCrawlQueue;
		this.dao = dao;
		this.getCrawlQueue = getCrawlQueue;
		this.id = id;
		this.maxDocSize = maxDocSize;
		this.newUrlQueue = newUrlQueue;
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.sitesCrawledThisSession = sitesCrawledThisSession;
		this.storageDirectory = storageDirectory;
	}
	
	
	@Override
	public void run() {
		while (Crawler.active){

			URL url = null;
			
			try {
				url = headCrawlQueue.take();
				
				URL filteredURL = CrawlerUtils.filterURL(url.toString());
				
				if (null == filteredURL){
					continue;
				}
								
				String domain = filteredURL.getHost();
				
				if (!siteInfoMap.containsKey(domain)){
					// Missing Site info ... return to the new URL queue
					
					logger.warn(CLASSNAME + ": Missing site info map for " + domain);
					this.newUrlQueue.put(filteredURL);
					continue;
				}
				
				SiteInfo info = siteInfoMap.get(domain);		
				String agentName = CrawlerUtils.extractAgent(info);
				
				if (info.canCrawl(agentName)){
					crawl(info, domain, filteredURL);
				} else {
					// Returning to the queue

					headCrawlQueue.put(filteredURL);
				}
				
			} catch (CrawlerException e){
				logger.debug(CLASSNAME + ": URL rejected because " + e.getMessage());
				
			}	catch (IllegalStateException e){
				logger.info(CLASSNAME + ": New url queue is full, dropping " + url);
			} catch (InterruptedException e) {
				logger.error(CLASSNAME + ": Unabble to get URL");
				logger.error(CLASSNAME + e.getMessage());
			} catch (IOException e){
				logger.error(CLASSNAME + ": Unable to crawl" + url + " because of " + e.getMessage() + ", skipping");
			}  catch (Exception e){
				logger.debug(CLASSNAME + " THROWING EXCEPTION");
				Utils.logStackTrace(e);
			}

		}
		logger.info(CLASSNAME + ": HEADWorker " + this.id + " has shut down");
	}
	
	/**
	 * Performs a HEAD request, and queues the result appropriately
	 * Also updates the site info map with the latest crawl time
	 * @param info
	 * @param url
	 * @param url
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void crawl(SiteInfo info, String domain, URL url) throws MalformedURLException, IOException{
		
		Response response = new Response();
		
		String ifModifiedSinceString = generateIfModifiedSinceString(url);
		
		if (url.toString().startsWith("https")){
			response = CrawlerUtils.retrieveHttpsResource(url, CrawlerUtils.Method.HEAD, ifModifiedSinceString);
		} else {
			response = CrawlerUtils.retrieveHttpResource(url, CrawlerUtils.Method.HEAD, ifModifiedSinceString);
		}
		
		if (null == response){
			logger.debug(CLASSNAME + " Non-2xx or 3xx response received from " + url + ", skipping");
			// returns null if the result is non-2xx or 3xx
			return;
		}
		
		updateSiteInfo(info, domain);
		// Now deal with response
		
		if (isRedirected(response)){
			URL redirectedURL = response.getLocation();
			// Adds the URL to the new URL queue
			logger.debug(CLASSNAME + ": Redirected URL " + redirectedURL + " added to newUrlQueue");
			
			try {
				
				if (!this.newUrlQueue.contains(redirectedURL)){
					this.newUrlQueue.put(redirectedURL);									
				}
				
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": New url queue is full, dropping " + redirectedURL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 	else if (isNotModified(response)){
			logger.info(CLASSNAME + ": " + url + " not modified, using database version");
			
			String contents = retrieveDocument(url);
			
			if (!contents.isEmpty()){ // This means that it's an HTML document
				RawCrawledItem  forLinkExtractor = new RawCrawledItem(url, contents.getBytes(CrawlerConstants.CHARSET), ContentType.HTML, false);
				try {
					this.contentForLinkExtractor.put(forLinkExtractor);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
		} else if (isTooBig(response.getContentLength())){
			logger.info("File exceeds maximum file length " + response.getContentLength() + ", skipping");
		} else if (!isValidResponseType(response)){
			logger.debug(CLASSNAME + ": URL " + url + " ignored as is neither HTML nor XML");
		} else if (!response.getContentLanguage().contains("en") && !response.getContentLanguage().equals("NONE")){
			logger.debug(CLASSNAME + ": URL " + url + " ignored as it's not in English but is in " + response.getContentLanguage());		
		} else {
			
			try {
				
				if (!this.getCrawlQueue.contains(url)){
					
					
					this.getCrawlQueue.put(url);
					
					logger.info(CLASSNAME + " Get queue size: "+ this.getCrawlQueue.size());
					
					// Also add to list of all sites crawled in this session to prevent crawling the same site multiple times
					
					if (this.sitesCrawledThisSession.size() > CrawlerConstants.QUEUE_CAPACITY){
						this.sitesCrawledThisSession.removeAllElements();
					}
					
					this.sitesCrawledThisSession.add(url);
				}				
				
			} catch (IllegalStateException e){
				logger.info(CLASSNAME + ": Get queue is full, dropping " + url);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
	
	/**
	 * Checks if response is redirected or not
	 * @param response
	 * @return
	 */
	private boolean isRedirected(Response response){
		String responseCode = response.getResponseCode();
		if (Arrays.asList(CrawlerConstants.REDIRECT_STATUS_CODES).contains(responseCode)){
			if (null != response.getLocation()){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Determines whether the content type of the response is neither html, xml, text or pdf
	 * @param response
	 * @return
	 */
	private boolean isValidResponseType(Response response){
		return (!(response.getContentType() == Response.ContentType.OTHERS));
	}
		
	/**
	 * Ensures that the size of the response will be smaller than the maximum tolerated document size
	 * @param response
	 */
	private boolean isTooBig(int responseSize){
		// If response doesn't have a content length then ... response size would be 0
		
		int maxDocSizeInBytes = maxDocSize * CrawlerConstants.BYTES_IN_MEGABYTE;
		return (responseSize > maxDocSizeInBytes);
		
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

	
	/**
	 * Retrieve a document from the database, if the document is HTML
	 * @param url
	 * @return
	 */
	private String retrieveDocument(URL url){
		
		if (!this.sitesCrawledThisSession.contains(url)){
			
			if (this.dao.hasUrlBeenCrawled(url.toString())){
				CrawledDocument document = this.dao.getCrawledDocumentByURL(url.toString());
				
				if (document.getContentType().equals("HTML")){
					return retrieveDocumentFromFileSystem(url);
				}
			}
		}
		
		// Already crawled in this session
		// Not an HTML document
		// Or has not been crawled at all so ignore
		return "";
	
	}
	
	
	/**
	 * Retrieves document from the file system
	 * @param url
	 * @return
	 */
	private String retrieveDocumentFromFileSystem(URL url){
		String documentID = this.dao.getDocIDFromURL(url.toString());
		String fileName = this.storageDirectory + "/" + documentID + ".html.gzip";
		File file = new File(fileName);
		if (file.exists()){
			try {
				byte[] bytes = Utils.unzip(file);
				return new String(bytes);				
			} catch (IOException e){
				e.printStackTrace();
				logger.error(CLASSNAME + ": Unable to open file " + fileName +  ", skipping");
				return "";
			}
		}  else {
			logger.error("File " + fileName + ", does not exist in storage, skipping");
			return "";			
		}
	}
	
	
	/**
	 * Generates an "If-Modified-Since: " string if file already exists in the database. If not, returns empty string
	 * @param url
	 * @return
	 */
	private String generateIfModifiedSinceString(URL url){
		
		if (!this.dao.hasUrlBeenCrawled(url.toString())){
			return "";
		} else {
						
			Date lastCrawlDate = this.dao.getLastCrawlDate(url.toString());
			SimpleDateFormat format = new SimpleDateFormat();
			format.applyPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
			String dateString = format.format(lastCrawlDate);
			
//			logger.info(CLASSNAME + ": " + url + " already exists in database with latest crawl date " + dateString);
			return dateString;
			
		}
		
	}
	
	/**
	 * Checks whether it's a not-modified response
	 * @param response
	 * @return
	 */
	private boolean isNotModified(Response response){
		String responseCode = response.getResponseCode();
		return responseCode.equals("304");
	}
		
}
