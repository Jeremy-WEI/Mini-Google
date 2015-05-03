package cis555.searchengine;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.utils.QueryTerm;
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
        // ServletContext context = getServletContext();

        String query = request.getParameter("query");
        Set<QueryTerm> terms = QueryProcessor.parseQuery(query);
        List<WeightedDocID> lst1 = QueryProcessor.preparePhase(terms);
        List<WeightedDocID> lst2 = QueryProcessor.posCheckPhase(lst1, terms);
        // List<WeightedDocID> lst2 = lst1;
        // List<WeightedDocID> lst3 = pageRankPhase(lst2);
        List<WeightedDocID> lst3 = lst2;
        List<WeightedDocID> lst4 = QueryProcessor.filterPhase(lst3, 0, 15);
        // List<String> URLs = QueryProcessor.getURLs(lst4);

        PrintWriter pw = response.getWriter();

        pw.write("<!DOCTYPE html><html>");
        pw.write("<head>");
        pw.write("<title>" + "CIS555 Search Engine" + "</title>");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + request.getContextPath() + "/stylesheet/bootstrap.min.css\">");
        pw.write("</head>");

        pw.write("<body>");
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

        pw.write("<div class=\"col-md-6\" style=\"overflow:scroll;height:500px\">");
        pw.write("<div class=\"row\">");
        pw.write("<div class=\"col-md-12\" style=\"height:500px;\">");
        pw.write("<table class=\"table table-striped table-hover table-bordered\">");
        pw.write("<thead>");
        pw.write("<tr>");
        pw.write("<td align=\"center\">Weight</th>");
        pw.write("<td align=\"center\">URL</th>");
        pw.write("</tr>");
        pw.write("</thead>");
        pw.write("<tbody>");
        // for (String URL : URLs) {
        for (WeightedDocID w : lst4) {
            pw.write("<tr>");
            pw.write("<td align=\"center\">"
                    + String.format("%.3f", w.getWeight()) + "</td>");
            String URL = UrlIndexDAO.getUrl(w.getDocID());
            pw.write("<td align=\"center\"><a href=\"" + URL
                    + "\" target=\"iFrame\">" + URL + "</a></td>");
            pw.write("</tr>");
        }
        pw.write("</tbody>");
        pw.write("</table>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");

        pw.write("<div class=\"col-md-6\" style=\"height:500px;\">");
        pw.write("<div class=\"row\">");
        pw.write("<div class=\"col-md-12\" style=\"height:500px;\">");
        // pw.write("<iframe src=\"" + URLs.get(0) + "\"></iframe>");
        pw.write("<iframe name=\"iFrame\""
                + (lst4.size() > 0 ? "src=\""
                        + UrlIndexDAO.getUrl(lst4.get(0).getDocID()) + "\""
                        : "")
                + " style=\"overflow:hidden;overflow-x:hidden;overflow-y:hidden;height:500px;width:100%;position:absolute;top:0px;left:0px;right:0px;bottom:0px\"></iframe>\"");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");

        pw.write("</div>");

        pw.write("</div>");
        pw.write("</body>");
        pw.write("</html>");

    }
}