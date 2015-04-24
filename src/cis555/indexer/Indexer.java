package cis555.indexer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

public class Indexer {

    protected static final String DELIMITER = ", \t\n\r\f|:/.![]{}()*^&%~'\\<>?#=+\"";
    protected static final Set<String> STOP_LIST = new HashSet<String>();
    {
        STOP_LIST.add("http");
        STOP_LIST.add("https");
        STOP_LIST.add("com");
        STOP_LIST.add("edu");
        STOP_LIST.add("org");
        STOP_LIST.add("the");
        STOP_LIST.add("a");
    }

    protected long docID;
    protected String URL;
    protected String content;
    protected InputStream is;
    protected Stemmer stemmer;
    protected DBWrapper db;
    protected Map<String, Map<Long, DocHit>> map;

    // @formatter:off
    protected static int tagToHitType(String tag) {
        switch (tag) {
            case "title": return 6;
            case "meta": return 5;
            case "a": return 4;
            case "img": return 3;
            default: return 0;
        }
    }
    // @formatter:on

    protected void parseElement(long docID, int hitType, String text) {
        text = text.trim();
        if (text.length() == 0)
            return;
        StringTokenizer tokenizer = new StringTokenizer(text, DELIMITER);
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            if (hitType == 7) {
                if (STOP_LIST.contains(word))
                    continue;
            }
            String stemWord = getStem(word);

            // word length longer than 30 characters is ignored
            // word length == 1 and if it's not a digit or character is
            // ignored

            if (isBasicLatin(stemWord)
                    && (stemWord.length() <= 30 && stemWord.length() >= 2)
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

    public Indexer(InputStream is, String URL, long docID, DBWrapper db)
            throws Exception {
        this.URL = URL;
        this.docID = docID;
        this.stemmer = new Stemmer();
        this.is = is;
        this.db = db;
        this.map = new HashMap<String, Map<Long, DocHit>>();
    }

    public static Indexer getInstance(InputStream is, String URL, long docID,
            String contentType, DBWrapper db) {
        try {
            if (contentType.equals("html"))
                return new HTMLIndexer(is, URL, docID, db);
            if (contentType.equals("txt"))
                return new TXTIndexer(is, URL, docID, db);
            if (contentType.equals("pdf"))
                return new PDFIndexer(is, URL, docID, db);
            if (contentType.endsWith("xml"))
                return new XMLIndexer(is, URL, docID, db);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getStem(String word) {
        word = word.trim();
        stemmer.add(word.toCharArray(), word.length());
        stemmer.stem();
        return stemmer.toString();
    }

    protected boolean isBasicLatin(String word) {
        for (int i = 0; i < word.length(); i++)
            if (Character.UnicodeBlock.of(word.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN)
                return false;
        return true;
    }

    protected void calTFValue() {
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

    protected long getDocID(String url) {
        return db.getDocId(url);
    }

    public void parse() {
        parseElement(docID, 7, URL);
        parseElement(docID, 0, content);
        calTFValue();
    }

    public String getWordByIndex(int index) {
        int i = 0;
        StringTokenizer tokenizer = new StringTokenizer(content, DELIMITER);
        while (tokenizer.hasMoreTokens() && i++ < index) {
            tokenizer.nextToken();
        }
        if (tokenizer.hasMoreTokens())
            return tokenizer.nextToken();
        return null;
    }

    public Map<String, Map<Long, DocHit>> getMap() {
        return map;
    }

    public void displayResult() {
        for (Entry<String, Map<Long, DocHit>> e1 : map.entrySet()) {
            for (Entry<Long, DocHit> e2 : e1.getValue().entrySet()) {
                System.out.println(e1.getKey() + " : " + e2.getValue());
            }
        }
    }
}
