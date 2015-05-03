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

        pw.write("<!DOCTYPE html><html>");
        pw.write("<head>");
        pw.write("<title>" + "CIS555 Search Engine" + "</title>");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + request.getContextPath() + "/stylesheet/bootstrap.min.css\">");
        pw.write("</head>");

        pw.write("<body>");
        pw.write("<div class=\"container\">");

        pw.write("<div style=\"margin: 100px;\">");
        pw.write("<form action=\"search\" method=\"GET\" class=\"form-horizontal col-md-12\">");
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
        pw.write("</body>");
        pw.write("</html>");
        // StringBuilder sb = new StringBuilder();
        // sb.append("<!DOCTYPE html>").append("<html>")
        //
        // .append("<head>").append("<div>");
        //
        // sb.append("<form action=\"search\" method=\"get\">")
        // .append("<input type=\"text\" name=\"query\">")
        // .append("</form> ").append("<br>");
        //
        // loadResult(sb);
        //
        // sb.append("</div></body></html>");
        // response.setContentType("text/html;charset=UTF-8");
        //
        // PrintWriter out = response.getWriter();
        // out.println(sb);

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
