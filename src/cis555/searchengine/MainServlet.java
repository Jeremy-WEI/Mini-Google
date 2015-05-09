package cis555.searchengine;

/**
 * 
 */
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        PrintWriter pw = response.getWriter();

        ServletHelper.prepareWrite(pw, request.getContextPath(), "Mini-Search",
                getServletContext());

        pw.write("<div id=\"headerwrap\">");
        pw.write("<br>");
        pw.write("<br>");
        pw.write("<br>");
        pw.write("<br>");
        pw.write("<br>");
        pw.write("<div class=\"container\">");
        pw.write("<div class=\"row\">");
        pw.write("<div class=\"col-lg-8\">");
        pw.write("<h1><b>Mini-Search</b> --> Cis555 Project</h1>");
        pw.write("<div class=\"row\">");
        pw.write("<form action=\"/searchengine/search\" method=\"GET\" class=\"form-horizontal col-lg-12\">");
        pw.write("<div class=\"form-group row\">");
        pw.write("<div class=\"col-lg-9\">");
        pw.write("<input name=\"query\" id=\"query\" type=\"text\" class=\"form-control\" placeholder=\"Type your search here...\" style=\"width:100%\">");
        pw.write("</div>");
        pw.write("<div class=\"col-lg-3\">");
        pw.write("<button type=\"submit\" class=\"btn btn-info btn-block\">Search</button>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</form>");    
        pw.write("</div>");
        pw.write("</div>");
        pw.write("<div class=\"col-lg-4\">");
        pw.write("<img class=\"img-responsive\" src=\""
                + request.getContextPath() + "/image/ipad-hand.png\" alt=\"\">");
        pw.write("</div>");

        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");
        // pw.write("<div class=\"container\">");
        //
        // pw.write("<div style=\"margin: 100px;\">");
        // pw.write("<form action=\"/searchengine/search\" method=\"GET\" class=\"form-horizontal col-md-12\">");
        // pw.write("<div class=\"form-group\">");
        // pw.write("<div class=\"row\">");
        // pw.write("<div class=\"col-md-10\">");
        // pw.write("<input name=\"query\" id=\"query\" type=\"text\" placeholder=\"Search...\" class=\"form-control\">");
        // pw.write("</div>");
        // pw.write("<div align=\"right\" class=\"col-md-2 form-group\">");
        // pw.write("<button type=\"submit\" class=\"btn btn-info btn-block\">Search</button>");
        // pw.write("</div>");
        // pw.write("</div>");
        // pw.write("</div>");
        // pw.write("</form>");
        // pw.write("</div>");

        pw.write("</div>");

        ServletHelper.finishWrite(pw);
    }

    // void loadResult(StringBuilder sb) {
    // ServletContext context = getServletContext();
    //
    // @SuppressWarnings("unchecked")
    // List<String> docList = (List<String>) context
    // .getAttribute("searchResult");
    //
    // if (docList != null) {
    // sb.append("<p><b><font size =\"5\">Search Result</font></b></p>")
    // .append("<ul style=\"list-style-type:disc\">");
    //
    // for (String url : docList) {
    // sb.append(
    // String.format("<li><a href=\"%s\">%s</a></li>", url,
    // url)).append("<br />");
    // }
    //
    // sb.append("</ul>");
    // context.setAttribute("searchResult", null);
    // }
    //
    // }

}
