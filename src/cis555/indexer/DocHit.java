package cis555.indexer;
import java.util.LinkedList;
import java.util.List;

public class DocHit {

    private double tf;
    private long docID;
    private List<Integer> hitLst;

    public DocHit(long docID) {
        this.docID = docID;
        this.hitLst = new LinkedList<Integer>();
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TF: " + tf);
        sb.append(": DocID: " + docID);
        sb.append(",[");
        for (Integer hit : hitLst) {
            sb.append("{Type:" + Hit.getHitType(hit) + ", Pos:"
                    + Hit.getHitPos(hit) + ", Cap:" + Hit.getHitCap(hit) + "}");
        }
        sb.append("]");
        return sb.toString();
    }
}
