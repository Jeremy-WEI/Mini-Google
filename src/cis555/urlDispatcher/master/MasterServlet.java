package cis555.urlDispatcher.master;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherException;
import cis555.urlDispatcher.utils.DispatcherUtils;
import cis555.utils.Utils;

@SuppressWarnings("serial")
public class MasterServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(MasterServlet.class);
	private static final String CLASSNAME = MasterServlet.class.getName();
	
	private TreeMap<String, WorkerDetails> workerDetailMap;
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
			Utils.logStackTrace(e);
		}

	}
	
	/**
	 * Parses out response from user, and sends a message to /runmap
	 * @param request
	 * @throws IOException 
	 */
	private void handleStatus(HttpServletRequest request) throws IOException{
		
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
	 * @throws IOException 
	 */
	private void startCrawl() throws IOException{
		validateStatus();
		int crawlerNumber = 0;
		for (String workerIPAddress : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddress).getIP(); 
			int port = this.workerDetailMap.get(workerIPAddress).getPort();
			String content = "";
			String urlString = "http://" + ipAddress + ":" + port + "/worker/" + DispatcherConstants.START_URL + "?" + generateStartContents(crawlerNumber);
			
			try {
				URL url = new URL(urlString);
				logger.debug(CLASSNAME + ": " + url);
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.POST, false);
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new DispatcherException("Unable to convert into url: " + urlString);
			}
			
			crawlerNumber++;
		}
	}
	
	/**
	 * Generates the contents to kickstart the workers
	 * @param i
	 * @return
	 */
	private String generateStartContents(int crawlerNumber){
		StringBuilder str = new StringBuilder();
		str.append(DispatcherConstants.CRAWLER_NAME_PARAM + "=worker" + crawlerNumber + "&");
		str.append(DispatcherConstants.STARTING_URL_PARAM + "=" + generateStartingUrlString(crawlerNumber)+ "&");
		
		Collection<WorkerDetails> workerDetails = this.workerDetailMap.values();
		
		int i = 0;
		for (WorkerDetails workerDetail : workerDetails){
			if (i == 0){
				str.append("worker" + i + "=" + workerDetail.getIP());				
			} else {
				str.append("&worker" + i + "=" + workerDetail.getIP());								
			}
			i++;
		}
		
		return str.toString();
	}
	
	/**
	 * Generates the starting urls for the specified crawler number
	 * @param crawlerNumber
	 * @return
	 */
	private String generateStartingUrlString(int crawlerNumber){
		// WILL NEED MODIFYING
		return "https://www.yahoo.com;http://ga.berkeley.edu/wp-content/uploads/2015/02/pdf-sample.pdf;";
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
	 * @throws IOException 
	 */
	private void stopCrawl() throws IOException{
		validateStatus();
		for (String workerIPAddress : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddress).getIP(); 
			int port = this.workerDetailMap.get(workerIPAddress).getPort();
			String urlString = "http://" + ipAddress + ":" + port + "/worker/" + DispatcherConstants.STOP_URL;
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
		
		logger.info(CLASSNAME + request.getRequestURI());
		
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
			Utils.logStackTrace(e);
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
		String ipAddress = workerStatus.getIP();
		if (ipAddress.matches(DispatcherConstants.IP_FORMAT)){
			this.workerDetailMap.put(ipAddress, workerStatus);
			logger.debug(CLASSNAME + ": Received update from " + ipAddress);
			
		} else {
			logger.debug(CLASSNAME + ": Invald key syntax" + ipAddress);
			throw new DispatcherException("Invalid key syntax: " + ipAddress);
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
  
