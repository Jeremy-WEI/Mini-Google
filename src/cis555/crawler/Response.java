package cis555.crawler;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;

import cis555.crawler.CrawlerUtils.Method;

public class Response {

	/**
	 * Member variables + relevant getters
	 */
	
	private static final Logger logger = Logger.getLogger(Response.class);
	private static final String CLASSNAME = Response.class.getName();
	
	private byte[] responseBody;	
	public void setResponseBody(byte[] responseBody){
		this.responseBody = responseBody;
	}
	public byte[] getResponseBody(){
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
		XML, HTML, TEXT, PDF, OTHERS
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
		} else if (header.contains("pdf")){
			this.contentType = ContentType.PDF;
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
	public static Response parseResponse(HttpURLConnection connection, Method method) throws IOException {
		
		try {
			int status = connection.getResponseCode();
			
			if (status > 350){
//				logger.info(CLASSNAME + ": non 2 or 300 result");
				return null;
			}
			
			Response response = new Response();
			int contentLength = connection.getContentLength();
			response.parseContentType(connection.getContentType());
			response.setResponseCode(Integer.toString(connection.getResponseCode()));
					
			String locationString = connection.getHeaderField("Location");
			if (null != locationString && !locationString.isEmpty()){
				URL url = CrawlerUtils.filterURL(locationString);
				if (null != url){
					response.setLocation(new URL(locationString));				
				}
			}
			
			String language = connection.getHeaderField("Content-Language");
			if (null != language && !language.isEmpty()){
				response.setContentLanguage(language.toLowerCase());
			}

			if (method == Method.GET){
				response.setResponseBody(ByteStreams.toByteArray(connection.getInputStream()));
			} else {
				response.setResponseBody(null);
			}
			return response;
			
		} catch (NullPointerException e){
			
			// Happens if connection is terminated in mid process
			return null;
		}
		
	}
	
}