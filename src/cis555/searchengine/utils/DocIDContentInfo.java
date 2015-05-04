package cis555.searchengine.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocIDContentInfo {

    @PrimaryKey
    private String docId;
    private String type;
    private byte[] content;

    private DocIDContentInfo() {
    }

    public DocIDContentInfo(String docId, String type, byte[] content) {
        this.docId = docId;
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

}
