package com.dragontek.mygpoclient.pub;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.dragontek.mygpoclient.simple.IPodcast;

public class PublicClientTest extends TestCase {

	PublicClient client;
	public PublicClientTest(String name)
	{
		super(name);
		client = new PublicClient();
		
	}
	
	@Test
	public void testGetClientConfig() throws Exception {
		ClientConfig config = client.getConfiguration();
		assertNotNull(config);
		assertNotNull(config.mygpo);
		assertNotNull(config.mygpo_feedservice);
		
		System.out.println( "mygpo: " + config.mygpo.get("baseurl") );
		System.out.println( "mygpo_feedservice: " +config.mygpo_feedservice.get("baseurl"));
		System.out.println( "update_timeout: " +config.update_timeout );
		
		
	}
	
	@Test
	public void testGetTopList() throws Exception {
		
		List<IPodcast> podcasts = client.getToplist();
		assertNotNull(podcasts); // new ArrayList() is never null!
		assertEquals(25, podcasts.size());
		
		for(IPodcast podcast : podcasts)
		{
			System.out.println(podcast.getUrl());
		}
		
	}
	
	@Test
	public void testSearchPodcast() throws Exception {
		
		List<IPodcast> podcasts = client.searchPodcast("Linux");
		assertNotNull(podcasts);
		for(IPodcast podcast : podcasts)
		{
			System.out.println(podcast.getUrl());
		}
		
	}
}
