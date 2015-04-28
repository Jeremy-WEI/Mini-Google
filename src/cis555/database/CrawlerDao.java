package cis555.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.aws.utils.DocumentMeta;
import cis555.utils.CrawlerConstants;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class CrawlerDao {

	private static final Logger logger = Logger.getLogger(CrawlerDao.class);
	private static final String CLASSNAME = CrawlerDao.class.getName();

	private PrimaryIndex<String, CrawledDocument> crawledDocumentDao;
	private PrimaryIndex<String, DocumentMeta> documentMetaDao;
	private PrimaryIndex<String, CounterObject> counterDao;
	
	public CrawlerDao(EntityStore store){
		crawledDocumentDao = store.getPrimaryIndex(String.class, CrawledDocument.class);
		documentMetaDao = store.getPrimaryIndex(String.class, DocumentMeta.class);
		counterDao = store.getPrimaryIndex(String.class, CounterObject.class);
	}
	
	/********
	 * Counter method (solely for recording the latest docID number)
	 * ********
	 */
	
	/**
	 * Write the latest value of the counter to database
	 * @param counterValue
	 */
	public void writeCounterValue(long counterValue){
		this.counterDao.putNoReturn(new CounterObject(counterValue));
	}
	
	/**
	 * Get the latest value of the counter
	 * @return
	 */
	public long getLatestCounterValue(){
		if (this.counterDao.contains(CrawlerConstants.DB_COUNTER_KEY)){
			return this.counterDao.get(CrawlerConstants.DB_COUNTER_KEY).getDocID();
		} else {
			
			// should not get here
			return -1;
		}
	}

	
	
	/********
	 * DocumentMetaData methods
	 * ********
	 */
	
	/**
	 * Add a new meta object (regardless of whether it has been crawled or not) 
	 * @param url
	 * @param docID
	 * @param crawlDate
	 * @param isCrawled
	 */
	public void addNewDocumentMeta(String url, String docID, Date crawlDate, boolean isCrawled){
		documentMetaDao.putNoReturn(new DocumentMeta(url, docID, crawlDate, isCrawled));
	}
	

	/**
	 * Retrieve the meta data object for particular URL string
	 * @param url
	 * @return
	 */
	public DocumentMeta getDocumentMetaData(String url){
		if (documentMetaDao.contains(url)){
			return documentMetaDao.get(url);
		} else {
			throw new EntryDoesNotExistException(url);
		}
	}
	
	/**
	 * Get the docID for a particular url
	 * @param url
	 * @return
	 */
	public String getDocIDFromURL(String url){
		return getDocumentMetaData(url).getDocID();
	}
	
	/**
	 * Get the last crawled date for a url
	 * @param url
	 * @return
	 */
	public Date getLastCrawlDate(String url){
		return getDocumentMetaData(url).getLastCrawledDate();
	}
	
	
	/**
	 * Indicates whether a meta object exists for a particular URL string
	 * @param url
	 * @return
	 */
	public boolean doesDocumentMetaExist(String url){
		return documentMetaDao.contains(url);
	}
	
	/********
	 * Crawled Document methods
	 * ********
	 */
	
	/**
	 * Add a new crawled document, as well as a new document meta data object
	 * @param url
	 * @param contents
	 * @param crawlDate
	 */
	public void addNewCrawledDocument(String docID, String url, Date crawlDate, String contentType){
		crawledDocumentDao.putNoReturn(new CrawledDocument(docID, url, contentType));
		addNewDocumentMeta(url, docID, crawlDate, true);
	}
	
	/**
	 * Retrieve a crawled document by document ID
	 * @param docID
	 * @return
	 * @throws EntryDoesNotExistException
	 */
	public CrawledDocument getCrawledDocumentByID(String docID) throws EntryDoesNotExistException {
		if (crawledDocumentDao.contains(docID)){
			return crawledDocumentDao.get(docID);
		} else {
			throw new EntryDoesNotExistException(docID);
		}
	}
	
	/**
	 * Retrieve a crawled document
	 * @param url
	 * @return
	 * @throws EntryDoesNotExistException
	 */
	public CrawledDocument getCrawledDocumentByURL(String url) throws EntryDoesNotExistException {
		String docID = getDocumentMetaData(url).getDocID();
		return getCrawledDocumentByID(docID);
	}
	
	/**
	 * Indicates whether we have already crawled a particular docID
	 * @param docid
	 * @return
	 */
	public boolean hasDocIDBennCrawled(String docID){
		return crawledDocumentDao.contains(docID);
	}	
	
	/**
	 * Indicates whether we have already crawled a particular URL
	 * @param url
	 * @return
	 */
	public boolean hasUrlBeenCrawled(String url){
		if (!doesDocumentMetaExist(url)){
			return false;
		}
		String docID = getDocumentMetaData(url).getDocID();
		return hasDocIDBennCrawled(docID);
	}
	
	
	
	/********
	 * Batch methods
	 * ******
	 */
	
	/**
	 * Retrieve a list of all crawled documents in the database
	 * @return
	 */
	public List<CrawledDocument> getAllCrawledDocuments(){
		List<CrawledDocument> documents = new ArrayList<CrawledDocument>();
		EntityCursor<CrawledDocument> crawledDocumentCursors = this.crawledDocumentDao.entities();
		for (CrawledDocument document = crawledDocumentCursors.first(); null != document; document = crawledDocumentCursors.next()){
			documents.add(document);
		}
		crawledDocumentCursors.close();
		return documents;
	}
	

	/**
	 * Returns a list of all urls in the database (from the DocumentMeta table)
	 * @return
	 */
	public Set<String> getAllCrawledUrls(){
		return this.documentMetaDao.map().keySet();
	}
	

	/**
	 * Returns a list of all docids in the database
	 * @return
	 */
	public Set<String> getAllDocIDs(){
		return this.crawledDocumentDao.map().keySet();
	}
	
	
	/**
	 * Retrieve a list of all document meta data objects in the database
	 * @return
	 */
	public List<DocumentMeta> getAllDocumentMetaObjects(){
		List<DocumentMeta> documents = new ArrayList<DocumentMeta>();
		EntityCursor<DocumentMeta> documentCursors = this.documentMetaDao.entities();
		for (DocumentMeta document = documentCursors.first(); null != document; document = documentCursors.next()){
			documents.add(document);
		}
		documentCursors.close();
		return documents;
	}
	
	
	/********
	 * Delete methods
	 * ******
	 */
	
	
	/**
	 * Delete a single document meta object
	 */
	private void deleteDocumentMeta(String url) throws EntryDoesNotExistException {
		if (this.documentMetaDao.contains(url)){
			this.documentMetaDao.delete(url);
		} else {
			throw new EntryDoesNotExistException(url);
		}
	}
	
	/**
	 * Delete a single crawled document
	 */
	private void deleteCrawledDocument(String docID) throws EntryDoesNotExistException {
		if (this.crawledDocumentDao.contains(docID)){
			this.crawledDocumentDao.delete(docID);
		} else {
			throw new EntryDoesNotExistException(docID);
		}
	}
	
	/**
	 * Delete all document meta entries
	 */
	private void deleteAllDocumentMeta(){
		Set<String> urls = getAllCrawledUrls();
		for (String url : urls){
			deleteDocumentMeta(url);
		}
	}
	
	/**
	 * Delete all crawled document entries
	 */
	private void deleteAllCrawledDocuments(){
		Set<String> docIDs = getAllDocIDs();
		for (String docID : docIDs){
			deleteCrawledDocument(docID);
		}
	}
	
	/**
	 * Delete entire database
	 */
	public void deleteDatabase(){
		deleteAllDocumentMeta();
		deleteAllCrawledDocuments();
	}
	

}
