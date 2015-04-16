package cis555.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cis555.database.Dao;

public class LinkExtractorWorker implements Runnable {

	private static final Logger logger = Logger.getLogger(LinkExtractorWorker.class);
	private static final String CLASSNAME = LinkExtractorWorker.class.getName();
	
	private BlockingQueue<RawCrawledItem> contentForLinkExtractor;
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private int id;
	private Dao dao;
	private DocIDGenerator counterGenerator;
	public static boolean active;
	
	public LinkExtractorWorker(BlockingQueue<RawCrawledItem> contentForLinkExtractor, 
			BlockingQueue<URL> preRedistributionNewURLQueue, int id, Dao dao, DocIDGenerator counterGenerator) {
		this.contentForLinkExtractor = contentForLinkExtractor;
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.id = id;
		this.dao = dao;
		this.counterGenerator = counterGenerator;
		LinkExtractorWorker.active = true;
	}

	@Override
	public void run() {
		while (LinkExtractorWorker.active){
			try {
				RawCrawledItem content = contentForLinkExtractor.take();
				
				URL url = content.getURL();
				String rawContents = content.getRawContents();
				String contentType = content.getContentType();
				
				Document cleansedDoc = Jsoup.parse(rawContents);
				
				long docID;
				
				if (content.isNew()){
					// Document hasn't previously been retrieved
					if (this.dao.doesDocumentMetaExist(url.toString())){
						// Previously crawled document
						docID = this.dao.getDocIDFromURL(url.toString());
					} else {
						// New document
						docID = counterGenerator.getDocIDAndIncrement();
					}
					logger.info("Storing " + docID + ": " + url.toString());
					this.dao.addNewCrawledDocument(docID, url.toString(), cleansedDoc.toString(), new Date(), contentType);
					
				}
				

				if (contentType.equals("HTML")){
					Elements links = cleansedDoc.select("a[href]");

					for (Element link : links){
						String urlString = link.attr("href");
						URL newURL = CrawlerUtils.convertToUrl(urlString, url);
						preRedistributionNewURLQueue.add(newURL);	
					}
				}
									
			} catch (InterruptedException | MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
