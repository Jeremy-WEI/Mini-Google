package cis555.aws.utils;

public class AWSConstants {
	
	// DynamoDB tables
	
	public static final String CRAWLED_DOCUMENT_TABLE = "CrawledDocument";
	public static final String CRAWLED_DOCUMENT_DOCID_FIELD = "docID";
	public static final String CRAWLED_DOCUMENT_URL_FIELD = "uRL";
	public static final String CRAWLED_DOCUMENT_CONTENT_TYPE_FIELD = "contentType";
	
	public static final String DOCUMENT_META_TABLE = "DocumentMeta";
	public static final String DOCUMENT_META_URL_FIELD = "uRL";
	public static final String DOCUMENT_META_DOCID_FIELD = "docID";
	public static final String DOCUMENT_META_LAST_CRAWLED_DATE_FIELD = "lastCrawledDate";
	public static final String DOCUMENT_META_ISCRAWLED_FIELD = "crawled";

	// S3 buckets

	public static final String DOCUMENT_BUCKET = "cis555crawleddata";
}
