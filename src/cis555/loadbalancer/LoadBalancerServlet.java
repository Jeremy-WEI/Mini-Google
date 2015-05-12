package cis555.loadbalancer;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cis555.searchengine.ServletHelper;

@SuppressWarnings("serial")
public class LoadBalancerServlet extends HttpServlet {
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        PrintWriter pw = response.getWriter();

        ServletHelper.prepareWrite(pw, request.getContextPath(),
                "CIS555 Search Engine", getServletContext());

        pw.write("<div class=\"container\">");

        pw.write("<div style=\"margin: 100px;\">");
        pw.write("<form action=\"/loadbalancer/search\" method=\"GET\" class=\"form-horizontal col-md-12\">");
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
}
