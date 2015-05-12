package cis555.PageRank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTesting {

    public static void main(String[] args) {
	String line = "830CFC21AF4D02C392AA6AD04E93B93E	E116CD383FE0665D31C0B4091B81A5BC;9AFA98884879C9C77BE86127288CC55A;5343F6DC09E457C233DA421420705391;5E4AD7673550368B65E54DAB3FDB4BD3;564B506AE8F7D5DBEFD0ADE0AACFB436";
	Pattern urlFromTo = Pattern.compile("^([A-F0-9]{32})\t(.*)");
	// Pattern urlFromTo = Pattern.compile("^([^\t]+)\t(.*)");
	Matcher urlMatcher = urlFromTo.matcher(line);
	if (urlMatcher.matches()) {
	    System.out.println("Success");
	    System.out.println(urlMatcher.group(1));
	    System.out.println(urlMatcher.group(2));
	}
    }
}
