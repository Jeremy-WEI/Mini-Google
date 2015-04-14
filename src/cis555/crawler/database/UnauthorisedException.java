package cis555.crawler.database;

public class UnauthorisedException extends RuntimeException {

	public UnauthorisedException(String msg){
		super(msg);
	}
}
