package cis555.indexer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import cis555.utils.CrawlerConstants;
import cis555.utils.Utils;

public class Test {
    public static void main(String... args) throws IOException {
        byte[] content = Utils.unzip(new File(
                "input/05BB204EA754B03F0CA2BA61F42E0B0B.pdf.gzip"));
        ByteArrayInputStream bais = new ByteArrayInputStream(content,
                CrawlerConstants.MAX_URL_LENGTH * 2, content.length);
        String URL = Utils.getURL(content);
        Indexer indexer = Indexer.getInstance(bais, URL,
                Utils.hashUrlToHexStringArray(URL), "pdf");
        // indexer.parse();
        // indexer.displayResult();
        System.out.println(indexer.getWordByIndex(Hit.getHitPos(1492)));
        // System.out.println(indexer.content);
        // System.out.println("asdsadsadas".getBytes().length);
    }
}
