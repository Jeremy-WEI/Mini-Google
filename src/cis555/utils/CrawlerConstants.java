package cis555.utils;

public class CrawlerConstants {
	
	// Crawler related constants
	
	public static final String CHARSET = "utf-8";
	public static final int QUEUE_CAPACITY = 250000;
	public static final int SMALL_QUEUE_CAPACITY = 10000;
	public static final int NUM_HEAD_GET_THREADS = 10;
	public static final int NUM_EXTRACTOR_THREADS = 17;
	public static final int NUM_MATCHER_THREADS = 5;
	public static final int NUM_QUEUER_THREADS = 4;
	
	public static final int THREAD_JOIN_WAIT_TIME = 500;
	public static final int THREAD_SLEEP_TIME = 1000;
	
	public static final String CRAWLER_USER_AGENT = "cis455crawler";
	public static final long DEFAULT_CRAWLER_DELAY_MS = 1000;
	public static final int BYTES_IN_MEGABYTE = 1000000;
	public static final int DEFAULT_CONTENT_LENGTH = Integer.MAX_VALUE;
	
	public static final String[] REDIRECT_STATUS_CODES = {"301", "302", "303", "307"};
	
	public static final int MAX_RETRY = 20;
	
	public static final String PROPERTIES_FILE = "properties/settings.properties";
	public static final String IP_PORT_FORMAT = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{2,5}";
	
	// DB constants
	
	public static final String DB_NAME = "Store";
	public static final String DB_COUNTER_KEY = "COUNTER_KEY";
	public static String DB_DIRECTORY = "crawler_db"; // This is actually set by the WorkerServlet's web.xml file
	public static final String LOCK_N_LOCK_TABLES = "17";
	public static final int LOCK_TIMEOUT = 5000;
	
	// File storage contents
	public static final String STORAGE_DIRECTORY = "/crawled_files";
	public static final String URL_STORAGE_DIRECTORY = "/crawled_urls";
	public static final String URL_STORAGE_FILENAME = "urls.txt";
	public static final String DOCUMENT_META_STORAGE_DIRECTORY = "/document_meta";
	public static final String DOCUMENT_META_STORAGE_FILENAME = "document_meta.txt";

	// URL filter
	public static final int MAX_URL_LENGTH = 200;
	public static final int MIN_URL_LENGTH = 10;
	
}
