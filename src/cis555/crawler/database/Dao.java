package cis555.crawler.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class Dao {

	private static final Logger logger = Logger.getLogger(Dao.class);
	private static final String CLASSNAME = Dao.class.getName();

	private PrimaryIndex<String, CrawledDocument> crawledDocumentDao;
	
	private Date lastCrawlDate;
	
	public Dao(EntityStore store){
		crawledDocumentDao = store.getPrimaryIndex(String.class, CrawledDocument.class);
		lastCrawlDate = null;
	}

	/**
	 * Used for the crawler to update the last crawl date if both servlet and the crawler are in operation
	 * @param date
	 */
	public void setLastCrawlDate(Date date){
		this.lastCrawlDate = date;
	}
	
	/**
	 * Used for channels to figure out when the last time the crawler did a crawl.
	 * Date is set via a previous query, the crawler, or by going through all records 
	 * in the entire database.
	 * This date is not persisted in the database and hence is reset every time Dao is initialised
	 * @return
	 */
	public Date lastCrawlDate(){
		if (null != this.lastCrawlDate){
			return this.lastCrawlDate;
		}
		// last crawl date not set, need to go through database
		Date candidateLastCrawlDate = null;
		Iterator<CrawledDocument> docs = getAllCrawledDocuments();
		while (docs.hasNext()){
			CrawledDocument doc = docs.next();
			Date documentDate = doc.getLastCrawledDate();
			if (null == candidateLastCrawlDate){
				candidateLastCrawlDate = documentDate;
			} else {
				if (documentDate.after(candidateLastCrawlDate)){
					candidateLastCrawlDate = documentDate;
				}
			}
		}
		this.lastCrawlDate = candidateLastCrawlDate;
		return this.lastCrawlDate;
	}
	
	/********
	 * Crawled Document methods
	 * ********
	 */
	
	/**
	 * Add a new crawled document
	 * @param url
	 * @param contents
	 * @param crawlDate
	 */
	public void addNewCrawledDocument(String url, String contents, Date crawlDate, String contentType){
		crawledDocumentDao.put(new CrawledDocument(url, contents, crawlDate, contentType));
	}
	
	/**
	 * Add a new crawled document
	 * @param document
	 */
	public void updateCrawledDocument(CrawledDocument document){
		crawledDocumentDao.put(document);
	}
	
	/**
	 * Retrieve a crawled document
	 * @param url
	 * @return
	 * @throws EntryDoesNotExistException
	 */
	public CrawledDocument getCrawledDocument(String url) {
		if (crawledDocumentDao.contains(url)){
			return crawledDocumentDao.get(url);
		} else {
			throw new EntryDoesNotExistException(url);
		}
	}
	
	/**
	 * Indicates whether the url exists in the database
	 * @param url
	 * @return
	 */
	public boolean doesDocumentExist(String url){
		return crawledDocumentDao.contains(url);
	}

	/**
	 * Update a crawled document
	 * @param url
	 * @param contents
	 * @param crawlDate
	 */
	public void updateCrawledDocument(String url, String contents, Date crawlDate){
		CrawledDocument document = getCrawledDocument(url);
		Date lastCrawledDate = document.getLastCrawledDate();
		if (lastCrawledDate.before(crawlDate)){			
			document.setContents(contents, crawlDate);
			this.crawledDocumentDao.put(document);
		} else {
			logger.info(CLASSNAME + ": " + url + " not updated as last crawl date (" + lastCrawledDate.toString() + 
					") is not earlier thatn current crawl date (" + crawlDate.toString() + ")");
		}
	}
	
	/**
	 * Retrieve an iterator for all crawled documents in the database
	 * @return
	 */
	public Iterator<CrawledDocument> getAllCrawledDocuments(){
		EntityCursor<CrawledDocument> crawledDocumentCursors = this.crawledDocumentDao.entities();
		return crawledDocumentCursors.iterator();	
	}
	

	
	/**
	 * Remove a url
	 * @param url
	 */
	private void removeURL(String url){
		if (this.crawledDocumentDao.contains(url)){
			crawledDocumentDao.delete(url);			
		} else {
			throw new EntryDoesNotExistException(url);
		}
	}
	
	
	/**
	 * Remove all urls from the database
	 * @param url
	 */
	private void removeAllURLs(){
		List<String> urls = getAllCrawledUrls();
		for (String url : urls){
			removeURL(url);
		}
	}

	/**
	 * Returns a list of all urls in the database
	 * @return
	 */
	public List<String> getAllCrawledUrls(){
		List<String> urls = new ArrayList<String>();
		EntityCursor<CrawledDocument> crawledDocumentCursors = this.crawledDocumentDao.entities();
		for (CrawledDocument document = crawledDocumentCursors.first(); null != document; document = crawledDocumentCursors.next()){
			urls.add(document.getURL());
		}
		crawledDocumentCursors.close();
		return urls;
	}
	
	
	
	
	
	/********
	 * Other methods methods
	 * ******
	 */
	
	/**
	 * Remove all records from the database. Must close database after
	 */
	public void clearDatabase(){
		removeAllURLs();
	}
	

}
