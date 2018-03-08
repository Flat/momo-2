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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Guild;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@SuppressWarnings("unchecked")
public class TwitterEventListener {
	private static File serializedFile = new File("resources/feeds/Twitter.bin");
	// twitterClient is null if no API keys set
	public static Twitter twitterClient;
	static Configuration config;
	static FilterQuery filter = new FilterQuery();
	static TwitterStream twitterStream;

	private static long lastChange;
	public static final long DELAY = 60;

	private static Map<Long, List<TwitterFeedObserver>> twitterFeed = new HashMap<Long, List<TwitterFeedObserver>>();

	public static void initTwitter() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		try {
			cb.setDebugEnabled(true)
			.setOAuthConsumerKey(Bot.getInstance().getApiKeys().get("twitterappkey"))
			.setOAuthConsumerSecret(Bot.getInstance().getApiKeys().get("twitterappsecret"))
			.setOAuthAccessToken(Bot.getInstance().getApiKeys().get("twittertokenkey"))
			.setOAuthAccessTokenSecret(Bot.getInstance().getApiKeys().get("twittertokensecret"));
			config = cb.build();
			TwitterFactory tf = new TwitterFactory(config);
			twitterClient = tf.getInstance();
			if(!serializedFile.createNewFile()) {
				serializedFile.getParentFile().mkdirs();
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedFile));
				twitterFeed = (Map<Long, List<TwitterFeedObserver>>) ois.readObject();
				ois.close();
				if(!twitterFeed.keySet().isEmpty())
					update();
			} else {
				saveFeed();
			}
		} catch (NoAPIKeyException e) {
			LoggerFactory.getLogger(TwitterEventListener.class).warn("No Twitter API Keys. Twitter feeds unavailable");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a twitterId to the feed with given observer
	 * If it does not already exist, trigger a queue and return seconds until addition
	 * @param twitterId TwitterID to add
	 * @param observer Observer to associate
	 * @return 1-60 if queued, -1 if already exists
	 */
	public static int addTwitterFeed(long twitterId, TwitterFeedObserver observer) {
		if(!twitterFeed.containsKey(twitterId)) {
			ArrayList<TwitterFeedObserver> toAdd = new ArrayList<TwitterFeedObserver>();
			toAdd.add(observer);
			twitterFeed.put(twitterId, toAdd);
			saveFeed();
			return queue(twitterId);
		} else {
			twitterFeed.get(twitterId).add(observer);
			saveFeed();
			return -1;
		}
	}

	/**
	 * Remove based on two params
	 * @param twitterId Twitter ID
	 * @param guild Guild to remove from
	 * @return True if removed, false if doesn't exist
	 */
	public static boolean removeTwitterFeed(long twitterId, Guild guild) {
		TwitterFeedObserver observer;
		if((observer = getObserver(twitterId, guild)) == null)
			return false;
		twitterFeed.get(twitterId).remove(observer);
		saveFeed();
		return true;
	}

	/**
	 * Get observer for this twitterId and guild
	 * @param twitterId Twitter ID to search
	 * @param guild Guild
	 * @return Observer if found, null if either twitterId doesn't exist or observer doesn't exist in twitterId
	 */
	public static TwitterFeedObserver getObserver(long twitterId, Guild guild) {
		if(!twitterFeed.containsKey(twitterId)) {
			return null;
		}
		for(TwitterFeedObserver observer : twitterFeed.get(twitterId)) {
			if(observer.getDiscoChannel().getGuild().equals(guild))
				return observer;
		}
		return null;
	}

	//private static Set<Long> queuedAdd = new HashSet<Long>();
	/**
	 * Update the filters with contents of queuedAdd + previous keys of twitterFeed
	 */
	public static void update() {
		long[] list = new long[twitterFeed.keySet().size()];
		Iterator<Long> iter = twitterFeed.keySet().iterator();
		int i = 0;
		while(iter.hasNext()) {
			list[i] = iter.next();
			i++;
		}
		if(twitterStream == null) {
			createNewStatusListener(config);
		}
		filter.follow(list);
		twitterStream.filter(filter);
	}

	public static void saveFeed() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializedFile));
			oos.writeObject(twitterFeed);
			oos.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Queue to add a twitter ID to the filter
	 * @param twitterId Twitter ID to add
	 * @return Seconds until this takes effect
	 */
	public static int queue(long twitterId) {
		if(!updateQueued()) {
			lastChange = System.currentTimeMillis();
			Util.setTimeout(() -> update(), (int) (DELAY * 1000), true);
			return (int) DELAY;
		}
		return (int) (DELAY - (sinceLastChange() / 1000));
	}

	public static Map<Long, List<TwitterFeedObserver>> getFeed() {
		return twitterFeed;
	}
	
	/**
	 * Initialize the Twitter status stream
	 * @param config Predone configuration
	 */
	static void createNewStatusListener(Configuration config) {		
		StatusListener listener = new StatusListener(){
			@Override
			public void onStatus(Status status) {
				if(twitterFeed.containsKey(status.getUser().getId())) {
					if(twitterFeed.get(status.getUser().getId()).isEmpty()) {
						twitterFeed.remove(status.getUser().getId());
						saveFeed();
						return;
					}
					Iterator<TwitterFeedObserver> iter = twitterFeed.get(status.getUser().getId()).iterator();
					while(iter.hasNext()) {
						TwitterFeedObserver observer = iter.next();
						if(!observer.trigger(status)) {
							iter.remove();
							saveFeed();
						}
					}
				}
			}
			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }
			@Override
			public void onScrubGeo(long arg0, long arg1) { }
			@Override
			public void onStallWarning(StallWarning arg0) { }
		};
		twitterStream = new TwitterStreamFactory(config).getInstance();
		twitterStream.addListener(listener);
	}

	private static boolean updateQueued() {
		return sinceLastChange() < (DELAY * 1000);
	}

	/**
	 * How long ago was the last edit
	 * @return Difference in milliseconds
	 */
	public static long sinceLastChange() {
		return System.currentTimeMillis() - lastChange;
	}
	
	public static Map<Long, List<TwitterFeedObserver>> getTwitterList() {
		return twitterFeed;
	}
}
