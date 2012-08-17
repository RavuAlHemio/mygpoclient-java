package com.dragontek.mygpoclient.feeds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.dragontek.mygpoclient.http.HttpClient;
import com.dragontek.mygpoclient.json.JsonClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GoogleReaderAPI extends HttpClient {

	private String mActionToken = null;
	private String BASE_API_URI = "http://www.google.com/reader/api/0/";
	
	public final static String CURRENT_USER = "user/-/";
	public final static String STATE_READ = "state/com.google/read";
	public final static String STATE_STARRED = "state/com.google/starred";
	public final static String STATE_KEPT_UNREAD = "state/com.google/kept-unread";
	public final static String STATE_FRESH = "state/com.google/fresh";
	
	public void requestActionToken() throws AuthenticationException, IOException
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("client", "Podder"));
		mActionToken =  GET(BASE_API_URI + "token"); 
	}
	public void mark(List<String> ids, String state) throws AuthenticationException, IOException
	{
		mark(ids, state, null);
	}	

	public void mark(List<String> ids, String state, String feedUrl) throws AuthenticationException, IOException
	{
        if(mActionToken == null)
        	requestActionToken();
        if(ids.isEmpty())
        	return;
        
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for(String id : ids)
        {
    		nameValuePairs.add(new BasicNameValuePair("i", id));
        }
        if(feedUrl != null) // Feed Url is not required, but makes the call more efficient
        	nameValuePairs.add(new BasicNameValuePair("s", "feed/" + feedUrl));
        nameValuePairs.add(new BasicNameValuePair("T", mActionToken));
        nameValuePairs.add(new BasicNameValuePair("a", CURRENT_USER + state));
        
        POST(BASE_API_URI + "edit-tag", new UrlEncodedFormEntity(nameValuePairs));
	}
	public void unMark(List<String> ids, String state) throws AuthenticationException, IOException
	{
		unMark(ids, state, null);
	}
	public void unMark(List<String> ids, String state, String feedUrl) throws AuthenticationException, IOException
	{
        if(mActionToken == null)
        	requestActionToken();

        if(ids.isEmpty())
        	return;
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for(String id : ids)
        {
        	nameValuePairs.add(new BasicNameValuePair("i", id));
        }
        if(feedUrl != null) // Feed Url is not required, but makes the call more efficient
        	nameValuePairs.add(new BasicNameValuePair("s", "feed/" + feedUrl));
        
        nameValuePairs.add(new BasicNameValuePair("T", mActionToken));
        nameValuePairs.add(new BasicNameValuePair("r", CURRENT_USER + state));
        
        POST(BASE_API_URI + "edit-tag", new UrlEncodedFormEntity(nameValuePairs));
	}
	
	public GoogleFeed parse(String url) throws AuthenticationException, IOException
	{
		
		Gson gson = new Gson();
		//Log.i(this.toString(), "GoogleFeed Parsing: " + url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
       	// TODO: Optimize with since timestamp and no comments 
		//nameValuePairs.add(new BasicNameValuePair("ot", timestamp));
		nameValuePairs.add(new BasicNameValuePair("comments", "false"));
		nameValuePairs.add(new BasicNameValuePair("likes", "false"));
		String response = GET(BASE_API_URI + "stream/contents/feed/" +  URLEncoder.encode(url, "UTF-8"));
		return gson.fromJson(response, GoogleFeed.class);
	}
	public void putSubscription(String feedUrl, String label) throws AuthenticationException, UnsupportedEncodingException, IOException
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
       	nameValuePairs.add(new BasicNameValuePair("s", "feed/" + feedUrl));
        nameValuePairs.add(new BasicNameValuePair("ac", "subscribe"));
        nameValuePairs.add(new BasicNameValuePair("a", "user/-/label/" + label));
        //nameValuePairs.add(new BasicNameValuePair("title", title));
        nameValuePairs.add(new BasicNameValuePair("T", mActionToken));
        
        POST(BASE_API_URI + "subscription/edit", new UrlEncodedFormEntity(nameValuePairs));
		
	}
	public void setLabel(String feedUrl, String label) throws AuthenticationException, UnsupportedEncodingException, IOException
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
       	nameValuePairs.add(new BasicNameValuePair("s", "feed/" + feedUrl));
        nameValuePairs.add(new BasicNameValuePair("ac", "edit"));
        nameValuePairs.add(new BasicNameValuePair("a", "user/-/label/" + label));
        //nameValuePairs.add(new BasicNameValuePair("title", title));
        nameValuePairs.add(new BasicNameValuePair("T", mActionToken));
        
        POST(BASE_API_URI + "subscription/edit", new UrlEncodedFormEntity(nameValuePairs));
		
	}
	public List<String> getSubscriptions(String label) throws AuthenticationException, IOException
	{
        //if(mActionToken == null)
        //	requestActionToken();

        //List<String> subscriptions = new ArrayList<String>();
		String response = GET(BASE_API_URI + "subscription/list?output=json");
		Gson gson = new Gson();
		Type collectionType = new TypeToken<ArrayList<String>>(){}.getType();
		return gson.fromJson(response, collectionType);
		
		/*
		JSONObject result = new JSONObject(response);
		JSONArray jsonSubscriptions = result.getJSONArray("subscriptions");
		for(int s=0; s < jsonSubscriptions.length(); s++)
		{
			JSONObject jsonSubscription = jsonSubscriptions.getJSONObject(s);
			JSONArray categories = jsonSubscription.getJSONArray("categories");
			for(int c=0; c < categories.length(); c++)
			{
				JSONObject category = categories.getJSONObject(c);
				String feedLabel = category.getString("label");
				if(feedLabel.equals(label))
				{
					String feedUrl =  jsonSubscription.getString("id").replaceFirst("feed/", "");
					subscriptions.add(feedUrl);
				}
			}
		}
		*/
		//return subscriptions;
	}
	
	
    public static class GoogleFeed
    {
        public String id;
    	public String title;
        public String description;
        public List<Alternate> alternate;
        public List<Item> items;
    }
   
   
    public static class Item
    {
        public String id;
        public List<Enclosure> enclosure;
        public List<Alternate> alternate;
		public String title;
		public Content content;
		public Content summary;
		public String published;
		public String updated;
		public String crawlTimeMsec;
        public List<String> categories;
        public boolean isState(String state)
        {
        	for(String category : categories)
        	{
        		if(category.endsWith(state))
        			return true;
        	}
        	return false;
        }
        
        public Enclosure getFirstEnclosure()
        {
        	if(enclosure != null && enclosure.size() > 0)
        		return enclosure.get(0);
        	else
        		return null;
        }
    }
    public class Content
    {
    	public String direction;
    	public String content;
    }
    public class Summary
    {
    	public String direction;
    	public String content;
    }
    public class Alternate
    {
    	public String href;
    	public String type;
    }
    public class Enclosure
    {
    	public String href;
    	public String type;
    	public String length;
    }
}