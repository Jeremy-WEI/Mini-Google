package cis555.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.log4j.Logger;

import cis555.crawler.CrawlerUtils.Method;

public class Response {

	/**
	 * Member variables + relevant getters
	 */
	
	private static final Logger logger = Logger.getLogger(Response.class);
	private static final String CLASSNAME = Response.class.getName();
	
	private String responseBody;	
	public void setResponseBody(String responseBody){
		this.responseBody = responseBody;
	}
	public String getResponseBody(){
		return this.responseBody;
	}
	
	private ContentType contentType;
	public ContentType getContentType(){
		return this.contentType;
	}
	
	private int contentLength;
	public int getContentLength(){
		return this.contentLength;
	}
	
	private String responseCode;
	public String getResponseCode(){
		return this.responseCode;
	}
	public void setResponseCode(String responseCode){
		this.responseCode = responseCode;
	}
	
	private URL location;
	public URL getLocation(){
		return this.location;
	}
	public void setLocation(URL location){
		this.location = location;
	}
	
	private String contentLanguage;
	public String getContentLanguage(){
		if (null == contentLanguage){
			return "NONE";
		} else {
			return this.contentLanguage;
		}
	}
	public void setContentLanguage(String language){
		this.contentLanguage = language;
	}
	
	
	public enum ContentType {
		XML, HTML, TEXT, OTHERS
	}
	
	/**
	 * Parse the content type of the header
	 * @param header
	 */
	public void parseContentType(String header) throws ResponseException {
		if (null == header){
			this.contentType = ContentType.OTHERS;
		} else if (header.contains("xml")){
			this.contentType = ContentType.XML;
		} else if (header.contains("html")){
			this.contentType = ContentType.HTML;
		} else if (header.contains("text")){
			this.contentType = ContentType.TEXT;
		} else {
			this.contentType = ContentType.OTHERS;
		}
	}
	
	/**
	 * Extracts relevant details from an http or https connection
	 * @param connection
	 * @param method
	 * @return
	 * @throws IOException
	 */
	public static Response parseResponse(HttpURLConnection connection, Method method) throws IOException{
		Response response = new Response();
		BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		int contentLength = connection.getContentLength();
		response.parseContentType(connection.getContentType());
		response.setResponseCode(Integer.toString(connection.getResponseCode()));
		
		String locationString = connection.getHeaderField("Location");
		if (null != locationString && !locationString.isEmpty()){
			response.setLocation(new URL(locationString));
		}
		
		String language = connection.getHeaderField("Content-Language");
		if (null != language && !language.isEmpty()){
			response.setContentLanguage(language.toLowerCase());
		}
		
		if (method == Method.GET){
			response.setResponseBody(response.extractBody(responseReader, contentLength));
		} else {
			response.setResponseBody("");
		}
		return response;
	}
	
	
	/**
	 * Extracts the body of the response
	 * @param response
	 * @param contentLength
	 * @return
	 * @throws IOException
	 */
	private String extractBody(BufferedReader response, int contentLength) throws IOException {
		
		if (contentLength < 1){
			contentLength = CrawlerConstants.DEFAULT_CONTENT_LENGTH;
//			String error = "Non positive content length: " + contentLength;
//			logger.error(CLASSNAME + ": " + error);
//			throw new CrawlerException(error);
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
	
	
//	/***
//	 * Methods
//	 *
//	 */
//
//	public Response() {}
//
//	/**
//	 * Parse a header
//	 */
//	public void parseHeader(String headerString) throws ResponseException {
//				
//		String[] headerLines = headerString.split("\\r?\\n");
//
//		if (headerLines.length < 1) {
//			String error = "No header received";
//			logger.error(CLASSNAME + ": " + error);
//			throw new ResponseException(error);
//		}
//
//		parseFirstLine(headerLines[0]);
//		parseOtherHeaders(headerLines);
//
//	}
//
//	/**
//	 * Parse the first line of a request
//	 * 
//	 * @param firstLine
//	 */
//	private void parseFirstLine(String firstLine)
//			throws ResponseException {
//		String[] parts = firstLine.split("\\s");
//
//		if (parts.length < 3) {
//			logger.error(CLASSNAME + ": First header line malformed: "
//					+ firstLine);
//			throw new ResponseException("First header line malformed");
//		}
//		
//		String http = parts[0].toUpperCase().trim();
//		String responseCode = parts[1].trim();
//		String responseDetails = parts[2].trim();
//
//		parseResponseCode(responseCode);
//	}
//
//	/**
//	 * Parse the response code
//	 * 
//	 * @param method
//	 */
//	private void parseResponseCode(String responseCode) throws ResponseException {
//
//		if (!responseCode.matches("\\d{3}")){
//			String error = "Response returned invalid response code: " + responseCode;
//			logger.error(CLASSNAME + ": " + error);
//			throw new ResponseException(error);
//		} else {
//			this.responseCode = responseCode;
//		}
//	}
//
//	/***
//	 * Other header lines 
//	 * ******
//	 */
//
//	/**
//	 * Checks headers: - Presence of Host - If-Modified-Since -
//	 * If-Unmodified-Since
//	 * 
//	 * @param headerLines
//	 */
//	private void parseOtherHeaders(String[] headerLines) {
//		for (String header : headerLines) {
//			
//			String lowerCaseHeader = header.toLowerCase();
//			
//			if (lowerCaseHeader.startsWith("content-type")){
//				parseContentType(header.trim());
//			} if (lowerCaseHeader.contains("content-length")){
//				parseContentLength(header.trim());
//			} if (lowerCaseHeader.contains("location")){
//				parseLocation(header.trim());
//			}
//		}
//	}
//	

//	
//	/**
//	 * Extract the length of the body
//	 * @param header
//	 * @throws ResponseException
//	 */
//	private void parseContentLength(String header) throws ResponseException {
//		try {
//			String lengthString = extractValueFromKeyValuePair(header)[1];
//			this.contentLength = Integer.parseInt(lengthString);
//			
//		} catch (Exception e){
//			String error = "Unable to parse content length: " + header;
//			e.printStackTrace();
//			logger.error(CLASSNAME + ": " + error);
//			throw new ResponseException(error);
//		}
//		
//	}
//	
//	/**
//	 * Parse a the location part of the header
//	 * @param header
//	 */
//	private void parseLocation(String header){
//		try {
//			String urlString = extractValueFromKeyValuePair(header)[1];
//			this.location = new URL(urlString);
//		} catch (MalformedURLException e){
//			String error = "Location URL not valid: " + header;
//			logger.error(CLASSNAME + ": " + error);
//			throw new ResponseException(error);
//			
//		}
//	}
//		
//	/***
//	 * From a key: value string, extract the value
//	 * @param header
//	 * @return
//	 */
//	private String[] extractValueFromKeyValuePair(String header) throws ResponseException {
//		int colonIndex = header.indexOf(":");
//		if (colonIndex < 1){
//			logger.error(CLASSNAME + ": Missing colon in header line " + header);
//			throw new ResponseException("Missing colon in header line: " + header);
//			
//		}
//		String key = header.substring(0, colonIndex).trim();
//		String value = header.substring(colonIndex + 1).trim();
//		String[] keyValuePair = {key, value};
//		return keyValuePair;
//	}
	



	
}