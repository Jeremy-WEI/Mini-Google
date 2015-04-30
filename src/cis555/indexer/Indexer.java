package cis555.indexer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cis555.utils.FastTokenizer;
import cis555.utils.Hit;
import cis555.utils.Stemmer;
import cis555.utils.Utils;

public class Indexer {

    // protected static final String DELIMITER =
    // ", \t\n\r\f|:/.![]{}()*^&%~'\\<>?#=+\"";
    protected static final Set<String> URL_STOP_LIST = new HashSet<String>();
    protected static final Set<String> STOP_LIST = new HashSet<String>();
    // protected static Table table;
    static {
        URL_STOP_LIST.add("http");
        URL_STOP_LIST.add("https");
        URL_STOP_LIST.add("edu");
        URL_STOP_LIST.add("org");
        URL_STOP_LIST.add("html");
        URL_STOP_LIST.add("htm");
        URL_STOP_LIST.add("net");
        URL_STOP_LIST.add("int");
        STOP_LIST.add("i");
        STOP_LIST.add("a");
        STOP_LIST.add("about");
        STOP_LIST.add("an");
        STOP_LIST.add("are");
        STOP_LIST.add("as");
        STOP_LIST.add("at");
        STOP_LIST.add("be");
        STOP_LIST.add("by");
        STOP_LIST.add("com");
        STOP_LIST.add("de");
        STOP_LIST.add("en");
        STOP_LIST.add("for");
        STOP_LIST.add("from");
        STOP_LIST.add("how");
        STOP_LIST.add("in");
        STOP_LIST.add("is");
        STOP_LIST.add("it");
        STOP_LIST.add("la");
        STOP_LIST.add("of");
        STOP_LIST.add("on");
        STOP_LIST.add("or");
        STOP_LIST.add("that");
        STOP_LIST.add("the");
        STOP_LIST.add("this");
        STOP_LIST.add("to");
        STOP_LIST.add("was");
        STOP_LIST.add("what");
        STOP_LIST.add("when");
        STOP_LIST.add("where");
        STOP_LIST.add("who");
        STOP_LIST.add("will");
        STOP_LIST.add("with");
        STOP_LIST.add("und");
        STOP_LIST.add("www");
    }

    protected String docID;
    protected String URL;
    protected String content;
    protected InputStream is;
    protected Stemmer stemmer;
    protected Map<String, Map<String, DocHit>> map;

    // @formatter:off
//    protected static int tagToHitType(String tag) {
//        switch (tag) {  
//            case "title": return 6;
//            case "meta": return 5;
//            case "a": return 4;
//            case "img": return 3;
//            default: return 0;
//        }
//    }
    // @formatter:on

    // protected static String trimWord(String word) {
    // int leftIndex = 0, rightIndex = word.length() - 1;
    // for (; leftIndex < word.length(); leftIndex++) {
    // if (Character.isLetterOrDigit(word.charAt(leftIndex)))
    // break;
    // }
    // for (; rightIndex >= leftIndex; rightIndex--) {
    // if (Character.isLetterOrDigit(word.charAt(rightIndex)))
    // break;
    // }
    // if (rightIndex < leftIndex)
    // return "";
    // return word.substring(leftIndex, rightIndex + 1);
    // }

    protected void parseElement(String docID, int hitType, String text) {
        text = text.trim();
        if (text.length() == 0)
            return;
        // StringTokenizer tokenizer = new StringTokenizer(text, DELIMITER);
        // StringTokenizer tokenizer = new StringTokenizer(text);
        FastTokenizer tokenizer = new FastTokenizer(text);
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            if (!isBasicLatin(word)) {
                index++;
                continue;
            }
            // word = trimWord(word);
            if (hitType == 7) {
                if (URL_STOP_LIST.contains(word))
                    continue;
            }

            if (STOP_LIST.contains(word) || word.length() < 1) {
                index++;
                continue;
            }

            String stemWord = getStem(word.toLowerCase());

            /*
             * word length longer than 30 characters is ignored
             */
            if (stemWord.length() <= 30) {
                // if (stemWord.length() <= 30) {
                Map<String, DocHit> hits = map.get(stemWord);
                if (hits == null) {
                    hits = new HashMap<String, DocHit>();
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

    public Indexer(InputStream is, String URL, String docID) throws Exception {
        this.URL = URL;
        this.docID = docID;
        this.stemmer = new Stemmer();
        this.is = is;
        this.map = new HashMap<String, Map<String, DocHit>>();
    }

    public static Indexer getInstance(InputStream is, String URL, String docID,
            String contentType) {
        try {
            if (contentType.equals("html"))
                return new HTMLIndexer(is, URL, docID);
            if (contentType.equals("txt"))
                return new TXTIndexer(is, URL, docID);
            if (contentType.equals("pdf"))
                return new PDFIndexer(is, URL, docID);
            if (contentType.endsWith("xml"))
                return new XMLIndexer(is, URL, docID);
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

    protected static boolean isBasicLatin(String word) {
        for (int i = 0; i < word.length(); i++)
            if (Character.UnicodeBlock.of(word.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN)
                return false;
        return true;
    }

    protected void calTFValue() {
        int max = 0;
        for (Entry<String, Map<String, DocHit>> e1 : map.entrySet()) {
            for (Entry<String, DocHit> e2 : e1.getValue().entrySet()) {
                if (e2.getKey().equals(docID))
                    max = Math.max(e2.getValue().getFreq(), max);
            }
        }
        for (Entry<String, Map<String, DocHit>> e1 : map.entrySet()) {
            for (Entry<String, DocHit> e2 : e1.getValue().entrySet()) {
                if (e2.getKey().equals(docID))
                    e2.getValue().calTFvalue(max);
            }
        }
    }

    protected static String getDocID(String url) {
        return Utils.hashUrlToHexStringArray(url);
    }

    public void parse() {
        parseElement(docID, 7, URL);
        parseElement(docID, 0, content);
        calTFValue();
    }

    public String getWordByIndex(int index) {
        int i = 0;
        // StringTokenizer tokenizer = new StringTokenizer(content, DELIMITER);
        FastTokenizer tokenizer = new FastTokenizer(content);
        while (tokenizer.hasMoreTokens() && i++ < index) {
            tokenizer.nextToken();
        }
        if (tokenizer.hasMoreTokens())
            return tokenizer.nextToken();
        return null;
    }

    public Map<String, Map<String, DocHit>> getMap() {
        return map;
    }

    public void displayResult() {
        for (Entry<String, Map<String, DocHit>> e1 : map.entrySet()) {
            for (Entry<String, DocHit> e2 : e1.getValue().entrySet()) {
                System.out.println(e1.getKey() + " : " + e2.getValue());
            }
        }
    }

    public static void main(String... args) {
        System.out.println(getDocID("https://www.yahoo.com/"));
    }
}
