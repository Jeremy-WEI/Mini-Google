package cis555.searchengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.text.WordUtils;

import cis555.searchengine.utils.PreviewTokenizer;
import cis555.searchengine.utils.WeightedDocID;

import com.amazonaws.util.json.JSONObject;

public class ServletHelper {
    public static void prepareWrite(PrintWriter pw, String contextPath,
            String title, ServletContext sc) {
        pw.write("<!DOCTYPE html><html>");
        pw.write("<head>");
        pw.write("<title>" + title + "</title>");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + contextPath + "/stylesheet/bootstrap.min.css\">");
        pw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + contextPath + "/stylesheet/main.css\">");
        addAjax(pw, sc);
        pw.write("</head>");
        pw.write("<body style=\"overflow:scroll;\">");
        pw.write("<div class=\"navbar navbar-default navbar-fixed-top\">");
        pw.write("<div class=\"container\">");
        pw.write("<div class=\"navbar-header\">");
        pw.write("<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\".navbar-collapse\">");
        pw.write("<span class=\"icon-bar\">");
        pw.write("</span>");
        pw.write("<span class=\"icon-bar\">");
        pw.write("</span>");
        pw.write("<span class=\"icon-bar\">");
        pw.write("</span>");
        pw.write("</button>");
        pw.write("<a class=\"navbar-brand\" href=\"/searchengine/\"><b>Mini-Search</b></a>");
        pw.write("</div>");
        pw.write("</div>");
        pw.write("</div>");
    }

    public static void addAjax(PrintWriter pw, ServletContext sc) {
        pw.write("<script src=\"http://code.jquery.com/jquery-1.7.js\" type=\"text/javascript\"></script>");
        pw.write("<script src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js\" type=\"text/javascript\"></script>");
        pw.write("<link href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css\" rel=\"stylesheet\" type=\"text/css\" />");
        pw.write("<STYLE TYPE=\"text/css\" media=\"all\">");
        pw.write(".ui-autocomplete {position: absolute; cursor: default; height: 200px; overflow-x: hidden;}");
        pw.write("</STYLE>");

        // try(BufferedReader br = new BufferedReader(new FileReader(new
        // File(sc.getResource("/script/ajax_script.txt").getPath())))) {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(
                "webapps/searchengine/script/ajax_script.txt")))) {

            String line = br.readLine();
            while (line != null) {
                pw.write(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void finishWrite(PrintWriter pw) {
        pw.write("</body>");
        pw.write("</html>");
    }

    public static void writePanel(PrintWriter pw, String panelHead,
            String panelBody, String className, boolean overFlowScroll) {
        pw.write("<div class=\"panel " + className + "\" style=\"\">");
        pw.write("<div class=\"panel-heading\">" + panelHead + "</div>");
        pw.write("<div class=\"panel-body\""
                + (overFlowScroll ? " style=\"height:100px;overflow:scroll;\">"
                        : ">"));
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

    public static String getPreview(WeightedDocID w, Set<String> words) {
        StringBuilder sb = new StringBuilder();
        try {
            String content = ContentDAO.getContent(w.getDocID());
            PreviewTokenizer tokenizer = new PreviewTokenizer(content);
            int start = w.getPreviewStartPos();
            int end = w.getPreviewEndPos();
            // sb.append("Start Index: " + start + "\n");
            // sb.append("End Index: " + end + "\n");
            int index = 0;
            while (tokenizer.hasMoreTokens()) {
                if ((index >= start) && (index <= end)) {
                    String word = tokenizer.nextToken();
                    boolean flag = false;
                    for (String tmp : words) {
                        if (tmp.length() > 1
                                && word.toLowerCase().startsWith(tmp)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        int indx = PreviewTokenizer.getNonLetterIndex(word);
                        sb.append("<mark><b>");
                        sb.append(word.substring(0, indx));
                        sb.append("</b></mark>");
                        sb.append(word.substring(indx));
                    } else
                        sb.append(word);
                    sb.append(" ");
                } else {
                    tokenizer.nextToken();
                }
                index++;
                if (index > end)
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sb.length() > 0)
            return "...... " + sb.toString() + " ......";
        return "No Preview is Available.";
    }

    public static String extractInfoFromWiki(String query) {
    	String titleCasedQuery = WordUtils.capitalizeFully(query);
        try {
            String url = "http://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles="
                    + URLEncoder.encode(titleCasedQuery, "UTF-8") + "&redirects&format=json&exintro=1";
            // System.out.println(url);
            
            URLConnection conn;
            conn = new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            JSONObject json = (JSONObject) ((JSONObject) new JSONObject(
                    sb.toString()).get("query")).get("pages");
            @SuppressWarnings("unchecked")
            Iterator<String> iter = json.keys();
            String key = null;
            while (iter.hasNext()) {
                key = iter.next();
                break;
            }
            return ((JSONObject) json.get(key)).getString("extract");
        } catch (Exception e) {
            // e.printStackTrace();
            return "";
        }
    }

}
