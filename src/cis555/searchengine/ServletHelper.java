package cis555.searchengine;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;

import cis555.searchengine.utils.DocIDContentInfo;
import cis555.searchengine.utils.WeightedDocID;
import cis555.utils.FastTokenizer;

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

    public static String getPreview(WeightedDocID w) {
        StringBuilder sb = new StringBuilder();
        DocIDContentInfo docIDContent = ContentDAO.getContentInfo(w.getDocID());
        try {
            String content = "";
            if (docIDContent != null) {
                switch (docIDContent.getType()) {
                case "pdf":
                    PDDocument document = PDDocument
                            .load(new ByteArrayInputStream(docIDContent
                                    .getContent()));
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setStartPage(1);
                    stripper.setEndPage(Integer.MAX_VALUE);
                    content = stripper.getText(document);
                    document.close();
                    break;
                case "html":
                    content = Jsoup
                            .parse(new ByteArrayInputStream(
                                    docIDContent.getContent()),
                                    Charset.defaultCharset().name(), "").body()
                            .text();
                    break;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    int ch;
                    while ((ch = new ByteArrayInputStream(
                            docIDContent.getContent()).read()) != -1) {
                        stringBuilder.append((char) ch);
                    }
                    content = stringBuilder.toString();
                    break;
                }

                FastTokenizer tokenizer = new FastTokenizer(content);
                int start = w.getPreviewStartPos();
                int end = w.getPreviewEndPos();
                int index = 0;
                while (tokenizer.hasMoreTokens()) {
                    if ((index >= start) && (index <= end)) {
                        sb.append(tokenizer.nextToken());
                        sb.append(" ");
                    } else {
                        tokenizer.nextToken();
                    }
                    index++;
                    if (index > end)
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sb.length() > 0)
            return "...... " + sb.toString() + " ......";
        return "No Preview is Available.";
    }

}
