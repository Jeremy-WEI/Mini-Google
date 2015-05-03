package cis555.searchengine;

import java.io.PrintWriter;

public class ServletHelper {
    public static void prepareWrite(PrintWriter pw, String contextPath,
            String title) {
        pw.write("<!DOCTYPE html><html>");
        pw.write("<head>");
        pw.write("<title>" + title + "</title>");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + contextPath + "/stylesheet/bootstrap.min.css\">");
        pw.write("</head>");
        pw.write("<body>");
    }

    public static void finishWrite(PrintWriter pw) {
        pw.write("</body>");
        pw.write("</html>");
    }

    public static void writePanel(PrintWriter pw, String panelHead,
            String panelBody, String className) {
        pw.write("<div class=\"panel " + className + "\" style=\"\">");
        pw.write("<div class=\"panel-heading\">" + panelHead + "</div>");
        pw.write("<div class=\"panel-body\" style=\"height:100px;overflow:scroll;\">");
        pw.write(panelBody);
        pw.write("</div>");
        pw.write("</div>");
    }

    public static void writePanel(PrintWriter pw, String panelHead,
            String panelBody, String panelFoot, String className) {
        pw.write("<div class=\"panel " + className + "\" style=\"\">");
        pw.write("<div class=\"panel-heading\">" + panelHead + "</div>");
        pw.write("<div class=\"panel-body\" style=\"height:100px;overflow:scroll;\">");
        pw.write(panelBody);
        pw.write("</div>");
        pw.write("<div class=\"panel-footer\">" + panelFoot + "</div>");
        pw.write("</div>");
    }

}
