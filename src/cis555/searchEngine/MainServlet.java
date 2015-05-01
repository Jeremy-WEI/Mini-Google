package cis555.searchEngine;
/**
 * 
 */
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.*;


/**
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException {
        
        StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>")
		.append("<html>")
		
		.append("<head>")
    	.append("<div>");
		
		sb.append("<form action=\"search\" method=\"get\">")
		.append("<input type=\"text\" name=\"query\">")
		.append("</form> ")
		.append("<br>");
		
		load_result(sb);	

		sb.append("</div></body></html>");
		response.setContentType("text/html;charset=UTF-8");
        
		PrintWriter out = response.getWriter();
		out.println(sb);
		
	}
	
	void load_result(StringBuilder sb) {
		ServletContext context = getServletContext();
		
		@SuppressWarnings("unchecked")
		List<String> docList = (List<String>)context.getAttribute("searchResult");
		
		if (docList != null) {
			sb.append("<p><b><font size =\"5\">Search Result</font></b></p>");
			for (String docID: docList) {
				System.out.println("docID" + docID);
				sb.append(String.format("<p>%s</p>", docID));
			}
			
			context.setAttribute("searchResult", null);
		}		
		
	}

}
