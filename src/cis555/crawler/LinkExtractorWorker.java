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

public class LinkExtractorWorker implements Runnable {

	private static final Logger logger = Logger.getLogger(LinkExtractorWorker.class);
	private static final String CLASSNAME = LinkExtractorWorker.class.getName();
	
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private int id;
	private CrawlerDao dao;
	private String storageDirectory;
	private String urlStorageDirectory;
	public static boolean active;
	
	public LinkExtractorWorker(BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			BlockingQueue<URL> preRedistributionNewURLQueue, int id, CrawlerDao dao,
			String storageDirectory, String urlStorageDirectory) {
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.id = id;
		this.dao = dao;
		this.storageDirectory = storageDirectory;
		this.urlStorageDirectory = urlStorageDirectory;
		LinkExtractorWorker.active = true;
	}

	/**
	 * Cleans the document, adds meta data to database, and store file to permanent storage (if it's a newly crawled file)

	 */
	@Override
	public void run() {
		while (LinkExtractorWorker.active){
			try {
				RawCrawledItem content = contentForLinkExtractor.take();
				
				URL url = content.getURL();
				byte[] rawContents = content.getRawContents();
				ContentType contentType = content.getContentType();
					
				Document cleansedDoc = null;
				
				if (content.isNew()){
					String docID = getDocID(url);
					this.dao.addNewCrawledDocument(docID, url.toString(), new Date(), contentType.name());
					if (contentType != ContentType.PDF){
						String stringContents = new String(rawContents, CrawlerConstants.CHARSET);
						if (contentType == ContentType.TEXT){
							storeCrawledContentsFile(stringContents.toString(), docID, contentType, url);							
						} else {
							cleansedDoc = Jsoup.parse(stringContents);
							storeCrawledContentsFile(cleansedDoc.toString(), docID, contentType, url);
							
						}
					} else {
						storePDF(rawContents, docID, url);
					}
					logger.debug(CLASSNAME + " stored " + url.toString() + " to file system");
				}
				
				if (contentType == ContentType.HTML){				
					if (null == cleansedDoc){
						String stringContents = new String(rawContents, CrawlerConstants.CHARSET);
						cleansedDoc = Jsoup.parse(stringContents);						
					}
					
					addAHrefLinks(cleansedDoc, content.isNew(), url);
					addImgSrcLinks(cleansedDoc, url);
				}					

				
			} catch (InterruptedException | MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException | IllegalStateException | IllegalArgumentException e1) {
				logger.info(CLASSNAME + " Unable to decode, skipping " + e1.getMessage());
			}
		}
	}
	
	/**
	 * Stores the source and target links to file (if it's a newly crawled file)
	 * and adds meta data for uncrawled new links
	 * @param cleansedDoc
	 * @param isNew
	 * @param sourceUrl
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private void addAHrefLinks(Document cleansedDoc, boolean isNew, URL sourceUrl) throws IllegalArgumentException, UnsupportedEncodingException, MalformedURLException{
		Elements links = cleansedDoc.select("a[href]");
		
		List<URL> newUrls = new ArrayList<URL>();
		
		for (Element link : links){
			String urlString = link.attr("href");
			if (!urlString.isEmpty()){
				if (!urlString.contains("mailto:")){ // We're ignoring urls with 'mailto's
					URL newUrl = null;
					try {
						String cleansedString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
						newUrl = CrawlerUtils.convertToUrl(cleansedString, sourceUrl);	
						this.preRedistributionNewURLQueue.add(newUrl);
						this.dao.addNewDocumentMeta(newUrl.toString(), getDocID(newUrl), new Date(), false);
						newUrls.add(newUrl);
					} catch (IllegalStateException e){
						logger.info(CLASSNAME + " New preRedistributionNewURLQueue is full, dropping " + newUrl);
						
					} 
				}
			}
		} 
		
		if (isNew){
			storeUrlsToFile(newUrls, sourceUrl);						
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
			String urlString = link.attr("src");
			if (!urlString.isEmpty()){
				if (!addedImgLinksForOnePage.contains(urlString)){
					URL newUrl = null;
					try {
						String cleansedString = URLDecoder.decode(urlString, CrawlerConstants.CHARSET);
						newUrl = CrawlerUtils.convertToUrl(cleansedString, sourceUrl);		
						this.dao.addNewDocumentMeta(newUrl.toString(), getDocID(newUrl), new Date(), false);
						addedImgLinksForOnePage.add(urlString);
					} catch (IllegalStateException e){
						logger.info(CLASSNAME + " Queue is full, dropping " + newUrl);
						
					} catch (IllegalArgumentException e){
						logger.info(CLASSNAME + " " + e.getMessage() + ", dropping");
					}					
				}
			}
		}
	}
	
	/**
	 * Get the docID from a url. If the url doesn't exist in the database, create a new ID
	 * @param url
	 * @return
	 */
	private String getDocID(URL url){

		if (this.dao.doesDocumentMetaExist(url.toString())){
			// Previously crawled document
			return this.dao.getDocIDFromURL(url.toString());
		} else {
			// New document, so generate a new ID
			return Utils.hashUrlToHexStringArray(url.toString());
		}

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
	 * Appends the new urls to a flat file
	 * @param newUrls
	 * @param sourceUrl
	 */
	private void storeUrlsToFile(List<URL> newUrls, URL sourceUrl){
		String fileName = CrawlerConstants.URL_STORAGE_FILENAME;
		File urlStorageFile = new File(this.urlStorageDirectory + "/" + fileName);
		BufferedWriter writer = null;
		try {
			if (!urlStorageFile.exists()){
				urlStorageFile.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(urlStorageFile.getAbsoluteFile(), true));
			if (newUrls.size() > 0){
				writer.write(Utils.hashUrlToHexStringArray(sourceUrl.toString()) + "\t");
				for (URL newUrl : newUrls){
					writer.write(Utils.hashUrlToHexStringArray(newUrl.toString()) + ";");
				}				
			}
			writer.write("\n");
			
		} catch (IOException e){
			logger.error(CLASSNAME + ": Unable to store file " + fileName +  ", skipping");
		} finally {
			if (null != writer){
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
