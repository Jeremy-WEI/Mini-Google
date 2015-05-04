package cis555.crawler;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.Logger;

import cis555.crawler.CrawlerUtils.Method;
import cis555.urlDispatcher.utils.DispatcherConstants;
import cis555.utils.CrawlerConstants;

import com.google.common.io.ByteStreams;

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
	 * @throws IOException
	 * @return
	 */
	public static Response parseResponse(HttpURLConnection connection, Method method) throws IOException {
		
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
			byte[] body = getBytesFromInputStream(connection.getInputStream());
			if (null == body){
				// Timed out
				return null;
			} else {
				response.setResponseBody(body);					
			}
		} else {
			response.setResponseBody(null);
		}
		return response;

	}
	
	/**
	 * Reads bytes from the input stream. Returns null 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static byte[] getBytesFromInputStream(InputStream in) throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[CrawlerConstants.BYTE_BUFFER_SIZE];
		boolean withinTimeLimit = true;
		Date timer = new Date();
		while (true){
			withinTimeLimit = (new Date().getTime() - timer.getTime()) < DispatcherConstants.READ_TIMEOUT;
			if (!withinTimeLimit){
				break;
			}

			int bytes = in.read(buffer);
			if (-1 == bytes){
				break;
			}
			out.write(buffer, 0, bytes);
			
		}
		
		if (!withinTimeLimit){
			logger.info(CLASSNAME + " Was unable to read within read timeout period, skipping");
			return null;
		} else {
			return out.toByteArray();
		}
		
	}
	
}