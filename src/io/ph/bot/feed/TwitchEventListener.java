package io.ph.bot.feed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import net.dean.jraw.http.NetworkException;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Reddit event listener
 * @author Paul
 */
@SuppressWarnings("unchecked")
public class TwitchEventListener implements Job {
	private static File serializedFile = new File("resources/feeds/Twitch.bin");
	private static Logger log = LoggerFactory.getLogger(TwitchEventListener.class);

	private static Map<String, List<TwitchFeedObserver>> twitchFeed = new HashMap<>();
	// Holds status of twitch users, a change will fire updates
	private static Map<String, Boolean> twitchOnlineStatus = new HashMap<>();

	private static boolean firstStartup = true;

	public static final String ENDPOINT = "https://api.twitch.tv/kraken/";

	/**
	 * Add a twitch userID to the feed
	 * @param userId UserID of the twitch streamer
	 * @param observer Observer to add
	 */
	public static void addTwitchFeed(String userId, TwitchFeedObserver observer) {
		userId = userId.toLowerCase();
		if (!twitchFeed.containsKey(userId)) {
			ArrayList<TwitchFeedObserver> toAdd = new ArrayList<TwitchFeedObserver>();
			toAdd.add(observer);
			twitchFeed.put(userId, toAdd);
		} else {
			twitchFeed.get(userId).add(observer);
		}
	}

	/**
	 * Remove a twitch feed by two params
	 * @param userId ID of twitch user to remove
	 * @param guild Guild to remove from
	 * @return True if removed, false if the feed never existed
	 */
	public static boolean removeTwitchFeed(String userId, Guild guild) {
		userId = userId.toLowerCase();
		TwitchFeedObserver observer;
		if ((observer = getObserver(userId, guild)) == null)
			return false;
		twitchFeed.get(userId).remove(observer);
		saveFeed();
		return true;
	}

	/**
	 * Get observer from two params
	 * @param userId Twitch user ID
	 * @param guild Guild
	 * @return TwitchFeedObserver if found, null if not
	 */
	public static TwitchFeedObserver getObserver(String userId, Guild guild) {
		userId = userId.toLowerCase();
		if (!twitchFeed.containsKey(userId)) {
			return null;
		}
		for (TwitchFeedObserver observer : twitchFeed.get(userId)) {
			if (observer.getDiscoChannel().getGuild().equals(guild))
				return observer;
		}
		return null;
	}

	/**
	 * Update and check all registered twitch channels
	 */
	public static void update() {
		for (Entry<String, Boolean> entry : twitchOnlineStatus.entrySet()) {
			try {
				HttpResponse<JsonNode> json = Unirest.get(ENDPOINT + "streams/" + entry.getKey())
						.header("Accept", "application/vnd.twitchtv.v5+json")
						.header("Client-ID", Bot.getInstance().getApiKeys().get("twitch"))
						.asJson();
				boolean currentStatus = !json.getBody().getObject().isNull("stream");
				if (entry.getValue() != currentStatus) {
					if (!firstStartup) {
						twitchFeed.get(entry.getKey())
						.removeIf(observer -> !observer.trigger(entry.getKey(), json.getBody().getObject()));
					} else {
						firstStartup = false;
					}
					twitchOnlineStatus.put(entry.getKey(), currentStatus);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (NoAPIKeyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if (Bot.DEBUG)
			log.info("Twitch job executing...");
		update();
	}

	/**
	 * Serialize the feeds
	 */
	public static void saveFeed() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializedFile));
			oos.writeObject(twitchFeed);
			oos.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, List<TwitchFeedObserver>> getFeed() {
		return twitchFeed;
	}

	static {
		try {
			Bot.getInstance().getApiKeys().get("twitch");
			if (!serializedFile.createNewFile()) {
				serializedFile.getParentFile().mkdirs();
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedFile));
				twitchFeed = (Map<String, List<TwitchFeedObserver>>) ois.readObject();
				ois.close();
				for (String userId : twitchFeed.keySet()) {
					twitchOnlineStatus.put(userId, false);
				}
			} else {
				saveFeed();
			}
		} catch (NoAPIKeyException e) {
			e.printStackTrace();
			log.warn("Twitch API keys not set. Cannot perform live Twitch updates");
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, List<TwitchFeedObserver>> getRedditFeed() {
		return twitchFeed;
	}

	/**
	 * Resolve a Twitch.tv User ID from a Username
	 * @param username Username to check for
	 * @return Twitch.tv user ID
	 * @throws IllegalArgumentException Bad username
	 * @throws UnirestException Unirest exception
	 * @throws NoAPIKeyException No API keys set
	 */
	public static String resolveUserIdFromUsername(String username) throws IllegalArgumentException,
	UnirestException, NoAPIKeyException {
		HttpResponse<JsonNode> json = Unirest.get(TwitchEventListener.ENDPOINT + "users?login=" + username)
				.header("Accept", "application/vnd.twitchtv.v5+json")
				.header("Client-ID", Bot.getInstance().getApiKeys().get("twitch"))
				.asJson();
		if (json.getBody().getObject().getInt("_total") == 0) {
			throw new IllegalArgumentException("The username " + username + " is not a valid Twitch.tv account");
		}
		return json.getBody().getObject().getJSONArray("users").getJSONObject(0).getString("_id");
	}
}
