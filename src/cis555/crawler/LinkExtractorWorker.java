package cis555.crawler;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cis555.crawler.Response.ContentType;
import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class LinkExtractorWorker extends Thread {


	private static final Logger logger = Logger.getLogger(LinkExtractorWorker.class);
	private static final String CLASSNAME = LinkExtractorWorker.class.getName();
	
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private int id;
	private CrawlerDao dao;
	private int documentsCrawled;
	private Set<String> sitesCrawledThisSession;
	private MessageDigest digest;
	
	public LinkExtractorWorker(BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			BlockingQueue<URL> preRedistributionNewURLQueue, int id, CrawlerDao dao,
			Set<String> sitesCrawledThisSession) throws NoSuchAlgorithmException {
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.id = id;
		this.dao = dao;
		this.documentsCrawled = 0;
		this.sitesCrawledThisSession = sitesCrawledThisSession;
		this.digest = MessageDigest.getInstance("MD5");
	}

	
	
	/**
	 * Cleans the document, adds meta data to database, and store file to permanent storage (if it's a newly crawled file)

	 */
	@Override
	public void run() {
		while (Crawler.active){
			try {	
				
				RawCrawledItem content = contentForLinkExtractor.take();
				
				URL url = content.getURL();
				byte[] rawContents = content.getRawContents();
				ContentType contentType = content.getContentType();
					
				Document cleansedDoc = null;
				
				boolean isSaved = true;
				
				if (contentType == ContentType.HTML){				
					if (null == cleansedDoc){
						String stringContents = new String(rawContents, CrawlerConstants.CHARSET);
						cleansedDoc = Jsoup.parse(stringContents);						
					}

					if (isSaved){
						addAHrefLinks(cleansedDoc, content.isNew(), url);
						addImgSrcLinks(cleansedDoc, url);
					} else {
						addAHrefLinks(cleansedDoc, false, url);
						addImgSrcLinks(cleansedDoc, url);												
					}

				}
				
				logger.info(CLASSNAME + " Redistribution queue size: " + this.preRedistributionNewURLQueue.size());
				
				
			} catch (UnsupportedEncodingException | IllegalStateException | IllegalArgumentException e1) {
				logger.info(CLASSNAME + " Unable to decode, skipping " + e1.getMessage());
			} catch (Exception e){
				logger.debug(CLASSNAME + " THROWING EXCEPTION");
				Utils.logStackTrace(e);
			}
		}
		logger.info(CLASSNAME + ": LinkExtractorWorker " + this.id + " has shut down");

	}
	
	/**
	 * Stores the source and target links to file (if it's a newly crawled file)
	 * and adds meta data for uncrawled new links
	 * @param cleansedDoc
	 * @param isNew
	 * @param sourceUrl
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws NoSuchAlgorithmException 
	 */
	private void addAHrefLinks(Document cleansedDoc, boolean isNew, URL sourceUrl) throws IllegalArgumentException, UnsupportedEncodingException, MalformedURLException, NoSuchAlgorithmException{
		Elements links = cleansedDoc.select("a[href]");
		
		List<String> newUrls = new ArrayList<String>();
		
		Date startExtractingLinks = new Date();
		
		for (Element link : links){
			
			Date start = new Date();
			
			String urlString = link.attr("abs:href");
			
//			Date urlStringTime = new Date();
//			Date filterString = urlStringTime;
//			Date checkContained = urlStringTime;
//			Date putInQueue = urlStringTime;
//			Date putInDatabase = urlStringTime;			
//			
			if (!urlString.isEmpty()){
				if (!urlString.contains("mailto:")){ // We're ignoring urls with 'mailto's
					URL newUrl = null;
					try {
						
						newUrl = CrawlerUtils.filterURL(urlString);	
						
//						filterString = new Date();
						
						if (null != newUrl){
							String newUrlString = newUrl.toString();
							if (!this.sitesCrawledThisSession.contains(newUrlString)){
								
//								checkContained = new Date();
								
								
								if (this.preRedistributionNewURLQueue.remainingCapacity() > 10){
									this.preRedistributionNewURLQueue.put(newUrl);
									
								}
								
								
//								putInQueue = new Date();
								
								if (!this.dao.doesDocumentMetaExist(newUrl.toString())){
									this.dao.addNewDocumentMeta(newUrl.toString(), getDocID(newUrl), new Date(), false);									
								}
								
								
//								putInDatabase = new Date();
								
							}
							
							newUrls.add(hashUrlToHexStringArray(newUrl.toString()));							
						}
					} catch (IllegalArgumentException | MalformedURLException e){
						logger.info(CLASSNAME + " :" + newUrl + " syntax is invalid " + e.getMessage());
					}
					  catch (IllegalStateException e){
						logger.info(CLASSNAME + " New preRedistributionNewURLQueue is full, dropping " + newUrl);
						
					} catch (Exception e){
						logger.debug(CLASSNAME + " THROWING EXCEPTION");
						Utils.logStackTrace(e);
					}
				}
			}
			
//			logger.info(CLASSNAME + " extractor " + this.id + " took " +
//					(urlStringTime.getTime() - start.getTime()) + "ms to extract link, " + 
//					(filterString.getTime() - urlStringTime.getTime()) + "ms to filter the url" + 
//					(checkContained.getTime() - filterString.getTime()) + "ms to check if we've added the link already " +
//					(putInQueue.getTime() - checkContained.getTime()) + "ms to add url into the queue " + 
//					(putInDatabase.getTime() - putInQueue.getTime()) + "ms to put URL into the database");

		} 
		
		Date savingToDatabase = new Date();
		
		if (isNew){
			this.dao.addNewFromToUrls(hashUrlToHexStringArray(sourceUrl.toString()), newUrls);						
		}		

		Date done = new Date();
		
		logger.info(CLASSNAME + "Extractor " + this.id + " took " + (savingToDatabase.getTime() - startExtractingLinks.getTime()) + "ms to extract links "
				+ (done.getTime() - savingToDatabase.getTime()) + "ms to save to database");
	}
	
	/**
	 * Add img links to database
	 * @param cleasnedDoc
	 * @param sourceUrl
	 * @throws IllegalArgumentException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private void addImgSrcLinks(Document cleasnedDoc, URL sourceUrl) throws IllegalArgumentException, UnsupportedEncodingException, MalformedURLException{
		Elements links = cleasnedDoc.select("img[src]");
		
		List<String> addedImgLinksForOnePage = new ArrayList<String>();
		
		for (Element link : links){
			String urlString = link.attr("abs:src");
			if (!urlString.isEmpty()){
				if (!addedImgLinksForOnePage.contains(urlString)){
					URL newUrl = null;
					try {
						String cleansedString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
						newUrl = CrawlerUtils.filterURL(cleansedString);	
						if (null != newUrl){
							this.dao.addNewDocumentMeta(newUrl.toString(), getDocID(newUrl), new Date(), false);
							addedImgLinksForOnePage.add(urlString);							
						}
					} catch (IllegalStateException e){
						logger.info(CLASSNAME + " Queue is full, dropping " + newUrl);
						
					} catch (IllegalArgumentException e){
						logger.info(CLASSNAME + " " + e.getMessage() + ", dropping");
					} catch (Exception e){
						logger.debug(CLASSNAME + " THROWING EXCEPTION");
						Utils.logStackTrace(e);
					}
				}
			}
		}
	}
	
	/**
	 * Hashes the url to generate a docID
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException  
	 */
	private String getDocID(URL url) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return hashUrlToHexStringArray(url.toString());
	}
	

	
	/**
	 * Returns the number of documents crawled
	 * @return
	 */
	public int getDocumentsCrawled(){
		return this.documentsCrawled;
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
        } catch (UnsupportedEncodingException e) {
            Utils.logStackTrace(e);
            throw new RuntimeException(e);
        }
    }

}
