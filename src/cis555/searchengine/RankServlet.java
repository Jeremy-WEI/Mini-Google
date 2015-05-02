package cis555.searchengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.utils.DocHitEntity;
import cis555.searchengine.utils.QueryTerm;
import cis555.searchengine.utils.SEHelper;
import cis555.searchengine.utils.WeightedDocID;

/**
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class RankServlet extends HttpServlet {

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        ServletContext context = getServletContext();

        Set<QueryTerm> querySet = QueryProcessor.parseQuery(request.getParameter("query"));
        List<WeightedDocID> weightedDocIdList = QueryProcessor.preparePhase(querySet);
        List<WeightedDocID> filteredList = QueryProcessor.filterPhase(weightedDocIdList, 0, 100);
        List<String> outputURL = QueryProcessor.getURLs(filteredList);
        


        context.setAttribute("searchResult", outputURL);
        response.sendRedirect(request.getContextPath());

    }

    public Set<Long> getDocIDsFromWord(String word) {
        Set<Long> docIDList = new HashSet<Long>();
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            docIDList.add((long) r.nextInt(1000));
        }

        return docIDList;

        // TableKeysAndAttributes invertedIndexTable = new
        // TableKeysAndAttributes("inverted-index");
        // invertedIndexTable.addPrimaryKey(new PrimaryKey("word", word));
        // Map<String, TableKeysAndAttributes> requestItems = new
        // HashMap<String, TableKeysAndAttributes>();
        // requestItems.put("inverted-index", invertedIndexTable);
        //
        // try {
        // BatchGetItemOutcome outcome =
        // dynamoDB.batchGetItem(invertedIndexTable);
        // List<Item> items = outcome.getTableItems().get("inverted-index");
        //
        // if (items.size() == 0){
        // // logger.error("No items returned");
        // return null;
        // }
        //
        // Set<Long> docIDList = new HashSet<Long>();
        // for (Item item: items) {
        // docIDList.add(item.getLong("docID"));
        // }
        // return docIDList;
        //
        // } catch (AmazonClientException e){
        // // logger.error(e.getMessage());
        // return null;
        // }
    }

    public Double getPageRankFromDocId(long docID) {
        return 1.0;
        // TableKeysAndAttributes pageRankTable = new
        // TableKeysAndAttributes("pagerank");
        // pageRankTable.addHashOnlyPrimaryKey("docID", docID);
        // Map<String, TableKeysAndAttributes> requestItems = new
        // HashMap<String, TableKeysAndAttributes>();
        // requestItems.put("pagerankx", pageRankTable);
        //
        // try {
        // BatchGetItemOutcome outcome = dynamoDB.batchGetItem(pageRankTable);
        // List<Item> items = outcome.getTableItems().get("pagerank");
        //
        // if (items.size() == 0){
        // // logger.error("No items returned");
        // return null;
        // }
        // // We assume that there is only one item
        //
        // Item item = items.get(0);
        // return item.getDouble("pagerank");
        //
        // } catch (AmazonClientException e){
        // // logger.error(e.getMessage());
        // return null;
        // }
    }
    

    
}