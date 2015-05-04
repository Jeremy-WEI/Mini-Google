package cis555.crawler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cis555.crawler.Response.ContentType;
import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class GETWorker implements Runnable {
	
	private static final Logger logger = Logger.getLogger(GETWorker.class);
	private static final String CLASSNAME = GETWorker.class.getName();
	
	private ConcurrentHashMap<String, SiteInfo> siteInfoMap;
	private BlockingQueue<URL> crawlQueue;
	private BlockingQueue<URL> newUrlQueue;
	private int id;
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private String storageDirectory;
	private MessageDigest digest;
	private CrawlerDao dao;
	private int maxDocSize;

	
	public GETWorker(ConcurrentHashMap<String, SiteInfo> siteInfoMap, BlockingQueue<URL> crawlQueue, 
			BlockingQueue<URL> newUrlQueue, int id, BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			CrawlerDao dao, String storageDirectory, int maxDocSize) throws NoSuchAlgorithmException{
		this.siteInfoMap = siteInfoMap;
		this.crawlQueue = crawlQueue;
		this.newUrlQueue = newUrlQueue;
		this.id = id;
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.storageDirectory = storageDirectory;
		this.dao = dao;
		this.digest = MessageDigest.getInstance("MD5");
		this.maxDocSize = maxDocSize;

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
					this.newUrlQueue.put(filteredURL);
					logger.debug(CLASSNAME + " adding back to new url queue due to missing site info map " + filteredURL);
					
					continue;
				}

				
				SiteInfo info = siteInfoMap.get(domain);
				
				String agentName = CrawlerUtils.extractAgent(info);
				
				if (info.canCrawl(agentName)){
					crawl(info, domain, filteredURL);
				} else {
					// Need to wait 
					
					if (this.crawlQueue.remainingCapacity() > 10){						
						this.crawlQueue.put(filteredURL);	
					} // otherwise drop
					

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
			
			logger.debug(CLASSNAME +  ": GETWorker" +  this.id + " is about to crawl " + url);				
			
			Date beforeCrawl = new Date();
			
			if (url.toString().startsWith("https")){
				response = CrawlerUtils.retrieveHttpsResource(url, CrawlerUtils.Method.GET, ifModifiedSinceString);
			} else {
				response = CrawlerUtils.retrieveHttpResource(url, CrawlerUtils.Method.GET, ifModifiedSinceString);
			} 
			
			if (null == response){
				logger.debug(CLASSNAME + " Non-2xx or 3xx response, or timeout response received from " + url + ", skipping");
				// returns null if the result is non-2xx or 3xx, or timeout received
				return;
			}

			Date afterCrawl = new Date();
			
			updateSiteInfo(info, domain);

			logger.debug(CLASSNAME + ": GETWorker" +  this.id + " Crawled " + url + ", about to parse response");
			
			
			// Redirect if is redirected
			
			if (isRedirected(response)){
				URL redirectedURL = response.getLocation();
				// Adds the URL to the new URL queue
				logger.debug(CLASSNAME + ": Redirected URL " + redirectedURL + " added to newUrlQueue");
				
				try {
					
					if (this.newUrlQueue.remainingCapacity() > 10){
						this.newUrlQueue.put(redirectedURL);	
					} // Otherwise we just drop
					
					
				} catch (IllegalStateException e){
					logger.info(CLASSNAME + ": New url queue is full, dropping " + redirectedURL);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (isTooBig(response.getContentLength())){
				logger.info("File exceeds maximum file length " + response.getContentLength() + ", skipping");
			} else if (!isValidResponseType(response)){
				logger.debug(CLASSNAME + ": URL " + url + " ignored as is neither HTML nor XML");
			} else if (!response.getContentLanguage().contains("en") && !response.getContentLanguage().equals("NONE")){
				logger.debug(CLASSNAME + ": URL " + url + " ignored as it's not in English but is in " + response.getContentLanguage());		
			} else {
			
				// We can parse the content
			
				byte[] rawContents = response.getResponseBody();
				
				if (null == rawContents){
					logger.error(CLASSNAME + ": Unable to crawl" + url + " because of no content was received, skipping");
					return;
				}
				
				logger.debug(CLASSNAME + ": GETWorker" +  this.id + " crawled " + url + ", taking " + (afterCrawl.getTime() - beforeCrawl.getTime()) + " ms");				
				ContentType contentType = response.getContentType();
				
				Date beforeSave = new Date();
	
				saveDocumentToDisk(url, rawContents, contentType);
				
				Date afterSave = new Date();
				
				logger.info(CLASSNAME + ": GETWorker" +  this.id + " took: "+ (afterSave.getTime() - beforeSave.getTime()) + "ms to save " + url);
	
				RawCrawledItem  forLinkExtractor = new RawCrawledItem(url, rawContents, contentType, true);
				
				if (this.contentForLinkExtractor.remainingCapacity() > 10){						
					this.contentForLinkExtractor.put(forLinkExtractor);	
				} // otherwise drop
				
				logger.info(CLASSNAME + " Content for link extractor size: "+ this.contentForLinkExtractor.size());
			}	
		} catch (IllegalStateException e){
			logger.info(CLASSNAME + ": Link Extractor queue is full, dropping " + url);				
		} catch (CrawlerException e){
			logger.debug("Unable to crawl " + url + " because of " + e.getMessage() + ", skipping." );
		} catch (Exception e){
			logger.debug(CLASSNAME + " THROWING EXCEPTION");
			Utils.logStackTrace(e);
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
		
		int maxDocSizeInBytes = this.maxDocSize * CrawlerConstants.BYTES_IN_MEGABYTE;
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
	
	
	private void saveDocumentToDisk(URL url, byte[] rawContents, ContentType contentType) throws UnsupportedEncodingException{
		Date startTime = new Date();
		Date checkLanguageTime = startTime;
		Date storeFile = startTime;
		Date saveToDatabaseTime = startTime;
		
		String docID = getDocID(url);
		Document cleansedDoc;
		if (contentType != ContentType.PDF){
			String stringContents = new String(rawContents, CrawlerConstants.CHARSET);
			if (contentsIsEnglish(stringContents)){
				
				checkLanguageTime = new Date();
				
				if (contentType == ContentType.TEXT){
					storeCrawledContentsFile(stringContents.toString(), docID, contentType, url);							
				} else {
					cleansedDoc = Jsoup.parse(stringContents);
					storeCrawledContentsFile(cleansedDoc.toString(), docID, contentType, url);
				}			
				
				storeFile = new Date();
				
				this.dao.addNewCrawledDocument(docID, url.toString(), new Date(), contentType.name());
				
				saveToDatabaseTime = new Date();
				
			} else {
				// otherwise, don't store
				logger.debug(CLASSNAME + " content doesn't pass English test, rejecting " + url);
			}
			
		} else {
			storePDF(rawContents, docID, url);
			this.dao.addNewCrawledDocument(docID, url.toString(), new Date(), contentType.name());
		}
		

		logger.debug(CLASSNAME + " GETWorker " + this.id + " stored " + url.toString() + " to file system using "
				+ (checkLanguageTime.getTime() - startTime.getTime()) + "ms to check language, " +
				(storeFile.getTime() - checkLanguageTime.getTime()) + "ms to store file and " +
				(saveToDatabaseTime.getTime() - storeFile.getTime()) + "ms to save to database");


	}
	
	
	/**
	 * Store the crawled document in a file
	 * @param contents
	 * @param docID
	 * @param url
	 */
	private void storeCrawledContentsFile(String contents, String docID, ContentType contentType, URL url){

		String fileName = generateFileName(docID, contentType);
		File storageFile = new File(this.storageDirectory + "/" + fileName);
		try {
			
			byte[] urlPlusContents = Utils.appendURL(url, contents.getBytes(CrawlerConstants.CHARSET));
			Utils.zip(urlPlusContents, storageFile.toString());
		} catch (IOException e){
			e.printStackTrace();
			logger.error(CLASSNAME + ": Unable to store file " + fileName +  ", skipping");
		} 
	}
	
	/**
	 * Generates the file name with the appropriate extension
	 * @param docID
	 * @param contentType
	 * @return
	 */
	private String generateFileName(String docID, ContentType contentType){
		switch (contentType){
		case HTML:
			return docID + ".html.gzip";
		case XML:
			return docID + ".xml.gzip";
		case TEXT:
			return docID + ".txt.gzip";
		default:
			throw new CrawlerException("Invalid content type: " + contentType.name());
		}
	}
	
	/**
	 * Store a PDF file
	 * @param contents
	 * @param docID
	 * @param url
	 */
	private void storePDF(byte[] contents, String docID, URL url){
		String fileName = docID + ".pdf.gzip";
		File storageFile = new File(this.storageDirectory + "/" + fileName);
		try {
			byte[] urlPlusContents = Utils.appendURL(url, contents);
			Utils.zip(urlPlusContents, storageFile.toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(CLASSNAME + ": Unable to store file " + fileName +  ", skipping");
		}
	}
	
	/**
	 * Hashes the url to generate a docID
	 * @param url
	 * @return
	 */
	private String getDocID(URL url) {
		return hashUrlToHexStringArray(url.toString());
	}
	
    /**
     * Hashes a url using MD5, and returns a Hex-string representation of the
     * hash
     * 
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    public String hashUrlToHexStringArray(String urlString) {
        byte[] hash = hashUrlToByteArray(urlString);
        return DatatypeConverter.printHexBinary(hash);
    }

    /**
     * Hashes a url using MD5, returns a byte array
     * 
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    public byte[] hashUrlToByteArray(String urlString) {
    	try {
        	
            digest.reset();
            digest.update(urlString.getBytes(CrawlerConstants.CHARSET));
            return digest.digest();    		
    	} catch (UnsupportedEncodingException e){
    		Utils.logStackTrace(e);
    		throw new RuntimeException(e);
    	}
    }
    
	/**
	 * Determines if the contents is english by checking if the document contains a number of common english words
	 * @param contents
	 * @return
	 */
	private boolean contentsIsEnglish(String contents){
		int count = 0;
		if (contents.contains(" the ")){
			count++;
		}
		
		if (contents.contains(" and ")){
			count++;
		}
		
		if (contents.contains(" that ")){
			count++;
		}

		if (contents.contains(" not ")){
			count++;
		}

		if (contents.contains(" with ")){
			count++;
		}

		if (contents.contains(" this ")){
			count++;
		}
		
		if (count > 2){
			return true;
		} else {
			return false;
		}

	}
	
}
