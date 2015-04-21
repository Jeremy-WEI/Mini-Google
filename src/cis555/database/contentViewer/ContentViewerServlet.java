package cis555.database.contentViewer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.database.DBWrapper;

@SuppressWarnings("serial")
public class ContentViewerServlet extends AbstractServlet {

	private static final Logger logger = Logger.getLogger(ContentViewerServlet.class);
	private static final String CLASSNAME = ContentViewerServlet.class.getName();

	public ContentViewerServlet(){}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		setupDatabase();
		
		List<CrawledDocument> crawledDocuments = dao.getAllCrawledDocuments();
		displayAllDocuments(response, crawledDocuments);

	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
	
	/**
	 * Display a list of all of the channels
	 * @param response
	 * @throws IOException
	 */
	private void displayAllDocuments(HttpServletResponse response, List<CrawledDocument> crawledDocuments) throws IOException {
		
		PrintWriter out = response.getWriter();
    	StringBuilder str = new StringBuilder();
        str.append("<html><head><title>Documents</title></head>");
        str.append("<body><h4>");
        
        if (crawledDocuments.size() == 0){
        	str.append("<br />No documents have been crawled yet.<br /><br />");
        	
        } else {
    		str.append("<br /><table border=\"1\"");
    		str.append("<tr>");
    		str.append("<th>ID</th>");
    		str.append("<th>URL</th>");
    		str.append("<th>Content type</th>");
    		str.append("</tr>");
    		
            for (CrawledDocument document : crawledDocuments){
            	str.append("<tr>");
            	str.append("<td width=\"25%\">" + document.getDocID() + "</td>"); 
                str.append("<td width=\"25%\"><a href=\"doc?docID=" + document.getDocID() + "\">" + document.getURL() + "</a></td>");
                str.append("<td width=\"25%\">" + document.getContentType() + "</td>");
            }	
            
        }
        str.append("<br /><br />");
        str.append("</h4></body>");
        str.append("</html>");
        out.println(str.toString());
	}
	
	
	@Override
	public void destroy(){
		DBWrapper.shutdown();
	}
}
