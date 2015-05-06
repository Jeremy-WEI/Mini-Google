/**
 * 
 */
package cis555.searchengine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;

import cis555.aws.utils.AWSClientAdapters;
import cis555.aws.utils.S3Adapter;
import cis555.searchengine.utils.DocHitEntity;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

/**
 * @author cis455
 *
 */
public class PopulateDBScript {

    private static int docNumber;
    private static double avgWord;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        IndexTermDAO.setup("database");
        PagerankDAO.setup("database");
        // UrlIndexDAO.setup("database");
        // ContentDAO.setup("database");

        // populateDocIDUrl("S3DATA/documentmeta");
        populateIndexTerm("S3DATA/indexer-output");
        populatePagerank("S3DATA/wcbucket555");
        // populateDocIDContent("S3DATA/cis555crawleddata");

        // populateIndexTerm("/Users/YunchenWei/Documents/EclipseWorkSpace/555_project/indexer");
        // populateDocIDContent("/Users/YunchenWei/Documents/EclipseWorkSpace/555_project/zipdata");

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

    public static void populateDocIDContent(String dirName) throws IOException {
        System.out.println("Start Building DocID-Content Database...");

        File dir = new File(dirName);
        for (File f : dir.listFiles()) {
            if (f.isHidden())
                continue;
            System.out.println("Start Processing " + f.getName() + "...");
            try {
                // String fileName = f.getName();
                // String docID = fileName.substring(0, fileName.indexOf('.'));
                // String type = fileName.substring(fileName.indexOf('.') + 1,
                // fileName.lastIndexOf('.'));
                // byte[] rawContent = Utils.unzip(f);
                // byte[] content = new byte[rawContent.length
                // - CrawlerConstants.MAX_URL_LENGTH * 2];
                // System.arraycopy(rawContent,
                // CrawlerConstants.MAX_URL_LENGTH * 2, content, 0,
                // rawContent.length - CrawlerConstants.MAX_URL_LENGTH * 2);
                // ContentDAO.putPagerank(docID, type, content);
                String fileName = f.getName();
                String docID = fileName.substring(0, fileName.indexOf('.'));
                String type = fileName.substring(fileName.indexOf('.') + 1,
                        fileName.lastIndexOf('.'));
                byte[] rawContent = Utils.unzip(f);
                byte[] realContent = new byte[rawContent.length
                        - CrawlerConstants.MAX_URL_LENGTH * 2];
                System.arraycopy(rawContent,
                        CrawlerConstants.MAX_URL_LENGTH * 2, realContent, 0,
                        rawContent.length - CrawlerConstants.MAX_URL_LENGTH * 2);
                String content = "";
                switch (type) {
                case "pdf":
                    PDDocument document = PDDocument
                            .load(new ByteArrayInputStream(realContent));
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setStartPage(1);
                    stripper.setEndPage(Integer.MAX_VALUE);
                    content = stripper.getText(document);
                    document.close();
                    break;
                case "html":
                    content = Jsoup
                            .parse(new ByteArrayInputStream(realContent),
                                    Charset.defaultCharset().name(), "").body()
                            .text();
                    break;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    ByteArrayInputStream bais = new ByteArrayInputStream(
                            realContent);
                    int ch;
                    while ((ch = bais.read()) != -1) {
                        stringBuilder.append((char) ch);
                    }
                    content = stringBuilder.toString();
                    break;
                }
                ContentDAO.putPagerank(docID, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public static void populatePagerank(String dirName) throws IOException {

        System.out.println("Start Building Pagerank Database...");
        File dir = new File(dirName);

        for (File f : dir.listFiles()) {
            if (f.isHidden())
                continue;
            System.out.println("Start Processing " + f.getName() + "...");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                if (tokens.length < 2)
                    continue;
                if (tokens[0].length() != 32)
                    continue;
                PagerankDAO.putPagerank(tokens[0],
                        Double.parseDouble(tokens[1]));
            }

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
        S3Adapter s3 = new S3Adapter();
        s3.downloadAllFilesInBucket("documentmeta", "S3DATA");
        s3.downloadDirectoryInBucket("indexer-output", "200k-output", "S3DATA");
        s3.downloadAllFilesInBucket("cis555crawleddata", "S3DATA");
        // s3.downloadDirectoryInBucket("wcbucket555", "crawlout35k", "S3DATA");

    }

}
