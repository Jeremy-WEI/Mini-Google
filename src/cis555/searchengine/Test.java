package cis555.searchengine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

import com.amazonaws.util.json.JSONException;

public class Test {

    public static void main(String... args) throws MalformedURLException,
            IOException, JSONException {
        File f = new File("6CC3F1423DA9819753E869418E39CD14.xml.gzip");
        String fileName = f.getName();
        String docID = fileName.substring(0, fileName.indexOf('.'));
        String type = fileName.substring(fileName.indexOf('.') + 1,
                fileName.lastIndexOf('.'));
        byte[] rawContent = Utils.unzip(f);
        byte[] realContent = new byte[rawContent.length
                - CrawlerConstants.MAX_URL_LENGTH * 2];
        System.arraycopy(rawContent, CrawlerConstants.MAX_URL_LENGTH * 2,
                realContent, 0, rawContent.length
                        - CrawlerConstants.MAX_URL_LENGTH * 2);
        String content = "";
        switch (type) {
        case "pdf":
            PDDocument document = PDDocument.load(new ByteArrayInputStream(
                    realContent));
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Integer.MAX_VALUE);
            content = stripper.getText(document);
            document.close();
            break;
        case "html":
            content = Jsoup
                    .parse(new ByteArrayInputStream(realContent),
                            Charset.defaultCharset().name(), "").body().text();
            break;
        default:
            StringBuilder stringBuilder = new StringBuilder();
            int ch;
            while ((ch = new ByteArrayInputStream(realContent).read()) != -1) {
                stringBuilder.append((char) ch);
            }
            content = stringBuilder.toString();
            break;
        }
        System.out.println(content);
    }
}
