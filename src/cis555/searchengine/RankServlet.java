package cis555.searchengine;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.utils.QueryTerm;
import cis555.searchengine.utils.WeightedDocID;
import cis555.utils.FastTokenizer;

/**
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class RankServlet extends HttpServlet {

    private static final String[] PANEL_CLASSES = new String[] {
            "panel-success", "panel-info", "panel-warning", "panel-danger" };

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        // ServletContext context = getServletContext();

        String query = request.getParameter("query");
        Set<QueryTerm> terms = QueryProcessor.parseQuery(query);
        List<WeightedDocID> lst = new ArrayList<WeightedDocID>();
        QueryProcessor.preparePhase(terms, lst);
        QueryProcessor.posCheckPhase(terms, lst);
        // QueryProcessor.pageRankPhase(lst);
        List<WeightedDocID> filteredLst = QueryProcessor
                .filterPhase(lst, 0, 15);
        // List<String> URLs = QueryProcessor.getURLs(lst4);

        PrintWriter pw = response.getWriter();

        ServletHelper.prepareWrite(pw, request.getContextPath(),
                "CIS555 Search Engine", getServletContext());

        pw.write("<div class=\"container\">");

        pw.write("<div style=\"margin:20px 100px 20px 100px;\">");
        pw.write("<form action=\"search\" method=\"GET\" class=\"form-horizontal col-md-12\">");
        pw.write("<div class=\"form-group\">");
        pw.write("<div class=\"row\">");
        pw.write("<div class=\"col-md-10\">");
        pw.write("<input name=\"query\" id=\"query\" type=\"text\" placeholder=\"Search...\" class=\"form-control\" value=\""
                + query + "\">");
        pw.write("</div>");
        pw.write("<div align=\"right\" class=\"col-md-2 form-group\">");
        pw.write("<button type=\"submit\" class=\"btn btn-info btn-block\">Search</button>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</form>");
        pw.write("</div>");

        pw.write("<div class=\"row\" style=\"height:500px;\">");

        pw.write("<div class=\"col-md-6\" style=\"overflow:scroll;height:600px\">");
        ServletHelper.writePanel(pw, "From Wikipedia: " + query,
                ServletHelper.extractInfoFromWiki(query), "panel-primary");
        Set<String> words = getQueryWords(query, terms);
        for (int i = 0; i < filteredLst.size(); i++) {
            WeightedDocID w = filteredLst.get(i);
            String preview = ServletHelper.getPreview(w, words);
            ServletHelper.writePanel(
                    pw,
                    "<a href=\"" + UrlIndexDAO.getUrl(w.getDocID())
                            + "\" target=\"iFrame\">"
                            + UrlIndexDAO.getUrl(w.getDocID()) + "</a>",
                    preview, "Weight: " + String.format("%.3f", w.getWeight()),
                    PANEL_CLASSES[i % 4]);
        }

        pw.write("</div>");

        pw.write("<div class=\"col-md-6\" style=\"height:600px;\">");
        pw.write("<iframe name=\"iFrame\""
                + (filteredLst.size() > 0 ? "src=\""
                        + UrlIndexDAO.getUrl(filteredLst.get(0).getDocID())
                        + "\"" : "")
                + " style=\"overflow:hidden;overflow-x:hidden;overflow-y:hidden;height:600px;width:100%;position:absolute;top:0px;left:0px;right:0px;bottom:0px\"></iframe>\"");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");

        ServletHelper.finishWrite(pw);

    }

    private static Set<String> getQueryWords(String query,
            Set<QueryTerm> queryTerms) {
        Set<String> set = new HashSet<String>();
        FastTokenizer tokenizer = new FastTokenizer(query);
        while (tokenizer.hasMoreTokens()) {
            set.add(tokenizer.nextToken().toLowerCase());
        }
        for (QueryTerm term : queryTerms) {
            set.add(term.getWord());
        }
        // System.out.println(set);
        return set;
    }

}