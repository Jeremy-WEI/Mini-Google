package cis555.database.contentViewer;

import javax.servlet.http.HttpServlet;

import cis555.database.DBWrapper;
import cis555.database.Dao;
import cis555.utils.CrawlerConstants;

@SuppressWarnings("serial")
public abstract class AbstractServlet extends HttpServlet{
	
	protected static Dao dao;
	
	protected void setupDatabase(){
		
		if (null == dao){
			String directory = CrawlerConstants.SERVLET_DB_DIRECTORY;
			DBWrapper wrapper = new DBWrapper(directory, true);
			dao = wrapper.getDao();
		}
	}	

	
	@Override
	public void destroy(){
		DBWrapper.shutdown();
	}

}
