package cis555.indexer;

import java.io.InputStream;

public class TXTIndexer extends Indexer {

    public TXTIndexer(InputStream is, String URL, String docID)
            throws Exception {
        super(is, URL, docID);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = this.is.read()) != -1) {
            sb.append((char) ch);
        }
        content = sb.toString();
    }

}
