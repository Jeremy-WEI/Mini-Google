package cis555.crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class CrawlerUtils {
	
	private static final Logger logger = Logger.getLogger(CrawlerUtils.class);
	private static final String CLASSNAME = CrawlerUtils.class.getName();


	public static enum Method {
		HEAD, GET
	}
			
	/**
	 * Retrieves a resource via http
	 * @param absoluteURL
	 * @param method
	 * @param ifModifiedDateString
	 * @return
	 */
	public static Response retrieveHttpResource(URL absoluteURL, final Method method, String ifModifiedDateString){
		
		HttpURLConnection httpConnection = null;
		
		try {
			httpConnection = (HttpURLConnection) absoluteURL.openConnection();
			httpConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
			if (method == Method.HEAD){
				httpConnection.setRequestMethod("HEAD");
			}

			if (!ifModifiedDateString.isEmpty()){
				httpConnection.addRequestProperty("If-Modified-Since", ifModifiedDateString);
			}
			httpConnection.setConnectTimeout(DispatcherConstants.HTTP_TIMEOUT);
			httpConnection.setReadTimeout(DispatcherConstants.READ_TIMEOUT);

//			if (method == Method.GET && !absoluteURL.toString().endsWith("robots.txt")){
//				
//				// Force closes the connection if it's open for too long
//				new Thread(new InterruptThread(httpConnection)).start();				
//			}
			
			httpConnection.connect();
			
			Response response = Response.parseResponse(httpConnection, method);

			return response;
			
//			// THIS STUFF IS NEW
//
//			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
//			final Future<Response> handler = executor.submit(new Callable<Response>() {
//			  public Response call() throws Exception {
//			    try {
//					Response response = Response.parseResponse(httpConnection, method);
////					logger.info(CLASSNAME + ": sending request to " + absoluteURL + " with response code " + httpConnection.getResponseCode());	
//					return response;
//			    } catch (Exception e) {
//			      e.printStackTrace();
//			    }
//			    return null;
//			  }
//			});
//			
//			executor.schedule(new Runnable() {
//				  public void run() {
//					  httpConnection.disconnect();
//				    handler.cancel(true);
//				    executor.shutdownNow(); 
//				  }
//				}, DispatcherConstants.HTTP_TIMEOUT, TimeUnit.MILLISECONDS);
//				Response response = handler.get();
//				executor.shutdownNow();
//				return response;
//				// THIS STUFF IS NEW
			
			
		}  catch (SocketTimeoutException e){
			logger.debug(CLASSNAME + " timed out when sending request to " + absoluteURL);
			return null;
		} catch (IOException e){
			logger.debug(CLASSNAME + " was unable to send request to " + absoluteURL);			
			return null;
		} catch (Exception e) {
			Utils.logStackTrace(e);
			logger.debug(CLASSNAME + " Unable to open connection to " + absoluteURL + ", skipping");
			return null;
		} finally {
			if (null != httpConnection){
				try {
					httpConnection.getInputStream().close();
					httpConnection.disconnect();
				} catch (IOException e) {
					logger.debug(CLASSNAME + " IO Errror when trying to close stream");

				}
				
			}
		}
	}

		
//		
//		HttpURLConnection httpConnection = null;
//		try {
//			httpConnection = (HttpURLConnection) absoluteURL.openConnection();
//			httpConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
//			if (method == Method.HEAD){
//				httpConnection.setRequestMethod("HEAD");
//			}
//
//			if (!ifModifiedDateString.isEmpty()){
//				httpConnection.addRequestProperty("If-Modified-Since", ifModifiedDateString);
//			}
//			httpConnection.setConnectTimeout(DispatcherConstants.HTTP_TIMEOUT);
//			httpConnection.setReadTimeout(DispatcherConstants.READ_TIMEOUT);
//
//			httpConnection.connect();
//			
//			Response response = Response.parseResponse(httpConnection, method);
//			
//			return response;
//
//			
//		} catch (Exception e) {
//			Utils.logStackTrace(e);
//			String error = "Unable to open connection to " + absoluteURL + ", skipping";
//			throw new CrawlerException(error);
//		} finally {
//			if (null != httpConnection){
//				httpConnection.disconnect();
//			}
//		}
	

	
	
	/**
	 * Retrieve a resource via https
	 * @param absoluteURI
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Response retrieveHttpsResource(final URL absoluteURL, final Method method, String ifModifiedDateString) throws MalformedURLException, IOException{
		
		HttpsURLConnection httpsConnection = null; 
		
		try {
		
			httpsConnection = (HttpsURLConnection)absoluteURL.openConnection();
			httpsConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
			if (method == Method.HEAD){
				httpsConnection.setRequestMethod("HEAD");
			}
			httpsConnection.setInstanceFollowRedirects(false);
			if (!ifModifiedDateString.isEmpty()){
				httpsConnection.addRequestProperty("If-Modified-Since", ifModifiedDateString);
			}
			httpsConnection.setConnectTimeout(DispatcherConstants.HTTP_TIMEOUT);
			httpsConnection.setReadTimeout(DispatcherConstants.READ_TIMEOUT);
			
//			if (method == Method.GET && !absoluteURL.toString().endsWith("robots.txt")){
//			
//			// Force closes the connection if it's open for too long
//			new Thread(new InterruptThread(httpConnection)).start();				
//		}
			
			httpsConnection.connect();
			
			Response response = Response.parseResponse(httpsConnection, method);

			return response;

			
			// THIS STUFF IS NEW

//			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
//			final Future<Response> handler = executor.submit(new Callable<Response>() {
//			  public Response call() throws Exception {
//			    try {
//					Response response = Response.parseResponse(httpsConnection, method);
////					logger.info(CLASSNAME + ": sending request to " + absoluteURL + " with response code " + httpsConnection.getResponseCode());	
//					return response;
//			    } catch (Exception e) {
//			      e.printStackTrace();
//			    }
//			    return null;
//			  }
//			});
//			
//			executor.schedule(new Runnable() {
//				  public void run() {
//					  httpsConnection.disconnect();
//				    handler.cancel(true);
//				    executor.shutdownNow(); 
//				  }
//				}, 5000, TimeUnit.MILLISECONDS);
//				Response response = handler.get();
//				executor.shutdownNow();
//				return response;
//				// THIS STUFF IS NEW
			

		} catch (IOException e){
			logger.debug(CLASSNAME + " was unable to send request to " + absoluteURL);	
			return null;
		} catch (Exception e) {
			Utils.logStackTrace(e);
			logger.debug(CLASSNAME + " was unable to send request to " + absoluteURL);	
			return null;
		}  finally {
			if (null != httpsConnection){
				httpsConnection.getInputStream().close();
				httpsConnection.disconnect();
			}
		}
//
//		HttpsURLConnection httpsConnection = null;
//		
//		try {
//			httpsConnection = (HttpsURLConnection)absoluteURL.openConnection();
//			httpsConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
//			if (method == Method.HEAD){
//				httpsConnection.setRequestMethod("HEAD");
//			}
//			httpsConnection.setInstanceFollowRedirects(false);
//			if (!ifModifiedDateString.isEmpty()){
//				httpsConnection.addRequestProperty("If-Modified-Since", ifModifiedDateString);
//			}
//			httpsConnection.setConnectTimeout(DispatcherConstants.HTTP_TIMEOUT);
//			httpsConnection.setReadTimeout(DispatcherConstants.READ_TIMEOUT);
//						
//			Response response = Response.parseResponse(httpsConnection, method);
//			return response;
//
//		} catch (Exception e) {
//			Utils.logStackTrace(e);
//			String error = "Unable to open connection to " + absoluteURL + ", skipping";
//			throw new CrawlerException(error);
//		}  finally {
//			if (null != httpsConnection){
//				httpsConnection.disconnect();
//			}
//		}

	
	
	}

	/**
	 * 
	 * @param rawUrl
	 * @return
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	public static URL filterURL(String rawUrl) throws MalformedURLException, UnsupportedEncodingException {
		if (rawUrl.isEmpty()){
			return null;
		}
		
		rawUrl = rawUrl.trim();
		
		if (!rawUrl.startsWith("http")){
			return null;
		}
		if (!rawUrl.contains(".")){
			return null;
		}
		
		if (rawUrl.length() > CrawlerConstants.MAX_URL_LENGTH){
			return null;
		} if (rawUrl.length() < CrawlerConstants.MIN_URL_LENGTH){
			return null;
		} else if (rawUrl.contains("#")){
			String newUrlString = rawUrl.substring(0, rawUrl.indexOf("#"));
			return new URL(newUrlString);
		}  else if (rawUrl.charAt(rawUrl.length() - 1) == '/'){
			// Remove trailing /
			rawUrl = rawUrl.substring(0, rawUrl.length() - 1);
		}
		
		rawUrl = URLDecoder.decode(rawUrl, CrawlerConstants.CHARSET);
		
		return new URL(rawUrl);
		
	}
	
	/**
	 * Determines the right agent name for disallow, allow and waits
	 * @param info
	 * @return
	 */
	public static String extractAgent(SiteInfo info){
		String agentInSiteInfo = "*";
		
		// filter vs allow / disallow list
		
		if (info.containsUserAgent(CrawlerConstants.CRAWLER_USER_AGENT)){
			agentInSiteInfo = CrawlerConstants.CRAWLER_USER_AGENT;
		}
		return agentInSiteInfo;
	}
	
	
	/**
	 * Determines if URL can be crawled or not
	 * @param info
	 * @param url
	 * @return
	 */
	public static URL filterURL(SiteInfo info, URL url, String agentName){
		
		List<String> allowList = info.getAllowedLinks(agentName);
		List<String> disallowList = info.getDisallowedLinks(agentName);
		
		if (null != allowList){
			for (String path : allowList){
				if (url.getFile().startsWith(path)){
					if (isLongerMatchThanDisallowList(url, path, disallowList)){
						// allow match is more specific than disallow match					
						return url;
					} else {
						// disallow match is more specific than allow match
						return null;
					}
				}
			}
		} 
		
		else {
			
			// Not on allow list, check if it's on disallow list			
			
			if (null == disallowList){
				// No disallow list
				return url;
			}
			
			for (String disallowPath : disallowList){
				if (url.getFile().startsWith(disallowPath)){
					return null;
				}
			}
			
		}
		
		// Not on allow nor disallow list
		
		return url;
	}
	
	/**
	 * Determines whether allow match is more specific than disallow match or not.
	 * @param url
	 * @param allowPath
	 * @param disallowList
	 * @return
	 */
	private static boolean isLongerMatchThanDisallowList(URL url, String allowPath, List<String> disallowList){
		if (null == disallowList){
			return true;
		} else {
			for (String disallowPath : disallowList){
				if (url.getFile().startsWith(disallowPath)){	
					return (allowPath.length() > disallowPath.length());
				}
			}
			return true;
		}
	}	
}
