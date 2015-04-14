package cis555.crawler;

public class CrawlLimitCounter {
	
	private static Integer crawlLimit = Integer.MAX_VALUE;
	
	public static void setCrawlLimit(int crawlLimitNum){
		crawlLimit = crawlLimitNum;
	}
	
	public static int getCounterAndDecrement(){
		synchronized (crawlLimit){
			if (crawlLimit < 0){
				// Don't even bother decrementing
				return -1;
			}
			
			int limit = crawlLimit;
			crawlLimit--;
			return limit;
		}
	}

}
