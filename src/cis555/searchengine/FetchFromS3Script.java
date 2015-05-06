package cis555.searchengine;

import cis555.aws.utils.S3Adapter;

public class FetchFromS3Script {
	
	public static void main(String[] args) {
	         S3Adapter s3 = new S3Adapter();
//	         s3.downloadAllFilesInBucket("documentmeta", "S3DATA");
//	         s3.downloadDirectoryInBucket("indexer-output","200k-output", "S3DATA");
	         s3.downloadAllFilesInBucket("cis555crawleddata", "S3DATA");
	         s3.downloadDirectoryInBucket("wcbucket555", "crawlout35k", "S3DATA");

	}
}
