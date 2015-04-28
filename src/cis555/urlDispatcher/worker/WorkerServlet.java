package cis555.urlDispatcher.worker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.urlDispatcher.utils.DispatcherUtils;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

@SuppressWarnings("serial")
public class WorkerServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(WorkerServlet.class);
	private static final String CLASSNAME = WorkerServlet.class.getName();

	private String masterIP;
	private int port;
	private BlockingQueue<URL> newUrlQueue;
	private Set<String> otherWorkerIPs;
	

	/**
	 * Get the address that the master is on
	 * @return
	 */
	protected String getMasterIP(){
		return this.masterIP;
	}

	/**
	 * Get the port number of this worker
	 * @return
	 */
	protected int getPort(){
		return this.port;
	}
	
	/**
	 * Get the number of urls read
	 * @return
	 */
	protected int getUrlsCrawled(){
//		if (this.status == Status.idle){	
//			return 0;
//		} else if (this.status == Status.mapping || this.status == Status.waiting){
//			return this.runMapHandler.getUrlsCrawled();
//		} else {
//			// Reducing
//			return this.runReduceHandler.getUrlsCrawled();
//		}		
		return 1;
	}
	
	public WorkerServlet(){}
	
	@Override
	public void init(){
		
		this.masterIP = getInitParameter(DispatcherConstants.MASTER_KEY_XML);
		String portString = getInitParameter(DispatcherConstants.PORT_KEY_XML);
		
				
		this.port = Integer.parseInt(portString); 
		
		this.newUrlQueue = new ArrayBlockingQueue<URL>(CrawlerConstants.QUEUE_CAPACITY);
		
		this.otherWorkerIPs = new TreeSet<String>();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new PingMasterTask(), 0, DispatcherConstants.TIMER_TASK_FREQUENCY_MS);
	}
	
	
	/***
	 * GET related methods
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			case DispatcherConstants.STOP_URL:
				stopCrawler();
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	  
	}
	
	private void stopCrawler(){
		logger.info(CLASSNAME + " STOP CRALWER!");
	}
	
	
	/***
	 * POST related methods
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String extension = DispatcherUtils.extractExtension(request);
		
		try {
			switch (extension){
			
			case DispatcherConstants.START_URL:
				startCrawler(request);
				break;
				
			case DispatcherConstants.ADD_URLS_URL:	
				addUrls(request);
				break;
			default:
				response.sendError(404);
				break;
				
			}
			
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	}
	
	/**
	 * Deals with request to start the crawler
	 * @param request
	 * @throws MalformedURLException 
	 */
	private void startCrawler(HttpServletRequest request) throws MalformedURLException{
		
		String[] startingUrls = request.getParameter(DispatcherConstants.STARTING_URL_PARAM).split(";");
		String crawlerNumString = request.getParameter(DispatcherConstants.CRAWLER_NUMBER_PARAM);
		int crawlerNum = Integer.parseInt(crawlerNumString);
		
		// TODO - parse out IP addresses of other crawlers
		
		for (String url : startingUrls){
			this.newUrlQueue.add(new URL(url));
			
			logger.info(CLASSNAME + " Added " + url + " for crawler number " + crawlerNum);
		}
	}
	
	/**
	 * Adds urls to file
	 * @param request
	 * @throws MalformedURLException 
	 */
	private void addUrls(HttpServletRequest request) throws MalformedURLException{
		String[] newUrls = request.getParameter(DispatcherConstants.NEW_URLS_PARAM).split(";");
		for (String url : newUrls){
			this.newUrlQueue.add(new URL(url));
			
			logger.info(CLASSNAME + " Added " + url);
		}
	}
	
	
	/**
	 * Private timer task class to ping the master
	 *
	 */
	
	class PingMasterTask extends TimerTask {
		@Override
		public void run(){
			StringBuilder str = new StringBuilder();
			str.append("http://" + getMasterIP() + "/master/" + DispatcherConstants.WORKER_STATUS_URL + "?");
			str.append(DispatcherConstants.PORT_PARAM + "=" + getPort() + "&");
			str.append(DispatcherConstants.PAGES_CRAWLED_PARAM + "=" + getUrlsCrawled() + "&");
			
			try {
				URL url = new URL(str.toString());		
				String content = "";
				DispatcherUtils.sendHttpRequest(url, content, DispatcherUtils.Method.GET, false);
			} catch (Exception e){
				Utils.logStackTrace(e);
			}
			
		}
	}
}
  
