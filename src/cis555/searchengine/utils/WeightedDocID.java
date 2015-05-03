package cis555.searchengine.utils;

import java.util.LinkedList;
import java.util.List;

public class WeightedDocID implements Comparable<WeightedDocID> {

    private String docID;
    private double weight;
    private List<DocHitEntity> docHits;
    private int previewStartPos;
    private int previewEndPos;

    public WeightedDocID(String docID) {
        this.docID = docID;
        this.weight = 0;
        this.docHits = new LinkedList<DocHitEntity>();
        this.previewStartPos = -1;
        this.previewEndPos = -1;
    }

    public String getDocID() {
        return docID;
    }

    public double getWeight() {
        return weight;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addWeight(double weight) {
        this.weight += weight;
    }

    public void mutiplyWeight(double factor) {
        this.weight *= factor;
    }

    public void addDocHit(DocHitEntity docHit) {
        docHits.add(docHit);
    }

    public List<DocHitEntity> getDocHits() {
        return docHits;
    }

    @Override
    public int compareTo(WeightedDocID o) {
        if (weight < o.weight)
            return 1;
        if (weight == o.weight)
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
        return "DocID:" + docID + ", Weight: " + weight;
    }
}
