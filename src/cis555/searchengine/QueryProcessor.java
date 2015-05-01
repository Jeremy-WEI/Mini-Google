package cis555.searchengine;

import java.util.List;
import java.util.Set;

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
    public static List<WeightedDocID> preparePhase(Set<QueryTerm> queryWords) {
        return null;
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

}
