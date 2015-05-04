package cis555.searchengine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;

import com.sleepycat.persist.EntityCursor;

public class AjaxRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public AjaxRequestServlet() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try (PrintWriter out = response.getWriter();) {
			response.setContentType("text/html");
			response.setHeader("Cache-control", "no-cache, no-store");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "-1");
			
			
			List<String> suggestList = new ArrayList<String>();
			String query = request.getParameter("term");
			System.out.println("query: " + query);
			
			String[] words = query.trim().toLowerCase().split("\\s+");
			int last = words.length - 1;
			int suggestCount = 0;
			String targetWord = words[last];
			StringBuilder prefixBuilder = new StringBuilder();
			
			for (int i = 0; i < last; i++) {
				prefixBuilder.append(words[i]).append(" ");
			}
			
			String prefix = prefixBuilder.toString();
			
			@SuppressWarnings("unchecked")
			TreeSet<String> wordSet = (TreeSet<String>) getServletContext().getAttribute("wordSet");
			int length = targetWord.length();
			
			String endWord = targetWord.substring(0, length-1) + (char)(targetWord.charAt(length-1)+1);
			for (String word: wordSet.subSet(targetWord, endWord)) {
				if (suggestCount > 5) break;
				
				suggestList.add(prefix + word);
				suggestCount++;
			}
	
			String jsonText = JSONValue.toJSONString(suggestList);
			System.out.println("suggestions: " + jsonText);
		  
			out.println(jsonText);
		}
		
	}

}