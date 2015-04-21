package cis555.indexer;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLIndexer extends Indexer {

    private Document document;

    // @formatter:off
	private static int tagToHitType(String tag) {
		switch (tag) {
    		case "title": return 7;
    		case "meta": return 6;
    		case "a": return 5;
    		case "img": return 4;
    		default: return 0;
		}
	}
	// @formatter:on

    public HTMLIndexer(File file, String URL, long docID) throws Exception {
        super(file, URL, docID);
        this.document = Jsoup.parse(fis, Charset.defaultCharset().name(), URL);
    }

    // For testing only
    public HTMLIndexer(String URL, long docID) throws Exception {
        super(new File("test.txt"), URL, docID);
        this.document = Jsoup.connect(URL).get();
    }

    private void parseElement(long docID, String tagName, String text) {
        text = text.trim();
        if (text.length() == 0)
            return;
        int hitType = tagToHitType(tagName);
        StringTokenizer tokenizer = new StringTokenizer(text, DELIMITER);
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            String stemWord = getStem(word.toLowerCase());

            // word length longer than 30 characters is ignored
            // word length == 1 and if it's not a digit or character is ignored

            if ((stemWord.length() <= 30 && stemWord.length() >= 2)
                    || (stemWord.length() == 1 && Character
                            .isLetterOrDigit(stemWord.charAt(0)))) {
                Map<Long, DocHit> hits = map.get(stemWord);
                if (hits == null) {
                    hits = new HashMap<Long, DocHit>();
                    map.put(stemWord, hits);
                }
                DocHit hitLst = hits.get(docID);
                if (hitLst == null) {
                    hitLst = new DocHit(docID);
                    hits.put(docID, hitLst);
                }
                hitLst.addHit(Hit.getHitValue(hitType, index,
                        Character.isUpperCase(word.charAt(0))));
            }
            index++;
        }
    }

    private void calTFValue() {
        int max = 0;
        for (Entry<String, Map<Long, DocHit>> e1 : map.entrySet()) {
            for (Entry<Long, DocHit> e2 : e1.getValue().entrySet()) {
                if (e2.getKey() == docID)
                    max = Math.max(e2.getValue().getFreq(), max);
            }
        }
        for (Entry<String, Map<Long, DocHit>> e1 : map.entrySet()) {
            for (Entry<Long, DocHit> e2 : e1.getValue().entrySet()) {
                if (e2.getKey() == docID)
                    e2.getValue().calTFvalue(max);
            }
        }
    }

    // TODO: need to process URL hit
    public void parse() {
        for (Element e : document.select("title")) {
            parseElement(docID, "title", e.text());
        }
        for (Element e : document.select("meta")) {
            parseElement(docID, "meta",
                    e.attr("content") + " " + e.attr("title"));
        }
        for (Element e : document.select("a[href]")) {
            long lDocID = getDocID(e.attr("abs:href"));
            // TODO: if lDocID not exist?
            parseElement(lDocID, "a", e.text() + " " + e.attr("title"));
        }
        for (Element e : document.select("img[src]")) {
            long iDocID = getDocID(e.attr("abs:src"));
            // TODO: if iDocID not exist?
            parseElement(iDocID, "img", e.attr("alt") + " " + e.attr("title"));
        }
        parseElement(docID, "p", document.body().text());
        calTFValue();
    }

    public static void main(String... args) throws Exception {
        // Document doc =
        // Jsoup.connect("https://en.wikipedia.org/wiki/Main_Page")
        // .get();
        // System.out.println(doc.body().text());
        // Elements es = doc.getAllElements();
        // int i = 0;
        // for (Element e : es) {
        // // if (e.ownText().trim().length() > 0)
        // System.out.println(i + " : " + e.tag() + " : " + e.ownText());
        // i++;
        // }
        // System.out.println();
        // HTMLIndexer indexer = new HTMLIndexer(
        // "https://en.wikipedia.org/wiki/Main_Page", 0);
        Indexer indexer = new HTMLIndexer(new File("wiki.html"),
                "https://en.wikipedia.org/wiki/Main_Page", 0);
        indexer.parse();
        indexer.displayResult();
    }

}
