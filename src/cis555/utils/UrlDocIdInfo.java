package cis555.utils;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UrlDocIdInfo {

    @PrimaryKey
    private String url;
    private long docId;

    private UrlDocIdInfo() {
    }

    public UrlDocIdInfo(String url, long docId) {
        this.url = url;
        this.docId = docId;
    }

    public long getDocId() {
        return docId;
    }
}
