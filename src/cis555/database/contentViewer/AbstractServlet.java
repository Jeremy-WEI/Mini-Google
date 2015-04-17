package cis555.database.contentViewer;

import javax.servlet.http.HttpServlet;

import cis555.database.DBConstants;
import cis555.database.DBWrapper;
import cis555.database.Dao;

@SuppressWarnings("serial")
public abstract class AbstractServlet extends HttpServlet{
	
	protected static Dao dao;
	
	protected void setupDatabase(){
		
		if (null == dao){
			String directory = DBConstants.DB_DIRECTORY;
			DBWrapper wrapper = new DBWrapper(directory, true);
			dao = wrapper.getDao();
		}
	}	

	
	@Override
	public void destroy(){
		DBWrapper.shutdown();
	}

}
