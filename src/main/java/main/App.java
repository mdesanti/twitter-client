package main;

import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.twitter.hbc.core.Client;

public class App {
	
	private static Charset CHARSET = Charset.forName("ISO-8859-1");

	public static void main(String[] args) throws InterruptedException {
		final BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
		final Client client = new TwitterClientBuilder()
								.withKeysInFile("twitter-keys.properties")
								.withQueue(queue)
								.tracking("#Elecciones2013")
								.build();
		
		client.connect();

		// Do whatever needs to be done with messages
		Thread t = new Thread(new Runnable() {

			public void run() {
				while (!client.isDone()) {
					String msg;
					try {
						msg = queue.take();
						JSONObject json = (JSONObject)new JSONParser().parse(msg);
						String text = new String(((String)json.get("text")).getBytes(), CHARSET);
						System.out.println(msg);
					} catch (InterruptedException e) {
						//interrupted!
					} catch (ParseException e) {
						//error!
					}
				}
			}
		});
		t.run();
		t.join();
		client.stop();

	}

}
