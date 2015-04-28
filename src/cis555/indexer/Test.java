package cis555.indexer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import cis555.utils.UrlDocIDMapper;
import cis555.utils.Utils;

public class Test {
    public static void main(String... args) throws IOException {
        byte[] content = Utils.unzip(new File("input/1.html.gzip"));
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        UrlDocIDMapper db = new UrlDocIDMapper("test");
        db.start();
        Indexer indexer = Indexer.getInstance(bais, "", 1, "html", db);
        indexer.getWordByIndex(Hit.getHitPos(758));
    }
}
