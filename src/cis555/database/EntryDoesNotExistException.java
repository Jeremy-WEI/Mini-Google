package cis555.database;

public class EntryDoesNotExistException extends RuntimeException {
	
	public EntryDoesNotExistException(String query){
		super("No entry for :" + query);
	}

}
