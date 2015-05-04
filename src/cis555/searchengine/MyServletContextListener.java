/**
 * 
 */
package cis555.searchengine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author cis455
 *
 */
public class MyServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        // ServletContext context = contextEvent.getServletContext();

        String path = "database";
//        String path = "../../inputdata/database";
        IndexTermDAO.setup(path);
        UrlIndexDAO.setup(path);
        PagerankDAO.setup(path);
        ContentDAO.setup(path);

    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }
}
