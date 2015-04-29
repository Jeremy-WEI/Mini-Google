package cis555.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UrlDocIdInfo {

    @PrimaryKey
    private String url;
    private String docId;

    private UrlDocIdInfo() {
    }

    public UrlDocIdInfo(String url, String docId) {
        this.url = url;
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }
}
