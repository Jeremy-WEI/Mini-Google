package cis555.searchengine.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * An Alexa ranking for each doc id that's been crawled
 *
 */
@Entity
public class DocIDAlexaRanking {

	private DocIDAlexaRanking(){}
	
    @PrimaryKey
    private String docID;
    private int rank;

    public DocIDAlexaRanking(String docID, int rank){
    	this.docID = docID;
    	this.rank = rank;
    }
    
    public String getDocID() {
        return this.docID;
    }
    
    public int getRank(){
    	return this.rank;
    }

}
