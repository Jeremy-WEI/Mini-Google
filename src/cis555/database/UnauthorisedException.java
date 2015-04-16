package cis555.database;

public class UnauthorisedException extends RuntimeException {

	public UnauthorisedException(String msg){
		super(msg);
	}
}
