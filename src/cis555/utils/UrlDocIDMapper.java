package cis555.utils;

import java.io.File;
import java.util.Map;

import cis555.aws.utils.AWSClientAdapters;
import cis555.aws.utils.AWSConstants;
import cis555.indexer.DocIdUrlInfo;
import cis555.indexer.UrlDocIdInfo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Class that provides a mapping from URL to docID, as well as from docID to URL
 * Involves generating a local database
 *
 */
public class UrlDocIDMapper {

    private String envDirectory;

    private static EntityStore store;
    private PrimaryIndex<String, UrlDocIdInfo> urlIndex;
    private PrimaryIndex<Long, DocIdUrlInfo> docIdIndex;

    public UrlDocIDMapper(String envDirectory) {
        this.envDirectory = envDirectory;
        File directory = new File(envDirectory);
        if (!directory.exists()) {
            directory.mkdir();
            buildDatabase();
        }
    }

    private void buildDatabase() {
        start();
        AmazonDynamoDBClient dynamoDB = AWSClientAdapters.getDynamoClient();
        ScanResult result = null;
        // Set<Long> set = new HashSet<>();
        // long maxDocID = -1;
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(AWSConstants.DOCUMENT_META_TABLE);
            if (result != null) {
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
            }
            result = dynamoDB.scan(scanRequest);
            // System.out.println(result.getScannedCount());
            for (Map<String, AttributeValue> item : result.getItems()) {
                saveInfo(
                        item.get(AWSConstants.DOCUMENT_META_URL_FIELD).getS(),
                        Long.parseLong(item.get(
                                AWSConstants.DOCUMENT_META_DOCID_FIELD).getN()));
                // System.out.println(item.get("uRL").getS());
                // maxDocID = Math.max(maxDocID,
                // Long.parseLong(item.get("docID").getN()));
                // set.add(Long.parseLong(item.get("docID").getN()));
            }
        } while (result.getLastEvaluatedKey() != null);
        // for (long i = 0; i < 28400; i++) {
        // if (!set.contains(i))
        // System.out.println(i);
        // }
        // System.out.println(maxDocID);
        shutdown();
    }

    public void start() {
        try {
            store = DBWrapper.setupDatabase(envDirectory, false);
            urlIndex = store.getPrimaryIndex(String.class, UrlDocIdInfo.class);
            docIdIndex = store.getPrimaryIndex(Long.class, DocIdUrlInfo.class);
        } catch (DatabaseException dbe) {
            dbe.printStackTrace();
        }
    }

    public void shutdown() {
        DBWrapper.shutdown();
    }

    public void sync() {
        DBWrapper.sync();
    }

    private void saveInfo(String url, long docId) {
        urlIndex.put(new UrlDocIdInfo(url, docId));
        docIdIndex.put(new DocIdUrlInfo(url, docId));
    }

    /**
     * Returns the docID for the corresponding URL
     * 
     * @param url
     * @return
     */
    public long getDocId(String url) {
        UrlDocIdInfo item = urlIndex.get(url);
        if (item == null)
            return -1;
        return item.getDocId();
    }

    /**
     * Returns the url for a particular docID
     * 
     * @param docId
     * @return
     */
    public String getUrl(long docId) {
        DocIdUrlInfo item = docIdIndex.get(docId);
        if (item == null)
            return null;
        return item.getURL();
    }

    // FOR TESTING PURPOSES ONLY
    // public static void main(String... args) {
    // UrlDocIDMapper db = new UrlDocIDMapper("test");
    // db.start();
    // // System.out.println(db.getDocId("https://www.yahoo.com/"));
    // // System.out.println(db.getDocId("https://www.yahoo.com123/"));
    // System.out.println(db.getUrl(356));
    // System.out.println(Hit.getHitType(-1610612718));
    // }

}
