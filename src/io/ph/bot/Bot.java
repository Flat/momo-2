package io.ph.bot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ph.bot.events.CustomEventDispatcher;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.listeners.Listeners;
import io.ph.bot.listeners.ModerationListeners;
import io.ph.bot.listeners.VoiceChannelListeners;
import io.ph.bot.scheduler.JobScheduler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * Singleton instance of the entire bot. Includes configuration and the main JDA singleton
 * @author Paul
 *
 */
public class Bot {
	private static final Bot instance;
	public Shards shards;
	private static ArrayList<JDA> jdaClients;
	private final static Logger logger = LoggerFactory.getLogger(Bot.class);

	// Sharding splits the connection gateways to Discord and splits servers
	// among the shards. Discord limits you to 2500 guilds per shard, so you should
	// try and make it so TOTAL_GUILDS/SHARD_COUNT ~= 1750
	private final static int SHARD_COUNT = 1;

	// Set to true if you want various debug statements
	public static final boolean DEBUG = false;
	public static final String BOT_VERSION = "v2.0.4";
	public static boolean isReady = false;

	private APIKeys apiKeys = new APIKeys();
	private BotConfiguration botConfig = new BotConfiguration();
	private CustomEventDispatcher eventDispatcher = new CustomEventDispatcher();

	public void start(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		if (!loadProperties()) {
			logger.error("Could not load Config.properties");
			System.exit(1);
		}
		jdaClients = new ArrayList<>(SHARD_COUNT);
		if (SHARD_COUNT > 1) {
			for (int i = 0; i < SHARD_COUNT; i++) {
				jdaClients.add(new JDABuilder(AccountType.BOT)
						.setToken(botConfig.getToken())
						.setStatus(OnlineStatus.DO_NOT_DISTURB)
						.setGame(Game.of("launching..."))
						.addEventListener(new Listeners(), new ModerationListeners(), new VoiceChannelListeners())
						.useSharding(i, SHARD_COUNT)
						.buildBlocking());
			}
		} else {
			jdaClients.add(new JDABuilder(AccountType.BOT)
					.setToken(botConfig.getToken())
					.setStatus(OnlineStatus.DO_NOT_DISTURB)
					.setGame(Game.of("launching..."))
					.addEventListener(new Listeners(), new ModerationListeners(), new VoiceChannelListeners())
					.buildBlocking());
		}
		shards = new Shards();
		State.changeBotAvatar(new File("resources/avatar/" + Bot.getInstance().getConfig().getAvatar()));
		State.changeBotPresence(OnlineStatus.ONLINE);
		State.changeBotStatus("www.momobot.io");
		initialize();
		isReady = true;
	}

	private static void initialize() {
		JobScheduler.initializeScheduler();
		TwitterEventListener.initTwitter();
	}

	public boolean loadProperties() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration("resources/Bot.properties");
			botConfig.setToken(config.getString("BotToken"));
			botConfig.setAvatar(config.getString("Avatar"));
			botConfig.setBotOwnerId(config.getString("BotOwnerId"));
			botConfig.setBotInviteLink(config.getString("InviteLink"));
			botConfig.setMaxSongLength(config.getInt("MaxSongLength", 15));


			Configuration subset = config.subset("apikey");
			Iterator<String> iter = subset.getKeys();
			while(iter.hasNext()) {
				String key = iter.next();
				String val = subset.getString(key);
				if(val.length() > 0) {
					this.apiKeys.put(key, val);
					logger.info("Added API key for: {}", key);
				}
			}
			StatusChangeJob.statuses = config.getStringArray("StatusRotation");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static {
		instance = new Bot();
	}	

	public static Bot getInstance() {
		return instance;
	}

	public ArrayList<JDA> getBots() {
		return jdaClients;
	}

	public APIKeys getApiKeys() {
		return this.apiKeys;
	}

	public BotConfiguration getConfig() {
		return this.botConfig;
	}

	public CustomEventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public class APIKeys {
		private Map<String, String> keys = new HashMap<String, String>();

		/**
		 * Get API key for given key
		 * @param key Key to get
		 * @return String of value, null if not found
		 */
		public String get(String key) throws NoAPIKeyException {
			if (keys.get(key) == null)
				throw new NoAPIKeyException();
			return keys.get(key);
		}

		void put(String key, String val) {
			this.keys.put(key, val);
		}
	}

	public class BotConfiguration {
		private String token, botOwnerId, avatar, botInviteLink;
		private int maxSongLength; // in minutes

		public int getMaxSongLength() {
			return maxSongLength;
		}

		public void setMaxSongLength(int songLength) {
			this.maxSongLength = songLength;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getBotOwnerId() {
			return botOwnerId;
		}

		public void setBotOwnerId(String botOwnerId) {
			this.botOwnerId = botOwnerId;
		}

		public String getAvatar() {
			return avatar;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}

		public String getBotInviteLink() {
			return botInviteLink;
		}

		public void setBotInviteLink(String botInviteLink) {
			this.botInviteLink = botInviteLink;
		}
	}

	public class Shards {
		/**
		 * Get a guild from an ID from all shards
		 * @param guildId Guild ID
		 * @return Guild if found, null if not
		 */
		public Guild getGuildById(String guildId) {
			for (JDA j : jdaClients) {
				Guild g;
				if ((g = j.getGuildById(guildId)) != null) {
					return g;
				}
			}
			return null;
		}

		/**
		 * Get a text channel from an ID from all shards
		 * @param channelId Channel ID
		 * @return TextChannel if found, null if not
		 */
		public TextChannel getTextChannelById(String channelId) {
			for (JDA j : jdaClients) {
				TextChannel t;
				if ((t = j.getTextChannelById(channelId)) != null) {
					return t;
				}
			}
			return null;
		}

		/**
		 * Get a voice channel from an ID from all shards
		 * @param channelId Channel ID
		 * @return VoiceChannel if found, null if not
		 */
		public VoiceChannel getVoiceChannelById(String channelId) {
			for (JDA j : jdaClients) {
				VoiceChannel c;
				if ((c = j.getVoiceChannelById(channelId)) != null) {
					return c;
				}
			}
			return null;
		}

		/**
		 * Get a user from an ID from all shards
		 * @param userId User ID
		 * @return User if found, null if not
		 */
		public User getUserById(String userId) {
			for (JDA j : jdaClients) {
				User u;
				if ((u = j.getUserById(userId)) != null) {
					return u;
				}
			}
			return null;
		}
	}
}
