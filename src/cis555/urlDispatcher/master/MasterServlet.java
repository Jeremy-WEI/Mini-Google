package cis555.urlDispatcher.master;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
	private List<URL> startingUrls; 
	private int numThreads;
	private Date startTime;
	private List<ResultsEntry> resultsEntries;
	
	public MasterServlet(){
		this.workerDetailMap = new TreeMap<String, WorkerDetails>(); // NB TreeMap is sorted
		this.isCrawling = false;
		this.resultsEntries = new ArrayList<ResultsEntry>();
	}
	
	@Override
	public void init(){
		String[] startingUrlArray = getInitParameter(DispatcherConstants.STARTING_URL_KEY_XML).split(";");
		try {
			populateStartingUrls(startingUrlArray);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.numThreads = Integer.parseInt(getInitParameter(DispatcherConstants.NUM_THREADS_KEY_XML));
	}
	
	/**
	 * Populates the excluded patterns array
	 * @param startingUrlArray
	 * @throws MalformedURLException 
	 */
	private void populateStartingUrls(String[] startingUrlArray) throws MalformedURLException{
		this.startingUrls = new ArrayList<URL>();
		for (String urlString : startingUrlArray){
			URL url = new URL(urlString);
			this.startingUrls.add(url);
		}
	}
	
	/***
	 * POST related methods
	 */
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug(CLASSNAME + " Received request from " + request.getRequestURI());
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
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new PingWorkersTask(), 0, DispatcherConstants.PING_WORKERS_FREQUENCY_MS);
			
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
	public void startCrawl() throws IOException{
		validateStatus();
		int crawlerNumber = 0;
		for (String workerIPAddressAndPort : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddressAndPort).getIP(); 
			int port = this.workerDetailMap.get(workerIPAddressAndPort).getPort();
			String content = "";
			String urlString = "http://" + ipAddress + ":" + port + "/worker/" + DispatcherConstants.START_URL + "?" + generateStartContents(crawlerNumber);
			
			try {
				URL url = new URL(urlString);
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.POST, true);
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new DispatcherException("Unable to convert into url: " + urlString);
			}
			
			crawlerNumber++;
		}
		
		if (!this.isCrawling){			
			this.startTime = new Date();
			resultsEntries.add(new ResultsEntry(null, 0, this.workerDetailMap.size(), this.numThreads));
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new CountPagesCrawledTask(), 0, DispatcherConstants.PING_WORKERS_FREQUENCY_MS);

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
		str.append(DispatcherConstants.NEW_URLS_PARAM + "=" + generateStartingUrlString(crawlerNumber)+ "&");
		str.append(DispatcherConstants.NUM_THREADS_PARAM + "=" + this.numThreads + "&");
		Collection<WorkerDetails> workerDetails = this.workerDetailMap.values();
		
		int i = 0;
		for (WorkerDetails workerDetail : workerDetails){
			if (i == 0){
				str.append("worker" + i + "=" + workerDetail.getIP() + ":" + workerDetail.getPort());				
			} else {
				str.append("&worker" + i + "=" + workerDetail.getIP() + ":" + workerDetail.getPort());								
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
		
		StringBuilder str = new StringBuilder();
		for (URL url : this.startingUrls){
			int bucket = Utils.determineBucketForURL(url, this.workerDetailMap.size());
			if (bucket == crawlerNumber){
				str.append(url.toString() + ";");
			}
		}
		String urls = str.toString();
		if (urls.isEmpty()){
			urls = "http://en.wikipedia.org/wiki/Main_Page";
		}
		return urls;
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
		for (String workerIPAddressAndPort : this.workerDetailMap.keySet()){
			String ipAddress = this.workerDetailMap.get(workerIPAddressAndPort).getIP(); 
			int port = this.workerDetailMap.get(workerIPAddressAndPort).getPort();
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
		String ipAddressAndPort = workerStatus.getIP() + ":" + workerStatus.getPort();
		this.workerDetailMap.put(ipAddressAndPort, workerStatus);
		logger.debug(CLASSNAME + ": Received update from " + ipAddressAndPort);
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
        out.println(StatusPageContentGenerator.generatePerformanceTable(this.resultsEntries));
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
	
	
	/**
	 * Private timer task class to ping all the workers with instructions to start crawl (in case of reset)
	 *
	 */
	
	class PingWorkersTask extends TimerTask {
		@Override
		public void run(){
			
			if (isCrawling){
				logger.info("PINGING WORKER");
				try {
					startCrawl();
				} catch (Exception e){
					Utils.logStackTrace(e);
				}
					
			}
			
		}
	}
	
	
	/**
	 * Private timer task class to ping all the workers with instructions to start crawl (in case of reset)
	 *
	 */
	
	class CountPagesCrawledTask extends TimerTask {
		@Override
		public void run(){
			
			if (isCrawling){
				logger.info("Counting pages crawled");
				try {
					int pagesCrawled = 0;
					for (String workerIP: workerDetailMap.keySet()){
						WorkerDetails workerDetails = workerDetailMap.get(workerIP);
						pagesCrawled = pagesCrawled + workerDetails.getPagesCrawled();
					}
					resultsEntries.add(new ResultsEntry(startTime, pagesCrawled, workerDetailMap.size(), numThreads));
					
				} catch (Exception e){
					Utils.logStackTrace(e);
				}
					
			}
			
		}
	}
  
}
  
