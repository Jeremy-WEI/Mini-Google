package cis555.searchengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cis555.searchengine.utils.DocHitEntity;
import cis555.searchengine.utils.QueryTerm;
import cis555.searchengine.utils.SEHelper;
import cis555.searchengine.utils.WeightedDocID;
import cis555.searchengine.utils.WordWithPosition;
import cis555.utils.Hit;

/**
 * Streamlined version of QueryProcessor
 * 
 * 1. Parse Query
 * 
 * 2. Prepare Phase (get docID + calculate TF-IDF value)
 * 
 * 3. PageRank Phase (combined with PageRank value)
 * 
 * 4. Position Check Phase (solve the cons of bag model)
 * 
 * 5. Fancy Check Phase (add weight by Fancy Hit)
 * 
 * 6. Filter Phase (Sort + get top N result)
 * 
 */
public class QueryProcessor {

    public static void setup(String dbPath) {
        IndexTermDAO.setup(dbPath);
        UrlIndexDAO.setup(dbPath);
    }

    public static Set<QueryTerm> parseQuery(String query) {
        return SEHelper.parseQuery(query);
    }

    /**
     * prepare phase for the query processor
     *
     * 1. Get matching docID
     * 
     * 2. Calculate MD25 value for each matching docID
     * 
     * 3. Attach corresponding DocIdEntity to weightedDocId
     * 
     * @param Set
     *            of queryTerms
     * 
     * @return List of WeightedDocID
     */
    public static List<WeightedDocID> preparePhase(Set<QueryTerm> queryTerms) {
        if (queryTerms.size() == 0)
            return new LinkedList<WeightedDocID>();
        Iterator<QueryTerm> iter = queryTerms.iterator();
        QueryTerm queryTerm = iter.next();
        Map<String, WeightedDocID> weightedDocIDMap = new HashMap<String, WeightedDocID>();
        Set<DocHitEntity> docHitSet = IndexTermDAO.getDocHitEntities(queryTerm
                .getWord());
        for (DocHitEntity docHit : docHitSet) {
            WeightedDocID weightedDocID = new WeightedDocID(docHit.getDocID());
            weightedDocID.addWeight(docHit.getTf() * queryTerm.getFreq()
                    * IndexTermDAO.getIdfValue(queryTerm.getWord()));
            weightedDocID.addDocHit(docHit);
            weightedDocIDMap.put(docHit.getDocID(), weightedDocID);
        }
        while (iter.hasNext()) {
            queryTerm = iter.next();
            docHitSet = IndexTermDAO.getDocHitEntities(queryTerm.getWord());
            for (DocHitEntity docHit : docHitSet) {
                WeightedDocID weightedDocID = weightedDocIDMap.get(docHit
                        .getDocID());
                if (weightedDocID == null) {
                    weightedDocID = new WeightedDocID(docHit.getDocID());
                    weightedDocIDMap.put(docHit.getDocID(), weightedDocID);
                }
                weightedDocID.addWeight(docHit.getTf() * queryTerm.getFreq()
                        * IndexTermDAO.getIdfValue(queryTerm.getWord()));
                weightedDocID.addDocHit(docHit);
            }
        }
        List<WeightedDocID> results = new ArrayList<WeightedDocID>(
                weightedDocIDMap.values());
        return results;
    }

    public static List<WeightedDocID> pageRankPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    public static List<WeightedDocID> posCheckPhase(
            List<WeightedDocID> weightedDocIDList, Set<QueryTerm> queryTerms) {
        Map<String, Integer> initialWordCount = new HashMap<String, Integer>();
        int tolerance = 0;
        int noOfWords = 0;
        for (QueryTerm term : queryTerms) {
            initialWordCount.put(term.getWord(), term.getFreq());
            tolerance = Math.max(2 * tolerance,
                    Collections.max(term.getPositions()) + 1);
            noOfWords += term.getFreq();
        }
        for (WeightedDocID w : weightedDocIDList) {
            Map<String, Integer> remainingWordCount = new HashMap<String, Integer>();
            remainingWordCount.putAll(initialWordCount);
            LinkedList<WordWithPosition> wordStateMachine = new LinkedList<WordWithPosition>();
            LinkedList<WordWithPosition> wordsSequence = new LinkedList<WordWithPosition>();
            for (DocHitEntity docHit : w.getDocHits()) {
                for (int hit : docHit.getPlainHitLst()) {
                    wordsSequence.add(new WordWithPosition(docHit.getWord(),
                            Hit.getHitPos(hit)));
                }
            }
            Collections.sort(wordsSequence);
            Iterator<WordWithPosition> iter = wordsSequence.iterator();
            int position = 0;
            int hitNumber = 0;
            while (iter.hasNext()) {
                WordWithPosition curWord = iter.next();
                position = curWord.getPos();
                wordStateMachine.add(curWord);
                remainingWordCount.put(curWord.getWord(),
                        remainingWordCount.get(curWord.getWord()) - 1);
                while (true) {
                    WordWithPosition tmp = wordStateMachine.peek();
                    if (tmp != null && tmp.getPos() < position - tolerance) {
                        wordStateMachine.remove();
                        remainingWordCount.put(tmp.getWord(),
                                remainingWordCount.get(tmp.getWord()) + 1);
                        continue;
                    }
                    break;
                }
                int remainingHit = 0;
                for (int x : remainingWordCount.values()) {
                    remainingHit += Math.max(0, x);
                }
                hitNumber = Math.max(noOfWords - remainingHit, hitNumber);
            }
            // System.out.println("-------- I am the line separator --------");
            // TODO: adjust the weight???
            // TODO: mutiple hit??
            w.mutiplyWeight(hitNumber / (double) noOfWords);
        }
        return weightedDocIDList;
    }

    public static List<WeightedDocID> fancyCheckPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    /**
     * filtering phase --> sort + top N (offset-based)
     * 
     * @param: weightDocIDList, the original list
     * @param: startOffset
     * @param: N, how many results you want
     * @return filtered weightedDocIDList
     */
    public static List<WeightedDocID> filterPhase(
            List<WeightedDocID> weightedDocIDList, int startOffset, int N) {
        Collections.sort(weightedDocIDList);
        List<WeightedDocID> newList = new ArrayList<WeightedDocID>(N);
        Iterator<WeightedDocID> iter = weightedDocIDList.iterator();
        int startIndex = startOffset;
        int endIndex = Math.min(N + startOffset - 1,
                weightedDocIDList.size() - 1);
        int index = 0;
        while (iter.hasNext()) {
            if (index < startIndex) {
                iter.next();
                index++;
            } else if (index <= endIndex) {
                newList.add(iter.next());
                index++;
            } else
                break;
        }
        return newList;
    }

    /**
     * @param weightDocIDList
     * @return URL List
     */
    public static List<String> getURLs(List<WeightedDocID> weightedDocIDList) {
        List<String> results = new ArrayList<String>(weightedDocIDList.size());
        for (WeightedDocID w : weightedDocIDList) {
            results.add(UrlIndexDAO.getUrl(w.getDocID()));
        }
        return results;
    }

    public static void printInfo(List<WeightedDocID> weightedDocIDList) {
        for (WeightedDocID w : weightedDocIDList) {
            System.out.println("Weight: " + w.getWeight() + " , "
                    + UrlIndexDAO.getUrl(w.getDocID()));
        }
    }

    public static void main(String... args) {
        setup("database");
        String[] queries = new String[] {

        // "United_Christian_Broadcasters",
        //
        // "Computer Science developer, hello a i world test wiki 12321 sd132 o98nasd what is ",

        "Hello World",

        // "WikiPedia",
        //
        // "Bank of America",
        //
        // "Apigee",
        //
        // "University of Pennsylvania",
        //
        // "Yunchen Wei"

        };
        System.out.println("-------- I am the line separator --------");
        for (String query : queries) {
            System.out.println("Query: " + query);
            // System.out.println();
            Set<QueryTerm> terms = parseQuery(query);
            List<WeightedDocID> lst1 = preparePhase(terms);
            List<WeightedDocID> lst2 = posCheckPhase(lst1, terms);
            // List<WeightedDocID> lst2 = lst1;
            List<WeightedDocID> lst3 = filterPhase(lst2, 0, 5);
            // List<String> URLs = getURLs(lst3);
            // for (String URL : URLs) {
            // System.out.println(URL);
            // }
            printInfo(lst3);
            System.out.println("-------- I am the line separator --------");
        }
    }
}
