package cis555.crawler;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
	
public class LinkQueuer implements Runnable {
	
	private static final Logger logger = Logger.getLogger(LinkQueuer.class);
	private static final String CLASSNAME = LinkQueuer.class.getName();

	
	private BlockingQueue<URL> preRedistributionNewURLQueue;
	private BlockingQueue<URL> newUrlQueue;
	private int crawlerID;
	private Map<String, String> otherCrawlerDetails;
	public static boolean active;
	
	public LinkQueuer(BlockingQueue<URL> preRedistributionNewURLQueue, BlockingQueue<URL> newUrlQueue,
			int crawlerID, Map<String, String> otherCrawlerDetails){
		this.preRedistributionNewURLQueue = preRedistributionNewURLQueue;
		this.newUrlQueue = newUrlQueue;
		this.crawlerID = crawlerID;
		this.otherCrawlerDetails = otherCrawlerDetails;
		LinkQueuer.active = true;
	}

	@Override
	public void run() {
		while(LinkQueuer.active){
			try {
				URL url = preRedistributionNewURLQueue.take();
				this.newUrlQueue.add(url);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
