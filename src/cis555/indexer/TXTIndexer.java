package cis555.indexer;

import java.io.InputStream;

public class TXTIndexer extends Indexer {

    public TXTIndexer(InputStream is, String URL, long docID, DBWrapper db)
            throws Exception {
        super(is, URL, docID, db);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = this.is.read()) != -1) {
            sb.append((char) ch);
        }
        content = sb.toString();
    }

}
