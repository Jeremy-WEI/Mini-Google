package cis555.searchengine.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocIdPagerankInfo {
	
	@PrimaryKey
    private String docId;
    private Double pagerank;
    
    private DocIdPagerankInfo() {}
    
    public DocIdPagerankInfo(String docId, Double pagerank) {
        this.docId = docId;
        this.pagerank = pagerank;
    }

    public Double getPagerank() {
        return pagerank;
    }
}
