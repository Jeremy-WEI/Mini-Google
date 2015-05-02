package cis555.urlDispatcher.utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	 * @throws IOException 
	 */
	public static void sendHttpRequest(final URL url, String content, Method method, boolean postParameters) throws IOException {
		final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		try {
			httpConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
			httpConnection.setConnectTimeout(DispatcherConstants.HTTP_TIMEOUT);
			httpConnection.setReadTimeout(DispatcherConstants.READ_TIMEOUT);
			
			if (method == Method.GET){
				httpConnection.setRequestMethod("GET");
				httpConnection.addRequestProperty("Content-Type", "text/plain");
			} else {
				httpConnection.setRequestMethod("POST");
				httpConnection.addRequestProperty("Content-Length", Integer.toString(content.length()));
				if (postParameters){
					httpConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				} else {
					httpConnection.addRequestProperty("Content-Type", "text/plain");
				}
				httpConnection.setDoOutput(true);
				httpConnection.getOutputStream().write(content.getBytes(CrawlerConstants.CHARSET));
			}
			
			// THIS STUFF IS NEW
			
			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			final Future<Void> handler = executor.submit(new Callable<Void>() {
			  public Void call() throws Exception {
			    try {
			    	httpConnection.connect();
					logger.info(CLASSNAME + ": sending request to " + url + " with response code " + httpConnection.getResponseCode());	
			    } catch (Exception e) {
			      e.printStackTrace();
			    }
			    return null;
			  }
			});
			
			executor.schedule(new Runnable() {
				  public void run() {
					  httpConnection.disconnect();
				    handler.cancel(true);
				    executor.shutdownNow(); 
				  }
				}, DispatcherConstants.READ_TIMEOUT, TimeUnit.MILLISECONDS);
				handler.get();
				executor.shutdownNow();
			
				// THIS STUFF IS NEW
			
//    	httpConnection.connect();
//		logger.info(CLASSNAME + ": sending request to " + url + " with response code " + httpConnection.getResponseCode());	

				
				
		} catch (CancellationException e){
			logger.debug(CLASSNAME + " request timed out after " + DispatcherConstants.READ_TIMEOUT + " seconds");
			
		} catch (SocketTimeoutException e){
			logger.debug(CLASSNAME + " timed out when sending request to " + url);
		} catch (IOException e){
			logger.debug(CLASSNAME + " was unable to send request to " + url);			
		} catch (Exception e){
			logger.debug(CLASSNAME + " raised exception " + url);
			Utils.logStackTrace(e);
		}  finally {
			if (null != httpConnection){
				httpConnection.disconnect();
			}
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
