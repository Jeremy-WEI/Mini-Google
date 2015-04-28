package cis555.indexer;

import java.io.InputStream;

/*
 * XML handler is internally the same as a TXTIndexer.
 * Some extra work need to be done for better indexing?
 */
public class XMLIndexer extends Indexer {

    public XMLIndexer(InputStream is, String URL, String docID)
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