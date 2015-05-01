package cis555.searchengine.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchEngine {

    private static DBWrapper db;

    public static void setDatabase(DBWrapper db) {
        SearchEngine.db = db;
    }

    /*
     * Search using Boolean Model, binary decision, no tf-idf value is used. For
     * Testing...
     * 
     * @param: query, from user input
     * 
     * @return: List of matching URLs
     */
    public static List<String> booleanSearch(String query) {
        Set<QueryTerm> queryTerms = SEHelper.parseQuery(query);
        if (queryTerms.size() == 0)
            return new LinkedList<String>();
        Iterator<QueryTerm> iter = queryTerms.iterator();
        Set<String> docIDSet = new HashSet<String>();
        QueryTerm queryTerm = iter.next();
        Set<DocHitEntity> docHitSet = db.getDocHit(queryTerm.getWord());
        for (DocHitEntity docHit : docHitSet) {
            docIDSet.add(docHit.getDocID());
        }
        while (iter.hasNext()) {
            queryTerm = iter.next();
            docHitSet = db.getDocHit(queryTerm.getWord());
            Set<String> newDocIDSet = new HashSet<String>();
            for (DocHitEntity docHit : docHitSet) {
                newDocIDSet.add(docHit.getDocID());
            }
            docIDSet.retainAll(newDocIDSet);
        }
        List<String> results = new ArrayList<String>();
        for (String docID : docIDSet) {
            results.add(db.getUrl(docID));
        }
        return results;
        // List<String> newResults = new ArrayList<String>();
        // for (int i = 0; i < Math.min(3, results.size()); i++)
        // newResults.add(results.get(i));
        // return newResults;
    }

    /*
     * Search using Vector Model, just a protoType right now
     * 
     * @param: query, from user input
     * 
     * @return: List of matching WeightedDocID, sorted by weight
     */
    public static List<WeightedDocID> vectorSearch(String query) {
        Set<QueryTerm> queryTerms = SEHelper.parseQuery(query);
        if (queryTerms.size() == 0)
            return new LinkedList<WeightedDocID>();
        Iterator<QueryTerm> iter = queryTerms.iterator();
        QueryTerm queryTerm = iter.next();
        Map<String, WeightedDocID> weightedDocIDMap = new HashMap<String, WeightedDocID>();
        Set<DocHitEntity> docHitSet = db.getDocHit(queryTerm.getWord());
        for (DocHitEntity docHit : docHitSet) {
            WeightedDocID weightedDocID = new WeightedDocID(docHit.getDocID());
            weightedDocID.setWeight(docHit.getTf() * queryTerm.getFreq()
                    * db.getIdfValue(queryTerm.getWord()));
            weightedDocID.addDocHit(docHit);
            weightedDocIDMap.put(docHit.getDocID(), weightedDocID);
        }
        while (iter.hasNext()) {
            queryTerm = iter.next();
            docHitSet = db.getDocHit(queryTerm.getWord());
            for (DocHitEntity docHit : docHitSet) {
                WeightedDocID weightedDocID = weightedDocIDMap.get(docHit
                        .getDocID());
                if (weightedDocID == null) {
                    weightedDocID = new WeightedDocID(docHit.getDocID());
                    weightedDocIDMap.put(docHit.getDocID(), weightedDocID);
                }
                weightedDocID.setWeight(weightedDocID.getWeight()
                        + docHit.getTf() * queryTerm.getFreq()
                        * db.getIdfValue(queryTerm.getWord()));
                weightedDocID.addDocHit(docHit);
            }
        }

        List<WeightedDocID> results = new ArrayList<WeightedDocID>(
                weightedDocIDMap.values());
        Collections.sort(results);
        return results;
        // List<WeightedDocID> newResults = new ArrayList<WeightedDocID>();
        // for (int i = 0; i < Math.min(20, results.size()); i++) {
        // newResults.add(results.get(i));
        // }
        // return newResults;
    }

    public static void main(String... args) {
        // DBWrapper db = new DBWrapper("database");
        // SearchEngine.setDatabase(db);
        // db.start();
        // String[] queries = new String[] {
        // "United_Christian_Broadcasters",
        // "Computer Science developer, hello a i world test wiki 12321 sd132 o98nasd what is ",
        // // "abd asd;wqekl .qwnlcasd.asd;", "computer Science.",
        // // "testing ", "WikiPedia", "Bank of America", "Apigee",
        // "University of Pennsylvania", };
        // for (String query : queries) {
        // System.out.println(query);
        // // System.out.println(SearchEngine.booleanSearch(query));
        // // for (WeightedDocID w : SearchEngine.vectorSearch(query)) {
        // // System.out.println(db.getUrl(w.getDocID()));
        // // }
        // System.out.println(SearchEngine.vectorSearch(query));
        // }
        db.shutdown();
    }
}
