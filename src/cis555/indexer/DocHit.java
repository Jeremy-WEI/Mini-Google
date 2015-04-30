package cis555.indexer;

import java.util.LinkedList;
import java.util.List;

import cis555.utils.Hit;

public class DocHit {

    private String docID;
    private int wordCount;
    private List<Integer> plainHitLst;
    private List<Integer> fancyHitLst;

    public DocHit(String line) {
        String[] tokens = line.split(",");
        docID = tokens[0].trim();
        wordCount = Integer.parseInt(tokens[1].trim());
        plainHitLst = new LinkedList<Integer>();
        fancyHitLst = new LinkedList<Integer>();
        String plain = tokens[2].trim();
        if (plain.length() > 0)
            for (String hit : plain.split("\\s+"))
                plainHitLst.add(Integer.valueOf(hit));
        String fancy = tokens[3].trim();
        if (fancy.length() > 0)
            for (String hit : fancy.split("\\s+"))
                fancyHitLst.add(Integer.valueOf(hit));
    }

    public DocHit(String docID, int hit) {
        this.docID = docID;
        this.plainHitLst = new LinkedList<Integer>();
        this.fancyHitLst = new LinkedList<Integer>();
        addHit(hit);
    }

    public void merge(DocHit docHit) {
        wordCount = Math.max(wordCount, docHit.wordCount);
        plainHitLst.addAll(docHit.plainHitLst);
        fancyHitLst.addAll(docHit.fancyHitLst);
    }

    public void addHit(int hit) {
        if (Hit.getHitType(hit) == 0)
            plainHitLst.add(hit);
        else
            fancyHitLst.add(hit);
    }

    public void setTotalWordNo(int wordNo) {
        wordCount = wordNo;
    }

    public int getFreq() {
        return plainHitLst.size();
    }

    public String getDocID() {
        return docID;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(docID);
        sb.append(",");
        sb.append(wordCount);
        sb.append(",");
        for (Integer hit : plainHitLst) {
            sb.append(hit);
            sb.append(' ');
        }
        sb.append(" ,");
        for (Integer hit : fancyHitLst) {
            sb.append(hit);
            sb.append(' ');
        }
        sb.append(" ,");
        return sb.toString();
    }
}
