package cis555.indexer;
import java.io.File;

public class Test {
    public static void main(String... args) throws Exception {
        Indexer indexer = Indexer.getInstance(new File("wiki.html"),
                "https://en.wikipedia.org/wiki/Main_Page", 0, "text/html");
        indexer.parse();
        indexer.displayResult();
    }
}
