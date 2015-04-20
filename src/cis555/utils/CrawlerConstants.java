package cis555.utils;

public class CrawlerConstants {
	
	// Crawler related constants
	
	public static final String CHARSET = "utf-8";
	public static final int QUEUE_CAPACITY = 250000;
	public static final int NUM_HEAD_GET_THREADS = 10;
	public static final int NUM_EXTRACTOR_THREADS = 10;
	public static final int NUM_MATCHER_THREADS = 10;
	
	public static final int THREAD_JOIN_WAIT_TIME = 500;
	public static final int THREAD_SLEEP_TIME = 1000;
	
	public static final String CRAWLER_USER_AGENT = "cis455crawler";
	public static final long DEFAULT_CRAWLER_DELAY_MS = 1000;
	public static final int BYTES_IN_MEGABYTE = 1000000;
	public static final int DEFAULT_CONTENT_LENGTH = Integer.MAX_VALUE;
	
	public static final String[] REDIRECT_STATUS_CODES = {"301", "302", "303", "307"};
	
	public static final String ENCODING = "UTF-8";
	public static final int PORT = 80;
	public static final int SOCKET_TIMEOUT = 10000;

	public static final int MAX_RETRY = 20;
	
	public static final String PROPERTIES_FILE = "properties/settings.properties";
	public static final String IP_PORT_FORMAT = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{2,5}";

	
	// DB constants
	
	public static final String DB_NAME = "Store";
	public static final String DB_COUNTER_KEY = "COUNTER_KEY";
	public static final String DB_DIRECTORY = "crawler_db";

	// File storage contents
	public static final String STORAGE_DIRECTORY = DB_DIRECTORY + "/crawled_files";
	public static final String URL_STORAGE_DIRECTORY = DB_DIRECTORY + "/crawled_urls";
	public static final String URL_STORAGE_FILENAME = "urls.txt";

	// URL filter
	public static final int MAX_URL_LENGTH = 50;
	
	public static final String[] DOMAIN_BLACKLIST = { "yahoo.uservoice.com",
		"login.",
		"games.yahoo.com",
		"javascript:void",
		"/forms",
		};

	
	// Servlet paths
	public static final String SERVLET_DB_DIRECTORY = "/Users/kevinlee/Dropbox/Class/4th semester/CIS555/hw/project/555_project/crawler_db";
	public static final String SERVLET_CRAWLED_FILES_DIR = SERVLET_DB_DIRECTORY + "/crawled_files";
}
