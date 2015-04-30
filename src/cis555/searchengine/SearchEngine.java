package cis555.searchengine;

import java.util.HashSet;
import java.util.Set;

import cis555.indexer.FastTokenizer;
import cis555.utils.Stemmer;

public class SearchEngine {

    private DBWrapper db;
    private static Set<String> STOP_LIST = new HashSet<String>();
    static {
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

    public SearchEngine(DBWrapper db) {
        this.db = db;
    }

    /*
     * word should be explicitly processed by FastTokenizer. If word contains
     * non-ASCII character, returns null.
     * 
     * (We only indexed word contains ASCII characters)
     */
    public static String getStem(String word) {
        Stemmer stemmer = new Stemmer();
        word = word.trim().toLowerCase();
        if (STOP_LIST.contains(word) || !isBasicLatin(word)
                || word.length() < 1)
            return null;
        stemmer.add(word.toCharArray(), word.length());
        stemmer.stem();
        return stemmer.toString();
    }

    public static boolean isBasicLatin(String word) {
        for (int i = 0; i < word.length(); i++)
            if (Character.UnicodeBlock.of(word.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN)
                return false;
        return true;
    }

    public static void main(String... args) {
        DBWrapper db = new DBWrapper("database");
        db.start();
        // SearchEngine se = new SearchEngine(db);
        String[] queries = new String[] {
                "Computer Science developer, hello a i world test wiki",
                "abd asd;wqekl .qwnlcasd.asd;", "computer Science." };
        for (int i = 0; i < queries.length; i++) {
            FastTokenizer tokenizer = new FastTokenizer(queries[i]);
            Set<DocHitEntity> set = null;
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();
                word = getStem(word);
                if (word != null) {
                    Set<DocHitEntity> newSet = db.getDocHit(word);
                    // System.out.println(newSet);
                    if (set == null) {
                        set = newSet;
                    } else {
                        set.retainAll(newSet);
                    }
                }
            }
            System.out.println("Query is: " + queries[i] + "\nHit Size: "
                    + set.size());
            // for (DocHitEntity docHit : set) {
            // System.out.println(db.getUrl(docHit.getDocID()));
            // }
        }
        db.shutdown();
    }
}
