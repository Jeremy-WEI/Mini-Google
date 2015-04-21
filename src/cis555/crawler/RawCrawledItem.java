package cis555.crawler;

import java.net.URL;

import cis555.crawler.Response.ContentType;

public class RawCrawledItem {

	private URL url;
	private byte[] rawContents;
	private ContentType contentType;
	private boolean isNew;
	
	public RawCrawledItem(URL url, byte[] rawContents, ContentType contentType, boolean isNew){
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
	
	public ContentType getContentType(){
		return this.contentType;
	}
	
	public boolean isNew(){
		return this.isNew;
	}
}
