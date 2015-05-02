/**
 * 
 */
package cis555.searchengine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author cis455
 *
 */
public class MyServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();

        IndexTermDAO.setup("../../inputdata/database");
        UrlIndexDAO.setup("../../inputdata/database");
        PagerankDAO.setup("../../inputdata/database");

    }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}
}
