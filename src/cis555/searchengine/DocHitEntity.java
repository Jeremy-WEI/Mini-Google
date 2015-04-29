package cis555.searchengine;

import java.util.LinkedList;
import java.util.List;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class DocHitEntity {

    @PrimaryKey(sequence = "ID")
    private long id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = IndexTerm.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String word;

    private double tf;
    private String docID;
    private List<Integer> hitLst;

    private DocHitEntity() {
    }

    public DocHitEntity(String word, String line) {
        this.word = word;
        String[] tokens = line.split(",");
        docID = tokens[0];
        tf = Double.parseDouble(tokens[1]);
        hitLst = new LinkedList<Integer>();
        for (String hit : tokens[2].split("\\s+"))
            hitLst.add(Integer.valueOf(hit));
    }

    public String getWord() {
        return word;
    }

    public double getTf() {
        return tf;
    }

    public String getDocID() {
        return docID;
    }

    public List<Integer> getHitLst() {
        return hitLst;
    }

    @Override
    public int hashCode() {
        return docID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DocHitEntity))
            return false;
        DocHitEntity docHit = (DocHitEntity) obj;
        return docHit.docID.equals(docID);
    }

    public String toString() {
        return "DocID: " + docID + " tf: " + tf;
    }
}
