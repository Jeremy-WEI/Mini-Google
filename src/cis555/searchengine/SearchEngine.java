package cis555.searchengine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SearchEngine {

    private static DBWrapper db;

    public static void setDatabase(DBWrapper db) {
        SearchEngine.db = db;
    }

    /*
     * Search using Boolean Model, binary decision, no tf-idf value used
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
        Set<DocHitEntity> set = db.getDocHit(iter.next().getWord());
        for (QueryTerm queryTerm : queryTerms) {
            Set<DocHitEntity> newSet = db.getDocHit(queryTerm.getWord());
            set.retainAll(newSet);
        }
        List<String> results = new LinkedList<String>();
        for (DocHitEntity docHit : set) {
            results.add(db.getUrl(docHit.getDocID()));
        }
        return results;
    }

    public static void main(String... args) {
        DBWrapper db = new DBWrapper("database");
        SearchEngine.setDatabase(db);
        db.start();
        // SearchEngine se = new SearchEngine(db);
        String[] queries = new String[] {
                "Computer Science developer, hello a i world test wiki",
                "abd asd;wqekl .qwnlcasd.asd;", "computer Science.",
                "testing ", "WikiPedia", "Bank of America", "Apigee",
                "University of Pennsylvania", "UCB", "....a" };
        for (String query : queries) {
            System.out.println(SearchEngine.booleanSearch(query));
        }
        db.shutdown();
    }
}
