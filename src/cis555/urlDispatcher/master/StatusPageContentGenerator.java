package cis555.urlDispatcher.master;

import java.util.List;
import java.util.Map;

import cis555.urlDispatcher.utils.DispatcherConstants;

public class StatusPageContentGenerator {
	
	/**
	 * Generate the status table
	 * @param details
	 * @return
	 */
	public static String generateStatusTable(Map<String, WorkerDetails> details){
		StringBuilder str = new StringBuilder();
		str.append("<br /><table border=\"1\"");
		str.append("<tr>");
		str.append("<th>IP</th>");
		str.append("<th>Pages Crawled</th>");
		str.append("<th>Last Updated</th>");
		str.append("</tr>");
		
		int totalCrawled = 0;
		
		for (String workerIP: details.keySet()){

			WorkerDetails workerDetails = details.get(workerIP);
			
			str.append("<tr>");
			str.append("<td>" + workerDetails.getIP() + ":" + workerDetails.getPort() + "</td>");
			str.append("<td>" + workerDetails.getPagesCrawled() + "</td>");
			str.append("<td>" + workerDetails.getLastUpdate() + "</td>");
			str.append("</tr>");
			totalCrawled = totalCrawled + workerDetails.getPagesCrawled();
			
		}
		str.append("</tr>");
		str.append("</table>");
		str.append("<br />Total pages crawled so far: " + totalCrawled + "<br />");
		return str.toString();
	}
	
	
	/**
	 * Generates a web form for the Master servlet
	 * @return
	 */
	public static String generateStartStopButtons(boolean isCrawling){
		
		StringBuilder str = new StringBuilder();
        str.append("<br /><br />");
        str.append("<form action = 'status' method = 'post'>");
        if (!isCrawling){
            str.append("<input type='submit' value='Start Crawl' name='" + DispatcherConstants.START_URL + "' size='30' /></form>");        	
        } else {
            str.append("<input type='submit' value='Stop Crawl' name='" + DispatcherConstants.STOP_URL + "' size='30' /></form>");        	
        }
        return str.toString();

	}
	
	/**
	 * Generates the performance table
	 * @param resultsEntry
	 * @return
	 */
	public static String generatePerformanceTable(List<ResultsEntry> resultsEntry){
		StringBuilder str = new StringBuilder();
		str.append("<br /><table border=\"10\"");
		str.append("<tr>");
		str.append("<th>Number of nodes</th>");
		str.append("<th>Number of GETWorkers</th>");
		str.append("<th>Minutes since crawl</th>");
		str.append("<th>Total pages Crawled</th>");
		str.append("</tr>");
		
		for (ResultsEntry entry: resultsEntry){
						
			str.append("<tr>");
			str.append("<td>" + entry.getNumCrawlers() + "</td>");
			str.append("<td>" + entry.getNumGetWorkers() + "</td>");
			str.append("<td>" + entry.getMinutesRan() + "</td>");
			str.append("<td>" + entry.getPagesCrawled() + "</td>");
			str.append("</tr>");
			
		}
		str.append("</tr>");
		str.append("</table>");
		return str.toString();

	}

}
