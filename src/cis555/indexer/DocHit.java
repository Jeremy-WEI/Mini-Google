package cis555.indexer;

import java.util.LinkedList;
import java.util.List;

public class DocHit {

    private double tf;
    private String docID;
    private List<Integer> hitLst;

    public DocHit(String line, boolean flag) {
        String[] tokens = line.split(",");
        docID = tokens[0];
        tf = Double.parseDouble(tokens[1]);
        hitLst = new LinkedList<Integer>();
        for (String hit : tokens[2].split("\\s+"))
            hitLst.add(Integer.valueOf(hit));
    }

    public DocHit(String docID) {
        this.docID = docID;
        this.hitLst = new LinkedList<Integer>();
    }

    public void merge(DocHit docHit) {
        tf = Math.max(tf, docHit.tf);
        hitLst.addAll(docHit.hitLst);
    }

    public void addHit(int hit) {
        hitLst.add(hit);
    }

    public void calTFvalue(int maxFreq) {
        tf = 0.5 + 0.5 * hitLst.size() / maxFreq;
    }

    public int getFreq() {
        return hitLst.size();
    }

    public String getDocID() {
        return docID;
    }

    public String toString() {
        // StringBuilder sb = new StringBuilder();
        // sb.append("TF: " + tf);
        // sb.append(": DocID: " + docID);
        // sb.append(",[");
        // for (Integer hit : hitLst) {
        // sb.append("{Type:" + Hit.getHitType(hit) + ", Pos:"
        // + Hit.getHitPos(hit) + ", Cap:" + Hit.getHitCap(hit) + "}");
        // }
        // sb.append("]");
        // return sb.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(docID);
        sb.append(',');
        sb.append(tf);
        sb.append(',');
        for (Integer hit : hitLst) {
            sb.append(hit);
            sb.append(' ');
        }
        return sb.toString();
    }
}
