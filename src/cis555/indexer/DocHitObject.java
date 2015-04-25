package cis555.indexer;

import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "InvertedIndex")
public class DocHitObject {

    private String word;
    private long docID;
    private double tfValue;
    private List<Integer> hitLst;

    @DynamoDBHashKey(attributeName = "word")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @DynamoDBRangeKey(attributeName = "docID")
    public long getDocID() {
        return docID;
    }

    public void setDocID(long docID) {
        this.docID = docID;
    }

    @DynamoDBAttribute(attributeName = "tf-value")
    public double getTfValue() {
        return tfValue;
    }

    public void setTfValue(double tfValue) {
        this.tfValue = tfValue;
    }

    @DynamoDBAttribute(attributeName = "hitList")
    public List<Integer> getHitLst() {
        return hitLst;
    }

    public void setHitLst(List<Integer> hitLst) {
        this.hitLst = hitLst;
    }

    public DocHitObject(String line) {
        String[] tokens = line.split("\\s+|,");
        word = tokens[0];
        docID = Long.parseLong(tokens[1]);
        tfValue = Double.parseDouble(tokens[2]);
        hitLst = new LinkedList<Integer>();
        for (int i = 3; i < tokens.length; i++)
            hitLst.add(Integer.valueOf(tokens[i]));
    }

}
