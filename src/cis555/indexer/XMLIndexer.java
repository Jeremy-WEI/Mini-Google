package cis555.indexer;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/*
 * XML handler is internally the same as a TXTIndexer.
 * Some extra work need to be done for better indexing?
 */
public class XMLIndexer extends Indexer {

    private StringTokenizer tokenizer;

    public XMLIndexer(File file, String URL, long docID) throws Exception {
        super(file, URL, docID);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        tokenizer = new StringTokenizer(sb.toString(), DELIMITER);
    }

    public XMLIndexer(InputStream is, String URL, long docID) throws Exception {
        super(is, URL, docID);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = this.is.read()) != -1) {
            sb.append((char) ch);
        }
        tokenizer = new StringTokenizer(sb.toString(), DELIMITER);
    }

    private void calTFValue() {
        int max = 0;
        for (Entry<String, Map<Long, DocHit>> e1 : map.entrySet()) {
            for (Entry<Long, DocHit> e2 : e1.getValue().entrySet()) {
                max = Math.max(e2.getValue().getFreq(), max);
            }
        }
        for (Entry<String, Map<Long, DocHit>> e1 : map.entrySet()) {
            for (Entry<Long, DocHit> e2 : e1.getValue().entrySet()) {
                e2.getValue().calTFvalue(max);
            }
        }
    }

    public void parse() {
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
                hitLst.addHit(Hit.getHitValue(0, index,
                        Character.isUpperCase(word.charAt(0))));
            }
            index++;
        }
        calTFValue();
    }

    public static void main(String... args) throws Exception {
        Indexer indexer = new TXTIndexer(new File("test.xml"), " ", 0);
        indexer.parse();
        indexer.displayResult();
    }

}