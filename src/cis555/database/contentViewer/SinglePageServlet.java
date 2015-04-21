package cis555.database.contentViewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.aws.utils.CrawledDocument;
import cis555.utils.CrawlerConstants;

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
		
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println(getContentsFromFile(docID));
		
	}
	
	/**
	 * Retrieve the contents of a file by its doc id
	 * @param docID
	 * @return
	 */
	private String getContentsFromFile(long docID){
		String filename = CrawlerConstants.SERVLET_CRAWLED_FILES_DIR + "/" + Long.toString(docID) + ".txt";
		File file = new File(filename);
		if (!file.exists()){
			String error = "File doesn't exist " + filename;
			logger.error(CLASSNAME + error);
			return error;
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = new String();
				StringBuffer str = new StringBuffer();
				while ((line = reader.readLine()) != null){
					str.append(line);
				}
				return str.toString();
			} catch (IOException e){
				String error = "IO error while opening " + filename;
				logger.error(CLASSNAME + error);
				return "";				
			} finally {
				if (null != reader){
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "";
					}
				}
			}
		}
	}
	
	
}
