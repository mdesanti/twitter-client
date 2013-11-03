package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.twitter.hbc.core.Client;

public class App {

	private static Charset CHARSET = Charset.forName("ISO-8859-1");
	private static int filenr = 0;
	private static Logger logger = Logger.getLogger("twitter-client");

	public static void main(String[] args) throws InterruptedException {

		final BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
		final Client client = new TwitterClientBuilder().withKeysInFile("twitter-keys.properties").withQueue(queue).tracking("#PonelePeteALaPelicula").build();

		client.connect();

		Thread t = new Thread(new Runnable() {

			public void run() {
				while (!client.isDone()) {
					// wait 5 seconds for the queue to get some tweets
					try {
						Thread.sleep(5000);
						List<String> tweets = Lists.newArrayList();
						queue.drainTo(tweets, 50);
						if(!tweets.isEmpty()) {
							PrintWriter writer = new PrintWriter("tweets/tweets" + filenr + ".txt", "UTF-8");
							for (String str : tweets) {
								JSONObject json = (JSONObject) new JSONParser().parse(str);
								String text = new String(((String) json.get("text")).getBytes(), CHARSET);
								writer.println(text);
							}
							logger.log(Level.INFO, "Wrote file tweets" + filenr + ".txt");
							filenr++;
							writer.close();
						}
					} catch (ParseException e) {
						// error!
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						// error!
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// error!
						e.printStackTrace();
					} catch (InterruptedException e) {
						// error!
						e.printStackTrace();
					}
				}
			}
		});
		t.run();
		t.join();
		client.stop();

	}
}
