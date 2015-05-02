/**
 * 
 */
package cis555.searchengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import cis555.aws.utils.AWSClientAdapters;
import cis555.searchengine.utils.DocHitEntity;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

/**
 * @author cis455
 *
 */
public class FetchAndPopulateScript {

    private static int docNumber;
    private static double avgWord;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        IndexTermDAO.setup("database");
        UrlIndexDAO.setup("database");
        PagerankDAO.setup("database");

        // fetchData();

        populateDocIDUrl("document_meta");
        populateIndexTerm("indexer");

    }

    public static void populateDocIDUrl(String dirName) throws IOException {
        System.out.println("Start Building DocID-URL Database...");

        File dir = new File(dirName);
        for (File f : dir.listFiles()) {
            if (f.isHidden())
                continue;
            System.out.println("Start Processing " + f.getName() + "...");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                String[] tokens = line.split("\\s+");
                if (tokens.length < 2)
                    continue;
                if (tokens[0].length() != 32)
                    continue;
                if (tokens[1].length() < '7')
                    continue;
                UrlIndexDAO.putUrlInfo(tokens[0], tokens[1]);
            }
            br.close();
            System.out.println("Finish Processing " + f.getName() + "...");
        }
        System.out.println("Finish Building DocID-URL Database...");
    }

    public static void populateIndexTerm(String dirName) throws IOException {

        System.out.println("Start Building Index-Term Database...");
        getFileNumberAndAvgWord();
        File dir = new File(dirName);
        for (File f : dir.listFiles()) {
            if (f.isHidden())
                continue;
            System.out.println("Start Processing " + f.getName() + "...");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            String lastWord = null;
            int freq = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                String[] tokens = line.split("\\s+", 2);
                if (lastWord == null) {
                    IndexTermDAO.putIndexTerm(tokens[0]);
                } else if (!tokens[0].equals(lastWord)) {
                    IndexTermDAO.putIndexTerm(
                            lastWord,
                            Math.max(
                                    0,
                                    Math.log((docNumber - freq + 0.5)
                                            / (freq + 0.5))));
                    IndexTermDAO.putIndexTerm(tokens[0]);
                    freq = 0;
                }
                lastWord = tokens[0];
                DocHitEntity docHitEntity = new DocHitEntity(tokens[0],
                        tokens[1], avgWord);
                IndexTermDAO.putDocHitEntity(docHitEntity);
                if (docHitEntity.getWordCount() > 0)
                    freq++;
            }
            if (lastWord != null)
                IndexTermDAO.putIndexTerm(
                        lastWord,
                        Math.max(
                                0,
                                Math.log((docNumber - freq + 0.5)
                                        / (freq + 0.5))));
            br.close();
            System.out.println("Finish Processing " + f.getName() + "...");
        }
    }

    private static void getFileNumberAndAvgWord() {


         AmazonDynamoDBClient client = AWSClientAdapters.getDynamoClient();

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

    public static void fetchData() {
        // S3Adapter s3 = new S3Adapter();
        // s3.downloadAllFilesInBucket("documentmeta", "S3DATA");
        // s3.downloadAllFilesInBucket("indexer-output", "S3DATA");

    }

}
