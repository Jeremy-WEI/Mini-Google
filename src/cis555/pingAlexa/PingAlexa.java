package cis555.pingAlexa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cis555.crawler.CrawlerUtils;
import cis555.crawler.CrawlerUtils.Method;
import cis555.crawler.Response;
import cis555.database.CrawlerDao;
import cis555.utils.CrawledDocument;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;
import cis555.utils.Utils;

import com.sleepycat.persist.EntityStore;


public class PingAlexa {

	private static final Logger logger = Logger.getLogger(PingAlexa.class);
	private static final String CLASSNAME = PingAlexa.class.getName();

	public static void main(String[] args){
		PingAlexa alexa = new PingAlexa();
		alexa.extractDataFromdDatabase();
	}
	
	private void extractDataFromdDatabase(){
		CrawlerDao dao = initialiseDb();
		List<CrawledDocument> crawledDocuments = dao.getAllCrawledDocuments();
		logger.info(CLASSNAME + " extracting urls");
		pingAlexa(crawledDocuments);
	}
	
	/**
	 * Initialise the database
	 */
	private CrawlerDao initialiseDb(){
		EntityStore store = DBWrapper.setupDatabase(CrawlerConstants.DB_DIRECTORY, true);
		return new CrawlerDao(store);
	}
	
	/**
	 * Extract all document meta from the database and save to flat file
	 * @param dao
	 */
	private void pingAlexa(List<CrawledDocument> crawledDocuments){
		
		String fileName = CrawlerConstants.ALEXA_FILENAME;
		String directoryName = CrawlerConstants.DB_DIRECTORY + CrawlerConstants.ALEXA_DIRECTORY;
		Utils.createDirectory(directoryName);
		File urlStorageFile = new File(directoryName + "/" + new Date().getTime() + "_" + fileName);
		BufferedWriter writer = null;
		try {
			if (!urlStorageFile.exists()){
				urlStorageFile.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(urlStorageFile.getAbsoluteFile(), true));
			
			for (CrawledDocument document : crawledDocuments){
				String url = document.getURL();
				String cleansedCode = URLEncoder.encode(url, CrawlerConstants.CHARSET);
				
				String requestUrl = "http://data.alexa.com/data?cli=10&url=" + cleansedCode;
				URL absoluteURL = new URL(requestUrl);
				
				String rank = new String();
				
				Response response = CrawlerUtils.retrieveHttpResource(absoluteURL, Method.GET, "");
				if (null == response){
					rank = (url + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				}
				
				String content = new String(response.getResponseBody(), CrawlerConstants.CHARSET);
				String rankString = "<REACH RANK=";
				int index = content.indexOf(rankString);
				if (index < 0){
					rank = (url + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				} 
				
				int endIndex = content.indexOf("\"/>", index);
				
				if (endIndex < 0){
					rank = (url + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				}

				String substring = content.substring(index + rankString.length() + 1, endIndex);
				if (substring.matches("\\d+")){
					rank = (url + "\t" + Integer.parseInt(substring));
					logger.info(rank);
					writer.write(rank + "\n");					
				} else {
					logger.error("Invalid rank string: " + substring +", skipping");
				}
				Thread.sleep(300);
			}
			
		
			
		} catch (IOException e){
			logger.error(CLASSNAME + ": Unable to store file " + fileName +  ", skipping");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != writer){
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	
}
