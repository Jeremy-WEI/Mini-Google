package cis555.urlDispatcher.master;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherException;
import cis555.urlDispatcher.utils.DispatcherUtils;

@SuppressWarnings("serial")
public class MasterServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(MasterServlet.class);
	private static final String CLASSNAME = MasterServlet.class.getName();
	
	private Map<String, WorkerDetails> workerDetailMap;
	private boolean isCrawling;
	
	public MasterServlet(){
		this.workerDetailMap = new TreeMap<String, WorkerDetails>(); // NB TreeMap is sorted
		this.isCrawling = false;
	}
	
	
	/***
	 * POST related methods
	 */
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			
			case DispatcherConstants.STATUS_URL:
					
				// Parse the request from the webpage
				handleStatus(request);
				
				// Then show status page
				generateStatus(response.getWriter(), null);
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		}  catch (DispatcherException e){
			generateStatus(response.getWriter(), e.getMessage());

		} catch (Exception e) {
			DispatcherUtils.logStackTrace(e);
		}

	}
	
	/**
	 * Parses out response from user, and sends a message to /runmap
	 * @param request
	 */
	private void handleStatus(HttpServletRequest request){
		
		if (null != request.getParameter(DispatcherConstants.START_URL)){
			startCrawl();
			this.isCrawling = true;
			return;
		} 
		
		if (null != request.getParameter(DispatcherConstants.STOP_URL)){
			if (this.isCrawling){
				stopCrawl();
				this.isCrawling = false;
				return;
			}
		}
	}
	
	/**
	 * Send command to start crawling
	 */
	private void startCrawl(){
		validateStatus();
		int crawlerNumber = 0;
		for (String workerIPAddress : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddress).getIP(); 
			String urlString = "http://" + ipAddress + ":8080/worker/" + DispatcherConstants.START_URL;
			String content = generateStartingUrls(crawlerNumber);
			
			try {
				URL url = new URL(urlString);
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.POST, true);
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new DispatcherException("Unable to convert into url: " + urlString);
			}
			
			crawlerNumber++;
		}
	}
	
	/**
	 * Generates the starting urls for a particular worker. WILL NEED MODIFYING
	 * @param i
	 * @return
	 */
	private String generateStartingUrls(int crawlerNumber){
		StringBuilder str = new StringBuilder();
		str.append(DispatcherConstants.CRAWLER_NUMBER_KEY + "=" + crawlerNumber);
		str.append(DispatcherConstants.STARTING_URL_KEY + "=");
		return str.toString() + "https://www.yahoo.com;http://ga.berkeley.edu/wp-content/uploads/2015/02/pdf-sample.pdf;";
	}
	
	/**
	 * Throws an exception if there are no workers available
	 */
	private void validateStatus(){
		if (this.workerDetailMap.isEmpty()){
			throw new DispatcherException("Cannot operate as no workers are avaialble");
		}
	}
	
	/**
	 * Issue a stop crawl order for all crawlers
	 */
	private void stopCrawl(){
		validateStatus();
		for (String workerIPAddress : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddress).getIP(); 
			String urlString = "http://" + ipAddress + ":8080/worker/" + DispatcherConstants.STOP_URL;
			String content = "";
			
			try {
				URL url = new URL(urlString);
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.GET, true);
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new DispatcherException("Unable to convert into url: " + urlString);
			}
		}

	}
	
	
	/***
	 * GET related methods
	 */
  
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			
			case DispatcherConstants.WORKER_STATUS_URL:
				handleWorkerStatus(request);
				break;
				
			case DispatcherConstants.STATUS_URL:
				generateStatus(response.getWriter(), null);
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		}  catch (Exception e){
			DispatcherUtils.logStackTrace(e);
		}
		
	}
	
	/**
	 * Handles requests for /workerstatus
	 * @param request
	 */
	private void handleWorkerStatus(HttpServletRequest request){
		populateWorkerDetails(request);
	}
	
	/**
	 * Adds a new worker to the worker detail map
	 * @param request
	 */
	private void populateWorkerDetails(HttpServletRequest request){
		
		WorkerDetails workerStatus = new WorkerDetails(request);
		String key = workerStatus.getIP();
		if (key.matches(DispatcherConstants.IP_FORMAT)){
			this.workerDetailMap.put(key, workerStatus);
			logger.debug(CLASSNAME + ": Received update from " + key);
			
		} else {
			logger.debug(CLASSNAME + ": Invald key syntax" + key);
			throw new DispatcherException("Invalid key syntax: " + key);
		}
	}
	
	/**
	 * Generates the status page
	 * @param out
	 */
	private void generateStatus(PrintWriter out, String message){
        out.println("<html><head><title>Status page</title></head>");
        out.println("<body>");
        out.println("<h1>Current status of crawlers</h1>");
        out.println(StatusPageContentGenerator.generateStatusTable(this.workerDetailMap));
        out.println("<br /><br />");
        
        if (null != message){
        	out.println("<h1>Error in starting job</h1>");
            out.println("<br />Error: " + message);
            out.println("<br /><br />");
        }
        
        out.println(StatusPageContentGenerator.generateStartStopButtons(this.isCrawling));        	
        out.println("</h4></body>");
        out.println("</html>");

	}
	
  
}
  
