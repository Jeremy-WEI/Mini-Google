package cis555.crawler;

import java.net.URL;

public class RawCrawledItem {

	private URL url;
	private byte[] rawContents;
	private String contentType;
	private boolean isNew;
	
	public RawCrawledItem(URL url, byte[] rawContents, String contentType, boolean isNew){
		this.url = url;
		this.rawContents = rawContents;
		this.contentType = contentType;
		this.isNew = isNew;
	}
	
	public URL getURL(){
		return this.url;
	}
	
	public byte[] getRawContents(){
		return this.rawContents;
	}
	
	public String getContentType(){
		return this.contentType;
	}
	
	public boolean isNew(){
		return this.isNew;
	}
}
