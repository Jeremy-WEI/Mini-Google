package cis555.searchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Test {

    private static String extractInfoFromWiki(String query) {
        String url = "http://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles="
                + URLEncoder.encode(query) + "&format=json&exintro=1";
        System.out.println(url);
        URLConnection conn;
        try {
            conn = new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            JSONObject json = (JSONObject) ((JSONObject) new JSONObject(
                    sb.toString()).get("query")).get("pages");
            Iterator<String> iter = json.keys();
            String key = null;
            while (iter.hasNext()) {
                key = iter.next();
                break;
            }
            return ((JSONObject) json.get(key)).getString("extract");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static void main(String... args) throws MalformedURLException,
            IOException, JSONException {
        // Document document = Jsoup.parse(new URL(
        // "http://www.bbc.com/autos/story/20150501-from-denmark"), 3000);
        // Elements links = document.select("img[src]");
        // for (Element link : links) {
        // System.out.println(link.attr("abs:src"));
        // }
        // links = document.select("a[href]");
        // for (Element link : links) {
        // System.out.println(link.attr("abs:href"));
        // }

        System.out.println(extractInfoFromWiki("Computer"));
        // System.out.println(URLEncoder.encode(s,
        // enc).encode("United States"));
        // JSONObject json = new JSONObject(
        // "http://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles=Threadless&exintro=1");
        // System.out.println(json.get("extract"));
    }
}
