package cis555.pingAlexa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
	private MessageDigest digest;
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException{
		PingAlexa alexa = new PingAlexa();	
		alexa.extractDataFromdDatabase();
	}
	
	private PingAlexa() throws NoSuchAlgorithmException{
		digest = MessageDigest.getInstance("MD5");
	}
	
	private void extractDataFromdDatabase(){
		CrawlerDao dao = initialiseDb();
		List<CrawledDocument> crawledDocuments = dao.getAllCrawledDocuments();
		logger.info(CLASSNAME + " expecting " + crawledDocuments.size() + " urls");
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
					rank = (hashUrlToHexStringArray(url) + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				}
				
				String content = new String(response.getResponseBody(), CrawlerConstants.CHARSET);
				String rankString = "<REACH RANK=";
				int index = content.indexOf(rankString);
				if (index < 0){
					rank = (hashUrlToHexStringArray(url) + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				} 
				
				int endIndex = content.indexOf("\"/>", index);
				
				if (endIndex < 0){
					rank = (hashUrlToHexStringArray(url) + "\t" + -1);
					logger.info(rank);
					writer.write(rank + "\n");
					continue;
				}

				String substring = content.substring(index + rankString.length() + 1, endIndex);
				if (substring.matches("\\d+")){
					rank = (hashUrlToHexStringArray(url) + "\t" + Integer.parseInt(substring));
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
	
	
    /**
     * Hashes a url using MD5, and returns a Hex-string representation of the
     * hash
     * 
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    public String hashUrlToHexStringArray(String urlString) {
        byte[] hash = hashUrlToByteArray(urlString);
        return DatatypeConverter.printHexBinary(hash);
    }

    /**
     * Hashes a url using MD5, returns a byte array
     * 
     * @param url
     * @return
     * @throws UnsupportedEncodingException 
     * @throws NoSuchAlgorithmException 
     */
    public byte[] hashUrlToByteArray(String urlString) {
    	try {
        	
            digest.reset();
            digest.update(urlString.getBytes(CrawlerConstants.CHARSET));
            return digest.digest();    		
    	} catch (UnsupportedEncodingException e){
    		Utils.logStackTrace(e);
    		throw new RuntimeException(e);
    	}
    }
	
	
}
