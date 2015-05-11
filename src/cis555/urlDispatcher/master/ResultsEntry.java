package cis555.urlDispatcher.master;

import java.util.Date;

import cis555.urlDispatcher.utils.DispatcherConstants;

public class ResultsEntry implements Comparable<ResultsEntry> {

	private int minutesRan;
	private int pagesCrawled;
	private int numCrawlers;
	private int numGETWorkers;
	
	public ResultsEntry(Date startTime, int pagesCrawled, int numCrawlers, int numGETWorkers){
		this.pagesCrawled = pagesCrawled;
		if (null == startTime){
			this.minutesRan = 0;
		} else {
			this.minutesRan = (int) (new Date().getTime() - startTime.getTime()) / DispatcherConstants.MILLISECONDS_IN_MINUTE;			
		}
		this.numCrawlers = numCrawlers;
		this.numGETWorkers = numGETWorkers;
 	}

	public int getMinutesRan(){
		return this.minutesRan;
	}

	public int getPagesCrawled(){
		return this.pagesCrawled;
	}
	
	public int getNumCrawlers(){
		return this.numCrawlers;
	}
	
	public int getNumGetWorkers(){
		return this.numGETWorkers;
	}
	
	@Override
	public int compareTo(ResultsEntry o) {
		if (this.minutesRan < o.getMinutesRan()){
			return -1;
		} else if (this.minutesRan > o.getMinutesRan()){
			return 1;
		} else {
			return 0;
		}
	}
	
	
}
