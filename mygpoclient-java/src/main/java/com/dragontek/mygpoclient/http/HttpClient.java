package com.dragontek.mygpoclient.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import com.dragontek.mygpoclient.Global;

public class HttpClient extends DefaultHttpClient {
	
	HttpHost _targetHost;
	//BasicHttpContext _localContext = new BasicHttpContext();
	protected String _authToken;
	
	public HttpClient()
	{
		this(null, null, Global.HOST);
	}
	public HttpClient(String host)
	{
		this(null, null, host);
	}
	public HttpClient(String username, String password)
	{
		this(username, password, Global.HOST);
	}
	public HttpClient(String username, String password, String host)
	{
		this(username, password, host, false);
	}
	public HttpClient(String username, String password, String host, boolean ssl)
	{
		if(ssl)
			_targetHost = new HttpHost(host, 443, "https");
		else
			_targetHost = new HttpHost(host);
		
		if(username != null && password!= null)
			getCredentialsProvider().setCredentials(new AuthScope(_targetHost.getHostName(), _targetHost.getPort()), new UsernamePasswordCredentials(username, password));
	}
	
	protected HttpRequest prepareRequest(String method, String uri, HttpEntity entity) throws UnsupportedEncodingException
	{
		
		HttpRequest request = new HttpGet(uri);
		if(_authToken != null)
		{
			// TODO: Set-Cookie?
			//request.set
		}
		if(method == "POST")
		{
			request = new HttpPost(uri);
			((HttpPost)request).setEntity(entity);
		}
		else if(method == "PUT")
		{
			request = new HttpPut(uri);
			((HttpPut)request).setEntity(entity);
		}
		request.addHeader("User-Agent", Global.USER_AGENT);
		return request;
	}
	
	protected String processResponse(HttpResponse response) throws IllegalStateException, IOException
	{
		String result = null;			
		HttpEntity entity = response.getEntity();
		
		if (entity != null) {
			InputStream instream = entity.getContent();
			result= convertStreamToString(instream);
			instream.close();
		}
		return result;
	}
	
	public String getAuthToken()
	{
		return this._authToken;
	}
	public void setAuthToken(String token)
	{
		this._authToken = token;
	}
	
	protected String request(String method, String uri, HttpEntity data) throws ClientProtocolException, IOException
	{
		HttpRequest request = prepareRequest(method, uri, data);
		HttpResponse response = execute(_targetHost, request);
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		
		if(Global.DEBUG)
		{
			System.out.println("HOST: " + _targetHost);
			System.out.println(String.format("%s: %s", method, uri));
			if(data != null)
			{
				System.out.println("DATA:");
				data.writeTo(System.out);
				System.out.println();
			}
			for(Header h : request.getAllHeaders())
			{
				System.out.println(String.format("HEADER: %s: %s", h.getName(), h.getValue()));
			}
			for(Cookie c : this.getCookieStore().getCookies())
			{
				if(c.getName().equals("sessionid"));
					_authToken = c.getValue();
					
				System.out.println(String.format("COOKIE: %s: %s -- %s", c.getName(), c.getValue(), c.getDomain()));
				
			}
			System.out.println(response.getStatusLine());
			//System.out.println(response);
		}

		StatusLine s = response.getStatusLine();
		if(s.getStatusCode() == 200)
		{
			return (String)processResponse(response);
		} else {
			System.out.println("ERROR: " + s.getReasonPhrase());
			throw new HttpResponseException(s.getStatusCode(), s.toString());
		}
	}
	
	
	public String GET(String uri) throws ClientProtocolException, IOException {
		return request("GET", uri, null);
	}
	
	public String POST(String uri, HttpEntity data) throws ClientProtocolException, IOException
	{
		return request("POST", uri, data);
	}
	public String PUT(String uri, HttpEntity data) throws ClientProtocolException, IOException
	{
		return request("PUT", uri, data);
	}
	
	public static String convertStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} finally {
			try {
				is.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}