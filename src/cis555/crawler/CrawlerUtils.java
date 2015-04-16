package cis555.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

public class CrawlerUtils {
	
	private static final Logger logger = Logger.getLogger(CrawlerUtils.class);
	private static final String CLASSNAME = CrawlerUtils.class.getName();


	public static enum Method {
		HEAD, GET
	}
	
	/**
	 * Retrieve an HTTP resource using GET
	 * @param absoluteURI
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static Response retrieveHttpResource(URL absoluteURL, Method method, String ifModifiedDateString) throws CrawlerException {
		try (Socket socket = new Socket(absoluteURL.getHost(), CrawlerConstants.PORT);){
			socket.setSoTimeout(CrawlerConstants.SOCKET_TIMEOUT);
			
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
			
			if (!ifModifiedDateString.isEmpty()){
				ifModifiedDateString = "If-Modified-Since: " + ifModifiedDateString + "\r\n";
			}
			
			String resoureName = absoluteURL.getFile();
			if (resoureName.isEmpty()){
				resoureName = "/";
			}
			
			String request = new String();
			
			if (method == Method.GET){
				request = "GET " + resoureName + " HTTP/1.0\r\nHost: " + absoluteURL.getHost() + 
						"\r\nUser-Agent: " + CrawlerConstants.CRAWLER_USER_AGENT + "\r\n\r\n";
				
			} else {
				request = "HEAD " + resoureName + " HTTP/1.0\r\nHost: " + absoluteURL.getHost() + 
						"\r\n" + ifModifiedDateString + "User-Agent: " + CrawlerConstants.CRAWLER_USER_AGENT + "\r\n\r\n";
			}
			
			output.print(request);
			output.flush();
			
			BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Response responseHeader = parseResponse(response, method);

			if (response != null){
				response.close();
			}
			
			if (socket != null){
				socket.close();
			}
			
			return responseHeader;
			
		} catch (UnknownHostException e ){
			String error = "Host unknown: " + absoluteURL.getHost();
			logger.error(CLASSNAME + ": " + error);
			e.printStackTrace();
			throw new CrawlerException(error);
		} catch (SocketTimeoutException e){
			String error = "Server timed out.";
			logger.error(CLASSNAME + ": " + error);
			e.printStackTrace();
			throw new CrawlerException(error);
		} catch (ResponseException e){
			e.printStackTrace();
			throw new CrawlerException(e.getMessage());
		}
		
		catch (IOException e){
			String error = "Unable to open resource.";
			logger.error(CLASSNAME + ": " + error);
			e.printStackTrace();
			throw new CrawlerException(error);			
		}
	}
	
	
	/**
	 * Given a response, parse out the headers and create a response object
	 * @param response
	 * @return
	 * @throws ResponseException
	 * @throws IOException
	 */
	private static Response parseResponse(BufferedReader response, Method method) throws ResponseException, IOException{
		String responseString;
		Response responseHeader = new Response();
		StringBuilder headerString = new StringBuilder();
		
		while ((responseString = response.readLine()) != null){
			if (responseString.isEmpty()){
				
				responseHeader.parseHeader(headerString.toString());
				int contentLength = responseHeader.getContentLength();
				
				if (method == Method.GET){					
					responseHeader.setResponseBody(extractBody(response, contentLength));
				}
				
				break;
			} else {
				headerString.append(responseString + "\n");
			}
		} 
		return responseHeader;

	}
	
	/**
	 * Retrieve an https resource
	 * @param absoluteURI
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Response retrieveHttpsResource(URL absoluteURL, Method method, String ifModifiedDateString) throws MalformedURLException, IOException{
		
		HttpsURLConnection httpConnection = (HttpsURLConnection)absoluteURL.openConnection();
		httpConnection.addRequestProperty("User-Agent", CrawlerConstants.CRAWLER_USER_AGENT);
		if (method == Method.HEAD){
			httpConnection.setRequestMethod("HEAD");
		}

		if (!ifModifiedDateString.isEmpty()){
			httpConnection.addRequestProperty("If-Modified-Since", ifModifiedDateString);
		}
		
//		logger.info(CLASSNAME + ": https connection established with " + absoluteURL);
		
		BufferedReader response = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		Response responseHeader = new Response();
		int contentLength = httpConnection.getContentLength();
		responseHeader.parseContentType(httpConnection.getContentType());
		responseHeader.setResponseCode(Integer.toString(httpConnection.getResponseCode()));
		String locationString = httpConnection.getHeaderField("Location");
		if (null != locationString && !locationString.isEmpty()){
			responseHeader.setLocation(new URL(locationString));
		}
		
		if (method == Method.GET && contentLength > 1){
			responseHeader.setResponseBody(extractBody(response, contentLength));
		} else {
			responseHeader.setResponseBody("");
		}
		

		if (response != null){
			response.close();
		}
		return responseHeader;
	}
	
	
	/**
	 * Extracts the body of the response
	 * @param response
	 * @param contentLength
	 * @return
	 * @throws IOException
	 */
	private static String extractBody(BufferedReader response, int contentLength) throws IOException {
		
		if (contentLength < 1){
			String error = "Non positive content length: " + contentLength;
			logger.error(CLASSNAME + ": " + error);
			throw new CrawlerException(error);
		}
		
		StringBuilder responseString = new StringBuilder();
		
		try {
			for (int i = 0; i < contentLength; i++){
				int charRead = response.read();
				if (charRead > -1){
					responseString.append((char) charRead);
				} else {
					break;
				}
			}
			return responseString.toString();
			
		} catch (SocketTimeoutException e){
			String error = "Socket timed out";
			logger.error(CLASSNAME + ": " + error);
			throw new CrawlerException(error);
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
		
		String domain = originalUrl.getHost();
		String protocol = originalUrl.getProtocol();
		String directoryPath = originalUrl.getFile();
		int slash = directoryPath.lastIndexOf("/");
		int dot = directoryPath.lastIndexOf(".");

		if (directoryPath.isEmpty()){
			directoryPath = "/";
		} else if (dot > -1){
			directoryPath = directoryPath.substring(0, slash + 1);
		} else if (directoryPath.charAt(directoryPath.length() - 1) != '/'){
			
			// Missing a "/" at the end
			directoryPath = directoryPath + "/";

		}
		
		
		if (rawUrl.isEmpty()){
			throw new MalformedURLException();
		}
		
		rawUrl = rawUrl.trim();
		// Starts with http
		
		if (rawUrl.toLowerCase().startsWith("http")){
			return new URL(rawUrl);
		}
		
		// Relative uri

//		String filenamePattern = "^[a-zA-Z0-9-_]+" + Pattern.quote(".") + "[a-z]+";
		
		if (rawUrl.startsWith("/")){
			
			// This is a root relative path
			
			rawUrl = protocol + "://" + domain + rawUrl;
		} else {

			// This is a relative path

			rawUrl = protocol + "://" + domain + directoryPath + rawUrl;
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
