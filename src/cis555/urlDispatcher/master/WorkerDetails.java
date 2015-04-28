package cis555.urlDispatcher.master;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import cis555.urlDispatcher.utils.DispatcherConstants;

public class WorkerDetails {
	
	private int pagesCrawled;
	private Date lastUpdate;
	private String ip;
	private int port;
	
	public WorkerDetails(HttpServletRequest request) {
		String pagesCrawledString = request.getParameter(DispatcherConstants.PAGES_CRAWLED_PARAM);
		this.pagesCrawled = Integer.parseInt(pagesCrawledString);
		String portString = request.getParameter(DispatcherConstants.PORT_PARAM);
		this.port = Integer.parseInt(portString);
		this.ip = request.getRemoteAddr();
		this.lastUpdate = new Date();
	}
	
	
	public String getIP(){
		return this.ip;
	}

	public int getPagesCrawled() {
		return this.pagesCrawled;
	}

	public Date getLastUpdate(){
		return this.lastUpdate;
	}
	
	public int getPort(){
		return this.port;
	}

}
