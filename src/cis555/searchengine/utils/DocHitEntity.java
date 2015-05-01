package cis555.searchengine.utils;

import java.util.LinkedList;
import java.util.List;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/*
 * DocHitEntity is the class representing the DocHit.
 * The class is written as an Entity, to difference from the DocHit in cis555.indexer.
 * 
 * DocHitEntity use an auto-increment ID for PrimaryKey, which we don't really care about it.
 * DocHitEntity use the word as SecondaryKey, so we can search for given word and get all the matching DocHitEntity as a Cursor.  
 * 
 */
@Entity
public class DocHitEntity {

    @PrimaryKey(sequence = "ID")
    private long id;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = IndexTerm.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private String word;

    private double tf;
    private int wordCount;
    private String docID;
    private List<Integer> plainHitLst;
    private List<Integer> fancyHitLst;

    private DocHitEntity() {
    }

    public DocHitEntity(String word, String line, double avgWord) {
        this.word = word;
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
        tf = plainHitLst.size()
                * (1.2 + 1)
                / (plainHitLst.size() + 1.2 * (1 - 0.75 + 0.75 * wordCount
                        / avgWord));
    }

    public List<Integer> getPlainHitLst() {
        return plainHitLst;
    }

    public List<Integer> getFancyHitLst() {
        return fancyHitLst;
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

    public int getWordCount() {
        return wordCount;
    }

    // @Override
    // public int hashCode() {
    // return docID.hashCode();
    // }
    //
    // @Override
    // public boolean equals(Object obj) {
    // if (!(obj instanceof DocHitEntity))
    // return false;
    // DocHitEntity docHit = (DocHitEntity) obj;
    // return docHit.docID.equals(docID);
    // }

    public String toString() {
        return "DocID: " + docID + " tf: " + tf;
    }
}
