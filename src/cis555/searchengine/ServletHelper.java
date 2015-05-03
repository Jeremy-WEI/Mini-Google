package cis555.searchengine;

import java.io.PrintWriter;

public class ServletHelper {

    public static void prepareWrite(PrintWriter pw) {
        pw.write("<!DOCTYPE html><html>");
    }

    public static void WriteHeader(PrintWriter pw, String title) {
        pw.write("<head>");
        pw.write("<title>" + title + "</title>");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet/bootstrap.min.css\">");
        pw.write("</head>");
    }

    public static void beginWriteBody(PrintWriter pw) {
        pw.write("<body>");
    }

    public static void endWriteBody(PrintWriter pw) {
        pw.write("</body>");
    }

    public static void endWrite(PrintWriter pw) {
        pw.write("</html>");
    }

}
