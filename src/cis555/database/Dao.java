package cis555.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class Dao {

	private static final Logger logger = Logger.getLogger(Dao.class);
	private static final String CLASSNAME = Dao.class.getName();

	private PrimaryIndex<Long, CrawledDocument> crawledDocumentDao;
	private PrimaryIndex<String, DocumentMeta> documentMetaDao;
	private PrimaryIndex<String, CounterObject> counterDao;
	
	public Dao(EntityStore store){
		crawledDocumentDao = store.getPrimaryIndex(Long.class, CrawledDocument.class);
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
	 * Get the latest value of the coutner
	 * @return
	 */
	public long getLatestCounterValue(){
		if (this.counterDao.contains(CrawlerConstants.DB_COUNTER_KEY)){
			return this.counterDao.get(CrawlerConstants.DB_COUNTER_KEY).getDocID();
		} else {
			return -1;
		}
	}

	
	
	/********
	 * DocumentMetaData methods
	 * ********
	 */
	
	/**
	 * Add a new meta object
	 * @param url
	 * @param docID
	 * @param crawlDate
	 */
	public void addNewDocumentMeta(String url, long docID, Date crawlDate){
		documentMetaDao.putNoReturn(new DocumentMeta(url, docID, crawlDate));
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
	public long getDocIDFromURL(String url){
		return getDocumentMetaData(url).getID();
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
	public void addNewCrawledDocument(long docID, String url, Date crawlDate, String contentType){
		crawledDocumentDao.putNoReturn(new CrawledDocument(docID, url, contentType));
		addNewDocumentMeta(url, docID, crawlDate);
	}
	
	/**
	 * Retrieve a crawled document by document ID
	 * @param docID
	 * @return
	 * @throws EntryDoesNotExistException
	 */
	public CrawledDocument getCrawledDocumentByID(long docID) throws EntryDoesNotExistException {
		if (crawledDocumentDao.contains(docID)){
			return crawledDocumentDao.get(docID);
		} else {
			throw new EntryDoesNotExistException(Long.toString(docID));
		}
	}
	
	/**
	 * Retrieve a crawled document
	 * @param url
	 * @return
	 * @throws EntryDoesNotExistException
	 */
	public CrawledDocument getCrawledDocumentByURL(String url) throws EntryDoesNotExistException {
		long docID = getDocumentMetaData(url).getID();
		return getCrawledDocumentByID(docID);
	}
	
	/**
	 * Indicates whether the docid exists in the database
	 * @param docid
	 * @return
	 */
	public boolean doesDocumentExist(long docID){
		return crawledDocumentDao.contains(docID);
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
	public Set<Long> getAllDocIDs(){
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
	private void deleteCrawledDocument(long docID) throws EntryDoesNotExistException {
		if (this.crawledDocumentDao.contains(docID)){
			this.crawledDocumentDao.delete(docID);
		} else {
			throw new EntryDoesNotExistException(Long.toString(docID));
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
		Set<Long> docIDs = getAllDocIDs();
		for (long docID : docIDs){
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
