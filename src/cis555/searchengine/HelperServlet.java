package cis555.searchengine;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            String docID = request.getParameter("docID");
            PrintWriter pw = response.getWriter();
            pw.println("docID:");
            pw.println(docID);
            pw.println("Content:");
            pw.println(ContentDAO.getContent(docID));
            pw.println("Pagerank Value:");
            pw.println(PagerankDAO.getPagerankValue(docID));
        } catch (Exception e) {
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }

}
