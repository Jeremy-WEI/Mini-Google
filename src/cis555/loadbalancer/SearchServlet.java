package cis555.loadbalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {
	
	private List<String> iPPort;
	private Incrementor incrementor;
	
	public SearchServlet(){
		this.iPPort = new ArrayList<String>();
		this.incrementor = new Incrementor();
	}
	
	@Override
	public void init(){
		String[] ipPorts = getInitParameter("ipPorts").split(";");
		for (String addresses : ipPorts){
			this.iPPort.add(addresses);
		}
	}
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        String query = request.getParameter("query");
        
        int serverNumber = this.incrementor.getCountAndIncrement() % this.iPPort.size();
        
        String cleansedQuery = URLEncoder.encode(query, "UTF-8");
        
        String requestUrl = "http://" + this.iPPort.get(serverNumber) + "/searchengine/search?query=" + cleansedQuery;
        URL url = new URL(requestUrl);
        
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);
        
        PrintWriter out = response.getWriter();

        BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
        String line;
        while ((line = br.readLine()) != null){
            out.write(line);        	
        }
    }
}
