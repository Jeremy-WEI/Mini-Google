/**
 * 
 */
package cis555.searchEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cis555.searchengine.DBWrapper;


/**
 * @author cis455
 *
 */
public class MyServletContextListener implements ServletContextListener {
	DBWrapper db;

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
        db.shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		ServletContext context = contextEvent.getServletContext();
		
		DocHitEntityIndexTermDBDAO.setup("database");
		UrlIndexDAO.setup("database");
		
		
	}
}
