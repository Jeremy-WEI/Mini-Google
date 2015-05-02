package cis555.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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
	private String storageDirectory;
	private String urlStorageDirectory;
	private int documentsCrawled;
	
	public LinkExtractorWorker(BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			BlockingQueue<URL> preRedistributionNewURLQueue, int id, CrawlerDao dao,
			String storageDirectory, String urlStorageDirectory) {
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.id = id;
		this.dao = dao;
		this.storageDirectory = storageDirectory;
		this.urlStorageDirectory = urlStorageDirectory;
		this.documentsCrawled = 0;
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
				
				if (content.isNew()){
					String docID = getDocID(url);
					if (contentType != ContentType.PDF){
						String stringContents = new String(rawContents, CrawlerConstants.CHARSET);
						if (contentsIsEnglish(stringContents)){
							if (contentType == ContentType.TEXT){
								storeCrawledContentsFile(stringContents.toString(), docID, contentType, url);							
							} else {
								cleansedDoc = Jsoup.parse(stringContents);
								storeCrawledContentsFile(cleansedDoc.toString(), docID, contentType, url);
							}							
							this.dao.addNewCrawledDocument(docID, url.toString(), new Date(), contentType.name());
							logger.debug(CLASSNAME + " stored " + url.toString() + " to file system");
							this.documentsCrawled++;
						} else {
							// otherwise, don't store
							logger.debug(CLASSNAME + " content doesn't pass English test, rejecting " + url);
							isSaved = false;
						}
						
					} else {
						storePDF(rawContents, docID, url);
						this.dao.addNewCrawledDocument(docID, url.toString(), new Date(), contentType.name());
						logger.debug(CLASSNAME + " stored " + url.toString() + " to file system");
						this.documentsCrawled++;
					}
				}
				
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
		
		for (Element link : links){
			String urlString = link.attr("abs:href");
			if (!urlString.isEmpty()){
				if (!urlString.contains("mailto:")){ // We're ignoring urls with 'mailto's
					URL newUrl = null;
					try {
						String cleansedString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
						newUrl = CrawlerUtils.filterURL(cleansedString);	
						if (null != newUrl){
							if (!this.preRedistributionNewURLQueue.contains(newUrl)){
								this.preRedistributionNewURLQueue.add(newUrl);
								this.dao.addNewDocumentMeta(newUrl.toString(), getDocID(newUrl), new Date(), false);								
							}
							
							newUrls.add(Utils.hashUrlToHexStringArray(newUrl.toString()));							
						}
					} catch (IllegalStateException e){
						logger.info(CLASSNAME + " New preRedistributionNewURLQueue is full, dropping " + newUrl);
						
					} catch (Exception e){
						logger.debug(CLASSNAME + " THROWING EXCEPTION");
						Utils.logStackTrace(e);
					}
				}
			}
		} 
		
		if (isNew){
			this.dao.addNewFromToUrls(Utils.hashUrlToHexStringArray(sourceUrl.toString()), newUrls);						
		}		
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
		return Utils.hashUrlToHexStringArray(url.toString());
//		if (this.dao.doesDocumentMetaExist(url.toString())){
//			// Previously crawled document
//			return this.dao.getDocIDFromURL(url.toString());
//		} else {
//			// New document, so generate a new ID
//			return ;
//		}
//
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
	 * Returns the number of documents crawled
	 * @return
	 */
	public int getDocumentsCrawled(){
		return this.documentsCrawled;
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
