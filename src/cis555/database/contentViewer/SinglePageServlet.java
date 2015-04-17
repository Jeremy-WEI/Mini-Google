package cis555.database.contentViewer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.database.CrawledDocument;

@SuppressWarnings("serial")
public class SinglePageServlet extends AbstractServlet {
	private static final Logger logger = Logger.getLogger(SinglePageServlet.class);
	private static final String CLASSNAME = SinglePageServlet.class.getName();

	public SinglePageServlet(){}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		setupDatabase();
		
		String docNumberString = request.getParameter("docID");
		if (null == docNumberString || docNumberString.isEmpty()){
			response.sendError(404);
		}
		
		Long docID = Long.parseLong(docNumberString);

		generateOutput(response, docID, request);
		
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
	
	/**
	 * Generate an xml response listing all the contents of the channel
	 * @param response
	 * @throws IOException 
	 */
	private void generateOutput(HttpServletResponse response, long docID, HttpServletRequest request) throws IOException{
		
		
		CrawledDocument document = dao.getCrawledDocumentByID(docID);
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println(document.getContents());
		
	}
	
	
}
