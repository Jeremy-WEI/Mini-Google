package cis555.indexer;
import java.net.URL;
import java.net.URLConnection;

public class Test {
    public static void main(String... args) throws Exception {

        URL url = new URL("http://google.com");
        URLConnection conn = url.openConnection();
        System.out.println(conn.getURL());
    }
}
