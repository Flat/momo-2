package io.ph.bot;

import java.io.File;
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
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.listeners.Listeners;
import io.ph.bot.listeners.ModerationListeners;
import io.ph.bot.listeners.VoiceChannelListeners;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * Singleton instance of the entire bot. Includes configuration and the main JDA singleton
 * @author Paul
 *
 */
public class Bot {
	private static final Bot instance;
	private static JDA jdaClient;
	private final static Logger logger = LoggerFactory.getLogger(Bot.class);
	
	// Set to true if you want various debug statements
	public static final boolean DEBUG = true;
	public static final String BOT_VERSION = "2.0";
	public static boolean isReady = false;
	
	private APIKeys apiKeys = new APIKeys();
	private BotConfiguration botConfig = new BotConfiguration();
	private CustomEventDispatcher eventDispatcher = new CustomEventDispatcher();
	
	public void start(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
		if (!loadProperties()) {
			logger.error("Could not load Config.properties");
			System.exit(1);
		}
		jdaClient = new JDABuilder(AccountType.BOT)
				.setToken(botConfig.getToken())
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setGame(Game.of("launching..."))
				.addListener(new Listeners(), new ModerationListeners(), new VoiceChannelListeners())
				.buildBlocking();
		State.changeBotAvatar(new File("resources/avatar/" + Bot.getInstance().getConfig().getAvatar()));
		State.changeBotPresence(OnlineStatus.ONLINE);
		State.changeBotStatus("www.momobot.io");
		isReady = true;
	}


	private boolean loadProperties() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration("resources/Bot.properties");
			botConfig.setToken(config.getString("BotToken"));
			botConfig.setAvatar(config.getString("Avatar"));
			botConfig.setBotOwnerId(config.getString("BotOwnerId"));
			botConfig.setBotInviteLink(config.getString("InviteLink"));
			botConfig.setMaxSongLength(config.getInt("MaxSongLength", 15));
			
			//StatusChangeJob.statuses = config.getStringArray("StatusRotation");
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
			System.out.println(StatusChangeJob.statuses);
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

	public JDA getBot() {
		return jdaClient;
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
}
