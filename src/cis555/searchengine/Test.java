package cis555.searchengine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Test {
    public static void main(String... args) throws MalformedURLException,
            IOException {
//        Document document = Jsoup.parse(new URL(
//                "http://www.bbc.com/autos/story/20150501-from-denmark"), 3000);
//        Elements links = document.select("img[src]");
//        for (Element link : links) {
//            System.out.println(link.attr("abs:src"));
//        }
//        links = document.select("a[href]");
//        for (Element link : links) {
//            System.out.println(link.attr("abs:href"));
//        }
        for (int  i = 0; i < 10000; i++) {
            new Date();
        }
        URL url = null;
    }
}
