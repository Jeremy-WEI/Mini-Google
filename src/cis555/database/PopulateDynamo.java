package cis555.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cis555.aws.utils.DynamoDao;
import cis555.utils.CrawlerConstants;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

public class PopulateDynamo {

	private static final Logger logger = Logger.getLogger(DBWrapper.class);
	private static final String CLASSNAME = DBWrapper.class.getName();
	
	private Dao dao;
    private AmazonDynamoDBClient dynamoDB;
    
	
	public PopulateDynamo(){
		setupDatabase();
		setupDynamo();
	}

	private void setupDatabase(){
		
//		if (null == dao){
//			String directory = CrawlerConstants.STORAGE_DIRECTORY;
//			DBWrapper wrapper = new DBWrapper(directory, true);
//			dao = wrapper.getDao();
//		}
	}	
	
	public void setupDynamo(){
	
	}
	
	public void populateDynamo(){
		try {
			String tableName = "CrawledDocument";
            // Add an item
            Map<String, AttributeValue> item = newItem(1, "http://www.google.com", "HTML");
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            logger.debug(CLASSNAME + ": Result: " + putItemResult);
		} catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
        	logger.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
        	logger.error("Error Message: " + ace.getMessage());
        }
//		List<CrawledDocument> crawledDocuments = this.dao.getAllCrawledDocuments();
		
	}
	

    private Map<String, AttributeValue> newItem(long docID, String url, String contentType) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("docID", new AttributeValue().withN(Long.toString(docID)));
        item.put("url", new AttributeValue(url));
        item.put("contentType", new AttributeValue(contentType));

        return item;
    }
	
	
	
	
}
