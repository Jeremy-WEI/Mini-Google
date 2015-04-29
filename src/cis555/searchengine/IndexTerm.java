package cis555.searchengine;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 * IndexTerm is the class representing the term in Inverted Index. 
 * Each IndexTerm contains the word and the corresponding idf-value;
 * 
 * e.g. IndexTerm indexTerm;
 *      indexTerm.getIdfValue();
 *      indexTerm.getWord();
 */
@Entity
public class IndexTerm {

    private IndexTerm() {
    }

    @PrimaryKey
    private String word;
    private double idfValue;

    public String getWord() {
        return word;
    }

    public double getIdfValue() {
        return idfValue;
    }

    public void setIdfValue(double idfValue) {
        this.idfValue = idfValue;
    }

    public IndexTerm(String word) {
        this.word = word;
        this.idfValue = 0;
    }

}
