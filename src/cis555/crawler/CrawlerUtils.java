package cis555.crawler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

import cis555.utils.CrawlerConstants;

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
	public static Response retrieveHttpResource(URL absoluteURL, Method method, String ifModifiedDateString){
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
			
			httpConnection.connect();
			
			Response response = Response.parseResponse(httpConnection, method);
			httpConnection.disconnect();

			
			return response;

			
		} catch (IOException | IllegalArgumentException e) {
			String error = "Unable to open connection to " + absoluteURL + ", skipping";
			logger.debug(CLASSNAME + ": " + error);
			throw new CrawlerException(error);
		} finally {
			if (null != httpConnection){
				httpConnection.disconnect();
			}
		}
	}
	

	
	/**
	 * Retrieve a resource via https
	 * @param absoluteURI
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Response retrieveHttpsResource(URL absoluteURL, Method method, String ifModifiedDateString) throws MalformedURLException, IOException{
		
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
			
			Response response = Response.parseResponse(httpsConnection, method);
			httpsConnection.disconnect();
			return response;

		} catch (IOException e) {
			String error = "Unable to open connection to " + absoluteURL + ", skipping";
			logger.debug(CLASSNAME + ": " + error);
//			e.printStackTrace();
			throw new CrawlerException(error);
		}  finally {
			if (null != httpsConnection){
				httpsConnection.disconnect();
			}
		}
	}

	/**
	 * Converts a link to a URL
	 * @param rawUrl
	 * @param domain
	 * @return
	 * @throws MalformedURLException 
	 */
	public static URL convertToUrl(String rawUrl, URL originalUrl) throws MalformedURLException {
		
		if (rawUrl.isEmpty()){
			throw new MalformedURLException();
		}
		
		rawUrl = rawUrl.trim();
		
		if (rawUrl.toLowerCase().startsWith("http")){
			
			if (rawUrl.charAt(rawUrl.length() - 1) == '/'){
				// Remove trailing /
				rawUrl = rawUrl.substring(0, rawUrl.length() - 1);
			}
			
			// Starts with http
			return new URL(rawUrl);
		}
		
		String domain = originalUrl.getHost();
		String protocol = originalUrl.getProtocol();
		String directoryPath = originalUrl.getFile();
		int slash = directoryPath.lastIndexOf("/");
		int dot = directoryPath.lastIndexOf(".");

		if (directoryPath.isEmpty()){
			directoryPath = "/";
		} else if (dot > -1){
			
			// No dot, so not a file
			
			directoryPath = directoryPath.substring(0, slash + 1);
		} else if (directoryPath.charAt(directoryPath.length() - 1) != '/'){
			
			// Missing a "/" at the end
			directoryPath = directoryPath + "/";

		}
		
		// Relative uri

		if (rawUrl.startsWith("//")){
			// relative to http:
			rawUrl = protocol + ":" + rawUrl;
		}
		
//		String filenamePattern = "^[a-zA-Z0-9-_]+" + Pattern.quote(".") + "[a-z]+";
		
		else if (rawUrl.startsWith("/")){
			
			// This is a root relative path
			
			rawUrl = protocol + "://" + domain + rawUrl;
		} else {

			// This is a relative path

			rawUrl = protocol + "://" + domain + directoryPath + rawUrl;
		} 

		if (rawUrl.charAt(rawUrl.length() - 1) == '/'){
			// Remove trailing /
			rawUrl = rawUrl.substring(0, rawUrl.length() - 1);
		}
		
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
