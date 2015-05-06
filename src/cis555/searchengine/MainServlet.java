package cis555.searchengine;

/**
 * 
 */
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletContext;
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

        ServletHelper.prepareWrite(pw, request.getContextPath(),
                "CIS555 Search Engine", getServletContext());

        pw.write("<div class=\"container\">");

        pw.write("<div style=\"margin: 100px;\">");
        pw.write("<form action=\"/searchengine/search\" method=\"GET\" class=\"form-horizontal col-md-12\">");
        pw.write("<div class=\"form-group\">");
        pw.write("<div class=\"row\">");
        pw.write("<div class=\"col-md-10\">");
        pw.write("<input name=\"query\" id=\"query\" type=\"text\" placeholder=\"Search...\" class=\"form-control\">");
        pw.write("</div>");
        pw.write("<div align=\"right\" class=\"col-md-2 form-group\">");
        pw.write("<button type=\"submit\" class=\"btn btn-info btn-block\">Search</button>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</form>");
        pw.write("</div>");

        pw.write("</div>");

        ServletHelper.finishWrite(pw);
    }

    void loadResult(StringBuilder sb) {
        ServletContext context = getServletContext();

        @SuppressWarnings("unchecked")
        List<String> docList = (List<String>) context
                .getAttribute("searchResult");

        if (docList != null) {
            sb.append("<p><b><font size =\"5\">Search Result</font></b></p>")
                    .append("<ul style=\"list-style-type:disc\">");

            for (String url : docList) {
                sb.append(
                        String.format("<li><a href=\"%s\">%s</a></li>", url,
                                url)).append("<br />");
            }

            sb.append("</ul>");
            context.setAttribute("searchResult", null);
        }

    }

}
