package cis555.urlDispatcher.utils;

public class DispatcherConstants {
	
	public static final String IP_FORMAT = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
	public static final int PING_MASTER_FREQUENCY_MS = 10000;
	public static final int REDISTRIBUTE_URLS_FREQUENCY_MS = 360000;
	public static final int PING_WORKERS_FREQUENCY_MS = REDISTRIBUTE_URLS_FREQUENCY_MS;
	public static final int HTTP_TIMEOUT = 1000;
	public static final int READ_TIMEOUT = 5000;
	
	public static final int URLS_TO_SEND = 1000;
	
	// GET / PUT URL paths
	
	public static final String STATUS_URL = "status";
	public static final String WORKER_STATUS_URL = "workerstatus";
	
	
	public static final String START_URL = "start";
	public static final String STOP_URL = "stop";
	
	public static final String ADD_URLS_URL = "addUrls";
	
	// URL Parameters
	
	public static final String PORT_PARAM = "port";
	public static final String PAGES_CRAWLED_PARAM = "pagesCrawled";

	public static final String CRAWLER_NAME_PARAM = "crawlerName";
	
	public static final String NEW_URLS_PARAM = "newUrls";

	// Worker Web.xml
	
	public static final String MASTER_KEY_XML = "master";
	public static final String PORT_KEY_XML = "port";
	public static final String STORAGE_DIRECTORY_KEY_XML = "dir";
	public static final String EXCLUDED_PATTERNS_KEY_XML = "excludedPatterns";
	public static final String MAX_SIZE_KEY_XML = "maxDocSize";
	
	// Master Web.xml
	public static final String STARTING_URL_KEY_XML = "startingUrls";

}
