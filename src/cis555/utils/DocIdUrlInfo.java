package cis555.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocIdUrlInfo {

    @PrimaryKey
    private long docId;
    private String url;

    private DocIdUrlInfo() {
    }

    public DocIdUrlInfo(String url, long docId) {
        this.url = url;
        this.docId = docId;
    }

    public String getURL() {
        return url;
    }
}
