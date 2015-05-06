package cis555.searchengine.utils;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocIDContentInfo {

    @PrimaryKey
    private String docId;
    // private String type;
    private String content;

    private DocIDContentInfo() {
    }

    // public DocIDContentInfo(String docId, String type, String content) {
    // this.docId = docId;
    // this.type = type;
    // this.content = content;
    // }

    public DocIDContentInfo(String docId, String content) {
        this.docId = docId;
        this.content = content;
    }

    // public String getType() {
    // return type;
    // }

    public String getContent() {
        return content;
    }

}
