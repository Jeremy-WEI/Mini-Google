package cis555.indexer;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLIndexer extends Indexer {

    private Document document;

    public HTMLIndexer(InputStream is, String URL, String docID)
            throws Exception {
        super(is, URL, docID);
        this.document = Jsoup.parse(this.is, Charset.defaultCharset().name(),
                URL);
        this.content = document.body().text();
    }

    public void parse() {
        parseElement(docID, 7, URL);
        for (Element e : document.select("title")) {
            parseElement(docID, 6, e.text());
            // parseElement(docID, tagToHitType("title"), e.text());
        }
        for (Element e : document.select("meta")) {
            parseElement(docID, 5, e.attr("content") + " " + e.attr("title"));
            // parseElement(docID, tagToHitType("meta"), e.attr("content") + " "
            // + e.attr("title"));
        }
        for (Element e : document.select("a[href]")) {
            String lDocID = getDocID(e.attr("abs:href"));
            parseElement(lDocID, 4, e.text() + " " + e.attr("title"));
            // parseElement(lDocID, tagToHitType("a"),
            // e.text() + " " + e.attr("title"));
        }
        for (Element e : document.select("img[src]")) {
            String iDocID = getDocID(e.attr("abs:src"));
            parseElement(iDocID, 3, e.attr("alt") + " " + e.attr("title"));
            // parseElement(iDocID, tagToHitType("img"), e.attr("alt") + " "
            // + e.attr("title"));
        }
        parseElement(docID, 0, content);
        // parseElement(docID, tagToHitType("p"), content);
        calTFValue();
    }
}
