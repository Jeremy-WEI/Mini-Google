package cis555.utils;

import java.net.URL;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Refers to source -> target URLs (hashed)
 *
 */
@Entity
public class FromToUrls {

	@PrimaryKey
	private String fromURL;
	
	private List<String> toURLs;
	
	private FromToUrls(){}
	
	public FromToUrls(String fromURL, List<String> toURLs){
		this.fromURL = fromURL;
		this.toURLs = toURLs;
	}
	
	public String getFromUrl(){
		return this.fromURL;
	}
	
	public List<String> getToURLs(){
		return this.toURLs;
	}
	
}
