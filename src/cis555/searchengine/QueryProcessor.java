package cis555.searchengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
 * 3. Fancy Check Phase (add weight by Fancy Hit)
 * 
 * 4. Position Check Phase (solve the cons of bag model)
 * 
 * 5. PageRank Phase (combined with PageRank value)
 * 
 * 6. Filter Phase (Sort + get top N result)
 * 
 */
public class QueryProcessor {

    // public static void setup(String dbPath) {
    // IndexTermDAO.setup(dbPath);
    // UrlIndexDAO.setup(dbPath);
    // PagerankDAO.setup(dbPath);
    // }

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
     * // * 4. Remove the later half (To speed up search process)
     * 
     * @param Set
     *            of queryTerms
     * @param List
     *            <WeightedDocID>
     */
    public static void preparePhase(Set<QueryTerm> queryTerms,
            List<WeightedDocID> weightedDocIDList) {
        if (queryTerms.size() == 0)
            return;
        for (QueryTerm term : queryTerms) {
            term.setIdfValue(IndexTermDAO.getIdfValue(term.getWord()));
        }
        Iterator<QueryTerm> iter = queryTerms.iterator();
        QueryTerm queryTerm = iter.next();
        Map<String, WeightedDocID> weightedDocIDMap = new HashMap<String, WeightedDocID>();
        Set<DocHitEntity> docHitSet = IndexTermDAO.getDocHitEntities(queryTerm
                .getWord());
        int freq = queryTerm.getFreq();
        double idfValue = queryTerm.getIdfValue();
        for (DocHitEntity docHit : docHitSet) {
            WeightedDocID weightedDocID = new WeightedDocID(docHit.getDocID());
            weightedDocID.addPlainWeight(docHit.getTf() * freq * idfValue);
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
                weightedDocID.addPlainWeight(docHit.getTf() * freq * idfValue);
                weightedDocID.addDocHit(docHit);
            }
        }
        weightedDocIDList.addAll(weightedDocIDMap.values());
        // Collections.sort(weightedDocIDList);
        // for (int i = weightedDocIDList.size() - 1; i >= 100; i--) {
        // weightedDocIDList.remove(i);
        // }
    }

    /**
     * position checking phase for the query processor
     * 
     * will modify the pass-in list
     * 
     * @param List
     *            <WeightedDocID>
     * 
     */
    public static void posCheckPhase(Set<QueryTerm> queryTerms,
            List<WeightedDocID> weightedDocIDList) {
        Map<String, Integer> initialWordCount = new HashMap<String, Integer>();
        int tolerance = 0;
        int noOfWords = 0;
        for (QueryTerm term : queryTerms) {
            initialWordCount.put(term.getWord(), term.getFreq());
            tolerance = Math.max(tolerance,
                    2 * Collections.max(term.getPositions()) + 1);
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
            int wordNumber = w.getDocHits().get(0).getWordCount() - 1;
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
                if (noOfWords - remainingHit > hitNumber) {
                    w.setPreviewStartPos(Math.max(0, wordStateMachine.get(0)
                            .getPos() - 10));
                    w.setPreviewEndPos(Math.min(wordNumber, position + 10));
                }
                hitNumber = Math.max(noOfWords - remainingHit, hitNumber);
            }
            int start = w.getPreviewStartPos();
            int end = w.getPreviewEndPos();
            if (end - start <= 40) {
                int dist = end - start;
                int offset = (40 - dist) / 2;
                w.setPreviewStartPos(Math.max(0, start - offset));
                w.setPreviewEndPos(Math.min(wordNumber, end + offset));
            }
            // System.out.println("-------- I am the line separator --------");
            // TODO: adjust the weight???
            // TODO: mutiple hit??
            w.multiplyPlainWeight(hitNumber / (double) noOfWords);
        }
    }

    /**
     * pagerank phase for the query processor
     * 
     * will modify the pass-in list
     * 
     * @param List
     *            <WeightedDocID>
     * 
     */
    public static void pageRankPhase(List<WeightedDocID> weightedDocIDList) {
        for (WeightedDocID w : weightedDocIDList) {
            double value = PagerankDAO.getPagerankValue(w.getDocID()) + 1;
            w.multiplyWeight(value);
            value = AlexaDAO.getAlexaRank(w.getDocID());
            if (value > 0) {
                w.multiplyWeight(1 + Math.pow(1 / value, 0.1));
            }
        }
    }

    /**
     * fancy hit checking phase for the query processor
     * 
     * add weight based on fancy hit
     * 
     * will modify the pass-in list
     * 
     * @param List
     *            <WeightedDocID>
     * 
     */
    public static void fancyCheckPhase(Set<QueryTerm> queryTerms,
            List<WeightedDocID> weightedDocIDList) {
        int wordNo = queryTerms.size();
        for (WeightedDocID w : weightedDocIDList) {
            double titleValue = 0;
            double urlValue = 0;
            double metaValue = 0;
            double hrefAltValue = 0;
            Set<String> titleWords = new HashSet<String>();
            Set<String> urlWords = new HashSet<String>();
            Set<String> metaWords = new HashSet<String>();
            Set<String> hrefAltWords = new HashSet<String>();
            Map<String, Double> map = new HashMap<String, Double>();
            for (QueryTerm term : queryTerms) {
                map.put(term.getWord(), term.getIdfValue());
            }
            for (DocHitEntity docHit : w.getDocHits()) {
                String word = docHit.getWord();
                if (map.get(word) == null)
                    continue;
                for (int hit : docHit.getFancyHitLst()) {
                    int hitType = Hit.getHitType(hit);
                    double value = map.get(word);
                    if (hitType == 7 && !urlWords.contains(word)) {
                        urlWords.add(word);
                        urlValue += value;
                    }
                    if (hitType == 6 && !titleWords.contains(word)) {
                        titleWords.add(word);
                        titleValue += value;
                    }
                    if (hitType == 5 && !metaWords.contains(word)) {
                        metaWords.add(word);
                        metaValue += value;
                    }
                    if ((hitType == 3 || hitType == 4)
                            && !hrefAltWords.contains(word)) {
                        hrefAltWords.add(word);
                        hrefAltValue += value;
                    }
                }
            }
            w.addFancyWeight((0.8 * urlValue * urlWords.size() + 0.8
                    * titleValue + titleWords.size() + 0.8 * metaValue
                    * metaWords.size() + 0.5 * hrefAltValue
                    * hrefAltWords.size())
                    / wordNo);
        }
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

}
