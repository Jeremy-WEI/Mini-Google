package cis555.aws.utils;

public class AWSConstants {
	
	// DynamoDB tables
	
	public static final String CRAWLED_DOCUMENT_TABLE = "CrawledDocument";
	public static final String CRAWLED_DOCUMENT_HASH_KEY = "docID";
	
	public static final String DOCUMENT_META_TABLE = "DocumentMeta";
	public static final String DOCUMENT_META_HASH_KEY = "url";
	
	// S3 buckets

	public static final String DOCUMENT_BUCKET = "cis555crawleddata";
}
