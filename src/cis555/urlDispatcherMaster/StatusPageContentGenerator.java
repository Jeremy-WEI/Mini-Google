package cis555.urlDispatcherMaster;

import java.util.Map;

import cis555.utils.DispatcherConstants;

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
		for (String workerIP: details.keySet()){

			WorkerDetails workerDetails = details.get(workerIP);
			
			str.append("<tr>");
			str.append("<td>" + workerDetails.getIP() + "</td>");
			str.append("<td>" + workerDetails.getPagesCrawled() + "</td>");
			str.append("<td>" + workerDetails.getLastUpdate() + "</td>");
			str.append("</tr>");
			
		}
		str.append("</tr>");
		str.append("</table>");
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

}
