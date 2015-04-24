package cis555.indexer;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cis555.utils.UrlDocIDMapper;

public class HTMLIndexer extends Indexer {

    private Document document;

    public HTMLIndexer(InputStream is, String URL, long docID, UrlDocIDMapper db)
            throws Exception {
        super(is, URL, docID, db);
        this.document = Jsoup.parse(this.is, Charset.defaultCharset().name(),
                URL);
        this.content = document.body().text();
    }

    public void parse() {
        parseElement(docID, 7, URL);
        for (Element e : document.select("title")) {
            parseElement(docID, tagToHitType("title"), e.text());
        }
        for (Element e : document.select("meta")) {
            parseElement(docID, tagToHitType("meta"), e.attr("content") + " "
                    + e.attr("title"));
        }
        for (Element e : document.select("a[href]")) {
            long lDocID = getDocID(e.attr("abs:href"));
            if (lDocID != -1)
                parseElement(lDocID, tagToHitType("a"),
                        e.text() + " " + e.attr("title"));
        }
        for (Element e : document.select("img[src]")) {
            long iDocID = getDocID(e.attr("abs:src"));
            if (iDocID != -1)
                parseElement(iDocID, tagToHitType("img"), e.attr("alt") + " "
                        + e.attr("title"));
        }
        parseElement(docID, tagToHitType("p"), content);
        calTFValue();
    }
}
