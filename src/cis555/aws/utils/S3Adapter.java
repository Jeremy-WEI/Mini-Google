package cis555.aws.utils;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Adapter {

	protected S3Adapter(){}
	
	public static AmazonS3 getClient(){
		AmazonS3 s3 = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(usEast1);
        return s3;
	}
}
