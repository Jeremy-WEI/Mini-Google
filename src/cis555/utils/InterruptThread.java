package cis555.utils;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;

import cis555.urlDispatcher.utils.DispatcherConstants;

public class InterruptThread implements Runnable {

	private static final Logger logger = Logger.getLogger(InterruptThread.class);
	private static final String CLASSNAME = InterruptThread.class.getName();
	
	private HttpURLConnection connection;
	
	public InterruptThread(HttpURLConnection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(DispatcherConstants.READ_TIMEOUT);
		} catch (InterruptedException e){
			
		}
		
		try {
			this.connection.getInputStream().close();
			this.connection.disconnect();
		} catch (IOException e) {
			
			logger.debug(CLASSNAME + " IO Errror when trying to close stream");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
