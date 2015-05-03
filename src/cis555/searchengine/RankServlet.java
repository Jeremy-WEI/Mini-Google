package cis555.searchengine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.utils.QueryTerm;
import cis555.searchengine.utils.WeightedDocID;

import com.amazonaws.util.json.JSONObject;

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
        QueryProcessor.pageRankPhase(lst);
        List<WeightedDocID> filteredLst = QueryProcessor
                .filterPhase(lst, 0, 15);
        // List<String> URLs = QueryProcessor.getURLs(lst4);

        PrintWriter pw = response.getWriter();

        ServletHelper.prepareWrite(pw, request.getContextPath(),
                "CIS555 Search Engine");

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
                extractInfoFromWiki(query), "panel-primary");
        for (int i = 0; i < filteredLst.size(); i++) {
            WeightedDocID w = filteredLst.get(i);
            ServletHelper.writePanel(
                    pw,
                    "<a href=\"" + UrlIndexDAO.getUrl(w.getDocID())
                            + "\" target=\"iFrame\">"
                            + UrlIndexDAO.getUrl(w.getDocID()) + "</a>",
                    "StartIndex: " + w.getPreviewStartPos() + ", EndIndex: "
                            + w.getPreviewEndPos(),
                    "Weight: " + String.format("%.3f", w.getWeight()),
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

    private static String extractInfoFromWiki(String query) {
        @SuppressWarnings("deprecation")
        String url = "http://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles="
                + URLEncoder.encode(query) + "&format=json&exintro=1";
        System.out.println(url);
        URLConnection conn;
        try {
            conn = new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            JSONObject json = (JSONObject) ((JSONObject) new JSONObject(
                    sb.toString()).get("query")).get("pages");
            @SuppressWarnings("unchecked")
            Iterator<String> iter = json.keys();
            String key = null;
            while (iter.hasNext()) {
                key = iter.next();
                break;
            }
            return ((JSONObject) json.get(key)).getString("extract");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}