package cis555.searchengine.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cis555.utils.DocIdUrlInfo;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

/*
 * DBWrapper for SearchEngine database.
 * Provided Interface:
 * 
 * 
 */
public class DBWrapper {

    private static final String STORE_NAME = "CIS455SEARCH_ENGINE";
    private static String envDirectory = null;

    private static int docNumber;
    private static double avgWord;
    private static Environment myEnv;
    private static EntityStore store;
    private static PrimaryIndex<String, DocIdUrlInfo> urlIndex;
    private static PrimaryIndex<String, IndexTerm> termIndex;
    private static PrimaryIndex<Long, DocHitEntity> docHitById;
    private static SecondaryIndex<String, Long, DocHitEntity> docHitByWord;

    public DBWrapper(String envDirectory) {
        DBWrapper.envDirectory = envDirectory;
        File directory = new File(envDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private static void getFileNumberAndAvgWord() {
        String ACCESS_KEY = "AKIAIHWLNGX7VTENATXQ";
        String SECRET_KEY = "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";
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(
                new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
        // DynamoDB dynamoDB = new DynamoDB(client);
        System.out.println("Connecting to DynamoDB...");
        ScanResult result = null;
        long sum = 0;
        do {
            ScanRequest req = new ScanRequest();
            req.setTableName("DocumentWordCount");
            if (result != null) {
                req.setExclusiveStartKey(result.getLastEvaluatedKey());
            }

            result = client.scan(req);
            docNumber += result.getCount();
            for (Map<String, AttributeValue> map : result.getItems()) {
                try {
                    AttributeValue v = map.get("length");
                    sum += Integer.parseInt(v.getN());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } while (result.getLastEvaluatedKey() != null);
        avgWord = sum / (double) docNumber;
        System.out.println("Done Calculating docNumber...");
        System.out.println("docNumber: " + docNumber + ", avgWord: " + avgWord);
    }

    /*
     * return true if successfully start; return false if error happens
     */
    public boolean start() {
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            // envConfig.setLocking(false);
            StoreConfig storeConfig = new StoreConfig();
            envConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            myEnv = new Environment(new File(envDirectory), envConfig);
            store = new EntityStore(myEnv, STORE_NAME, storeConfig);
            urlIndex = store.getPrimaryIndex(String.class, DocIdUrlInfo.class);
            termIndex = store.getPrimaryIndex(String.class, IndexTerm.class);
            docHitById = store.getPrimaryIndex(Long.class, DocHitEntity.class);
            docHitByWord = store.getSecondaryIndex(docHitById, String.class,
                    "word");
            return true;
        } catch (DatabaseException dbe) {
            dbe.printStackTrace();
            return false;
        }
    }

    public void sync() {
        if (store != null)
            store.sync();
        if (myEnv != null)
            myEnv.sync();
    }

    public void shutdown() {
        if (store != null)
            store.close();
        if (myEnv != null)
            myEnv.close();
    }

    public void saveDocIdUrl(String docID, String url) {
        urlIndex.put(new DocIdUrlInfo(url, docID));
    }

    public String getUrl(String docID) {
        DocIdUrlInfo docIdUrlInfo = urlIndex.get(docID);
        if (docIdUrlInfo == null)
            return null;
        return docIdUrlInfo.getURL();
    }

    private void saveIndexTerm(String word) {
        termIndex.put(new IndexTerm(word));
    }

    private void saveIndexTerm(String word, double value) {
        termIndex.put(new IndexTerm(word, value));
    }

    public double getIdfValue(String word) {
        IndexTerm indexTerm = termIndex.get(word);
        if (indexTerm == null)
            return 0;
        return termIndex.get(word).getIdfValue();
    }

    // private void saveDocHit(String word, String line, double avgWords) {
    // docHitById.put(new DocHitEntity(word, line, avgWords));
    // }

    private void saveDocHit(DocHitEntity docHitEntity) {
        docHitById.put(docHitEntity);
    }

    /*
     * Given a word return a List<DocHit>; This function would automatically do
     * a checking: if the URL of docId is not saved in database, then it will
     * not be returned.
     * 
     * TODO: query word process should be done explicitly outside this function.
     */
    public Set<DocHitEntity> getDocHit(String word) {
        EntityCursor<DocHitEntity> cursor = docHitByWord.entities(word, true,
                word, true);
        Set<DocHitEntity> lst = new HashSet<DocHitEntity>();
        for (DocHitEntity docHit : cursor) {
            if (getUrl(docHit.getDocID()) != null) {
                lst.add(docHit);
            }
        }
        cursor.close();
        return lst;
    }

    public void populateDocIDUrl(String fileName) throws IOException {
        System.out.println("Start Building DocID-URL Database...");
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2)
                continue;
            if (tokens[0].length() != 32)
                continue;
            if (tokens[1].length() < '7')
                continue;
            saveDocIdUrl(tokens[0], tokens[1]);
        }
        br.close();
        System.out.println("Finish Building DocID-URL Database...");
    }

    public void populateIndexTerm(String dirName) throws IOException {
        System.out.println("Start Building Index-Term Database...");
        getFileNumberAndAvgWord();
        File dir = new File(dirName);
        for (File f : dir.listFiles()) {
            System.out.println("Start Processing " + f.getName() + "...");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            String lastWord = null;
            int freq = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+", 2);
                if (lastWord == null) {
                    saveIndexTerm(tokens[0]);
                } else if (!tokens[0].equals(lastWord)) {
                    saveIndexTerm(lastWord,
                            Math.log((docNumber - freq + 0.5) / (freq + 0.5)));
                    saveIndexTerm(tokens[0]);
                    freq = 0;
                }
                lastWord = tokens[0];
                DocHitEntity docHitEntity = new DocHitEntity(tokens[0],
                        tokens[1], avgWord);
                saveDocHit(docHitEntity);
                if (docHitEntity.getWordCount() > 0)
                    freq++;
            }
            br.close();
            System.out.println("Finish Processing " + f.getName() + "...");
        }
        System.out.println("Finish Building Index-Term Database...");
    }

    public static void main(String... args) throws IOException {
        DBWrapper db = new DBWrapper("database");
        db.start();
        db.populateDocIDUrl("document_meta.txt");
        db.populateIndexTerm("indexer");
        // Set<DocHitEntity> lst1 = db.getDocHit("comput");
        // Set<DocHitEntity> lst2 = db.getDocHit("scienc");
        // // Set<DocHitEntity> lst3 = db.getDocHit("donor");
        // // for (DocHitEntity docHit : lst2) {
        // // System.out.println(db.getUrl(docHit.getDocID()));
        // // }
        // lst1.retainAll(lst2);
        // // System.out.println(lst1);
        // for (DocHitEntity docHit : lst1) {
        // System.out.println(db.getUrl(docHit.getDocID()));
        // }
        db.shutdown();
    }
}
