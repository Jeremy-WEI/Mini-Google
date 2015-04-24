package cis555.indexer;

import java.io.File;
import java.util.Map;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {

    private static final String STORE_NAME = "CIS555";
    private String envDirectory;

    private static Environment myEnv;
    private static EntityStore store;
    private PrimaryIndex<String, UrlDocIdInfo> urlIndex;
    private PrimaryIndex<Long, DocIdUrlInfo> docIdIndex;

    public DBWrapper(String envDirectory) {
        this.envDirectory = envDirectory;
        File directory = new File(envDirectory);
        if (!directory.exists()) {
            directory.mkdir();
            start();
            buildDatabase();
        }
    }

    private void buildDatabase() {
        AmazonDynamoDBClient dynamoDB;
        String ACCESS_TOKEN = null;
        String SECRET_TOKEN = null;
        dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(
                ACCESS_TOKEN, SECRET_TOKEN));
        dynamoDB.setRegion(Region.getRegion(Regions.US_EAST_1));
        ScanResult result = null;
        // Set<Long> set = new HashSet<>();
        // long maxDocID = -1;
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("DocumentMeta");
            if (result != null) {
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
            }
            result = dynamoDB.scan(scanRequest);
            // System.out.println(result.getScannedCount());
            for (Map<String, AttributeValue> item : result.getItems()) {
                saveInfo(item.get("uRL").getS(),
                        Long.parseLong(item.get("docID").getN()));
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
            EnvironmentConfig envConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();
            envConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            myEnv = new Environment(new File(envDirectory), envConfig);
            store = new EntityStore(myEnv, STORE_NAME, storeConfig);
            urlIndex = store.getPrimaryIndex(String.class, UrlDocIdInfo.class);
            docIdIndex = store.getPrimaryIndex(Long.class, DocIdUrlInfo.class);
        } catch (DatabaseException dbe) {
            dbe.printStackTrace();
        }
    }

    public void shutdown() {
        if (store != null)
            store.close();
        if (myEnv != null)
            myEnv.close();
    }

    public void sync() {
        if (store != null)
            store.sync();
        if (myEnv != null)
            myEnv.sync();
    }

    public void saveInfo(String url, long docId) {
        urlIndex.put(new UrlDocIdInfo(url, docId));
        docIdIndex.put(new DocIdUrlInfo(url, docId));
    }

    public long getDocId(String url) {
        UrlDocIdInfo item = urlIndex.get(url);
        if (item == null)
            return -1;
        return item.getDocId();
    }

    public String getUrl(long docId) {
        DocIdUrlInfo item = docIdIndex.get(docId);
        if (item == null)
            return null;
        return item.getURL();
    }

    public static void main(String... args) {
        DBWrapper db = new DBWrapper("test");
        db.start();
        System.out.println(db.getDocId("https://www.yahoo.com/"));
        System.out.println(db.getDocId("https://www.yahoo.com123/"));
    }
}
