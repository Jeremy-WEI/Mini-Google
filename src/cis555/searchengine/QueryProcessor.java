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

public class QueryProcessor {

    public static void setup(String dbPath) {
        IndexTermDAO.setup(dbPath);
        UrlIndexDAO.setup(dbPath);
    }

    public static Set<QueryTerm> parseQuery(String query) {
        return SEHelper.parseQuery(query);
    }

    // this function is a combination of original phase + tf-if value phase
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
            weightedDocID.setWeight(docHit.getTf() * queryTerm.getFreq()
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
                weightedDocID.setWeight(weightedDocID.getWeight()
                        + docHit.getTf() * queryTerm.getFreq()
                        * IndexTermDAO.getIdfValue(queryTerm.getWord()));
                weightedDocID.addDocHit(docHit);
            }
        }

        List<WeightedDocID> results = new ArrayList<WeightedDocID>(
                weightedDocIDMap.values());
        Collections.sort(results);
        return results;
    }

    public static List<WeightedDocID> pageRankPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    public static List<WeightedDocID> posCheckPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    public static List<WeightedDocID> fancyCheckPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    public static List<WeightedDocID> filterPhase(
            List<WeightedDocID> weightedDocIDList) {
        return null;
    }

    public static void main(String... args) {
        setup("database");
        String[] queries = new String[] {
                "United_Christian_Broadcasters",
                "Computer Science developer, hello a i world test wiki 12321 sd132 o98nasd what is ",
                // "abd asd;wqekl .qwnlcasd.asd;", "computer Science.",
                // "testing ", "WikiPedia", "Bank of America", "Apigee",
                "University of Pennsylvania", };

        for (String query : queries) {
//            System.out.println(query);
//            System.out.println();
            for (WeightedDocID w : preparePhase(parseQuery(query))) {
//                System.out.println(w);
            }
        }
    }

}
