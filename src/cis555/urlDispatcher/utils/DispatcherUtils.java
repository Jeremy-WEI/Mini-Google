package cis555.urlDispatcher.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class DispatcherUtils {

	private static final Logger logger = Logger.getLogger(DispatcherUtils.class);
	private static final String CLASSNAME = DispatcherUtils.class.getName();
	
	
	public enum Method {
		GET, POST
	}
	
	/**
	 * Send an http request
	 * @param url
	 * @param port
	 * @param content
	 * @param method
	 * @param postParameter - True if POST contents relates to parameters, false otherwise. Irrelevant for GET requests
	 */
	public static void sendHttpRequest(URL url, String content, Method method, boolean postParameters) {
		HttpURLConnection httpConnection = null;
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
			
			if (method == Method.GET){
				httpConnection.setRequestMethod("GET");
				httpConnection.addRequestProperty("Content-type", "text/plain");
			} else {
				httpConnection.setRequestMethod("POST");
				httpConnection.addRequestProperty("Content-length", Integer.toString(content.length()));
				if (postParameters){
					httpConnection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
				} else {
					httpConnection.addRequestProperty("Content-type", "text/plain");
				}
				
			}
			
			httpConnection.connect();
		
			logger.info(CLASSNAME + ": sending request to " + url);
		
		} catch (Exception e){
			Utils.logStackTrace(e);
		}
	}
	
	/**
	 * Return the extension from the request URL
	 * @param request
	 * @return
	 */
	public static String extractExtension(HttpServletRequest request){
		String uri = request.getRequestURI();
		String[] uriBlocks = uri.split("/");
		return uriBlocks[(uriBlocks.length - 1)].trim();
	}
	
	
	
	/**
	 * Creates a directory to store the database and environment settings
	 * @path
	 */
	public static void createDirectory(Path dirPath, boolean overwrite){
		File dirPathFile = dirPath.toFile();
		
		if (overwrite){
			deleteDirectory(dirPathFile);
		}
		
		if (!dirPathFile.exists()){
			try {
				
				dirPathFile.mkdirs();
				logger.info(CLASSNAME + ": New directory created " + dirPathFile);
			} catch (SecurityException e){
				String error = "Unable to create directory";
				logger.error(CLASSNAME + ": " + error);
				throw new DispatcherException(error);
			}
		}
		
	}
	
	/**
	 * Recursively delete the contents of a directory
	 * @param dirPathFile
	 */
	private static void deleteDirectory(File dirPathFile){
		if (dirPathFile.isDirectory()) {
            File[] files = dirPathFile.listFiles();
            if(null != files) { 
                for(File file: files) {
                    if (file.isDirectory()){
                    	deleteDirectory(file);
                        file.delete();
                    } else {
                        file.delete();
                    }
                }
            }
		}
	}
	
}
