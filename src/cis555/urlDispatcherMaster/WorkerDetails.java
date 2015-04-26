package cis555.urlDispatcherMaster;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class WorkerDetails {
	
	private int pagesCrawled;
	private Date lastUpdate;
	private String ip;
	
	public WorkerDetails(HttpServletRequest request) {

		String pagesCrawledString = request.getParameter("pagesCrawled");
		this.pagesCrawled = Integer.parseInt(pagesCrawledString);
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

}
