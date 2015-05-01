package cis555.searchengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.utils.DocHitEntity;

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

        String[] words = request.getParameter("query").split("\\s*");
        Set<DocHitEntity> docHitList = new HashSet<DocHitEntity>();
        List<String> urlList = new ArrayList<String>();

        for (int i = 0; i < words.length; i++) {
            docHitList.addAll(DocHitEntityIndexTermDBDAO
                    .getDocHitEntities(words[i]));
        }

        // for (DocHitEntity docHit : docHitList) {
        // System.out.println(db.getUrl(docHit.getDocID()));
        // }
        // Set<DocHitEntity> lst1 = db.getDocHit("comput");
        // Set<DocHitEntity> lst2 = db.getDocHit("scienc");
        // Set<DocHitEntity> lst3 = db.getDocHit("donor");

        for (DocHitEntity docHit : docHitList) {
            urlList.add(DocHitEntityIndexTermDBDAO.getUrl(docHit.getDocID()));
            System.out.println(DocHitEntityIndexTermDBDAO.getUrl(docHit
                    .getDocID()));
        }
        // lst1.retainAll(lst2);
        // System.out.println(lst1);
        // for (DocHitEntity docHit : lst1) {
        // System.out.println(db.getUrl(docHit.getDocID()));
        // }

        context.setAttribute("searchResult", urlList);
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
