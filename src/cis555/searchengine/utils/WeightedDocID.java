package cis555.searchengine.utils;

import java.util.LinkedList;
import java.util.List;

public class WeightedDocID implements Comparable<WeightedDocID> {

    private String docID;
    private double plainWeight;
    private double fancyWeight;

    private List<DocHitEntity> docHits;
    private int previewStartPos;
    private int previewEndPos;

    public WeightedDocID(String docID) {
        this.docID = docID;
        this.plainWeight = 0;
        this.fancyWeight = 0;
        this.docHits = new LinkedList<DocHitEntity>();
        this.previewStartPos = -1;
        this.previewEndPos = -1;
    }

    public String getDocID() {
        return docID;
    }

    public double getPlainWeight() {
        return plainWeight;
    }

    public double getFancyWeight() {
        return fancyWeight;
    }
    
    public double getWeight() {
        return plainWeight + fancyWeight;
    }

    public int getPreviewStartPos() {
        return previewStartPos;
    }

    public void setPreviewStartPos(int previewStartPos) {
        this.previewStartPos = previewStartPos;
    }

    public int getPreviewEndPos() {
        return previewEndPos;
    }

    public void setPreviewEndPos(int previewEndPos) {
        this.previewEndPos = previewEndPos;
    }

    public void setPlainWeight(double weight) {
        this.plainWeight = weight;
    }

    public void addPlainWeight(double weight) {
        this.plainWeight += weight;
    }

    public void multiplyPlainWeight(double factor) {
        this.plainWeight *= factor;
    }

    public void setFancyWeight(double weight) {
        this.fancyWeight = weight;
    }

    public void addFancyWeight(double weight) {
        this.fancyWeight += weight;
    }

    public void multiplyFancyWeight(double factor) {
        this.fancyWeight *= factor;
    }
    
    public void multiplyWeight(double factor) {
        this.fancyWeight *= factor;
        this.plainWeight *= factor;
    }

    public void addDocHit(DocHitEntity docHit) {
        docHits.add(docHit);
    }

    public List<DocHitEntity> getDocHits() {
        return docHits;
    }

    @Override
    public int compareTo(WeightedDocID o) {
        double weight1 = plainWeight + fancyWeight;
        double weight2 = o.plainWeight + o.fancyWeight;
        if (weight1 < weight2)
            return 1;
        if (weight1 == weight2)
            return 0;
        return -1;
    }

    @Override
    public int hashCode() {
        return docID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WeightedDocID))
            return false;
        WeightedDocID weightedDocID = (WeightedDocID) obj;
        return docID.equals(weightedDocID.docID);
    }

    @Override
    public String toString() {
        return "DocID:" + docID + ", Weight: " + getWeight();
    }
}
