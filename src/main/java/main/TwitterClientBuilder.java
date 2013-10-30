package main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;

public class TwitterClientBuilder {
	
	private BlockingQueue<String> queue;
	
	private List<String> tracking = Lists.newArrayList();
	
	private String consumerKey;
	
	private String consumerSecret;
	
	private String token;
	
	private String secret;

	
	public TwitterClientBuilder withQueue(BlockingQueue<String> queue) {
		this.queue = queue;
		return this;
	}
	
	public TwitterClientBuilder tracking(String word) {
		tracking.add(word);
		return this;
	}
	
	public TwitterClientBuilder tracking(List<String> words) {
		tracking.addAll(words);
		return this;
	}
	
	private TwitterClientBuilder withConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
		return this;
	}
	
	private TwitterClientBuilder withConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
		return this;
	}
	
	private TwitterClientBuilder withToken(String token) {
		this.token = token;
		return this;
	}
	
	private TwitterClientBuilder withSecret(String secret) {
		this.secret = secret;
		return this;
	}
	
	public TwitterClientBuilder withKeysInFile(String name) {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream(name));
			withConsumerKey(properties.getProperty("consumerKey"));
			withConsumerSecret(properties.getProperty("consumerSecret"));
			withSecret(properties.getProperty("secret"));
			withToken(properties.getProperty("token"));
		} catch (IOException e) {
			System.err.println("File not found!");
		}
		
		return this;
	}
	
	public Client build() {
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
		// add some track terms
		if(!tracking.isEmpty()) {
			endpoint.trackTerms(tracking);
		}

		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
		// Authentication auth = new BasicAuth(username, password);
		
		// Create a new BasicClient. By default gzip is enabled.
		Client client = new ClientBuilder()
						.hosts(Constants.STREAM_HOST)
						.endpoint(endpoint)
						.authentication(auth)
						.processor(new StringDelimitedProcessor(queue))
						.build();
		
		return client;
	}
}
