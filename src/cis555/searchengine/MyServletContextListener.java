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

        String path = "/Users/YunchenWei/Documents/EclipseWorkSpace/555_project/database";
        IndexTermDAO.setup(path);
        UrlIndexDAO.setup(path);
        PagerankDAO.setup(path);

    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }
}
