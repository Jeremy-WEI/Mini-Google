package cis555.database.contentViewer;

import javax.servlet.http.HttpServlet;

import cis555.database.CrawlerDao;
import cis555.utils.CrawlerConstants;
import cis555.utils.DBWrapper;

import com.sleepycat.persist.EntityStore;

@SuppressWarnings("serial")
public abstract class AbstractServlet extends HttpServlet{
	
	protected static CrawlerDao dao;
	
	protected void setupDatabase(){
		
		if (null == dao){
			EntityStore store = DBWrapper.setupDatabase(CrawlerConstants.SERVLET_DB_DIRECTORY, true);
			dao = new CrawlerDao(store);
		}
	}	

	
	@Override
	public void destroy(){
		DBWrapper.shutdown();
	}

}
