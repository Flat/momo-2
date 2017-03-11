package io.ph.bot.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.ph.bot.audio.AudioManager;
import io.ph.bot.audio.GuildMusicManager;
import io.ph.bot.audio.PlaylistEntity;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.exception.IllegalArgumentException;
import net.dv8tion.jda.core.entities.Guild;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * Holds instantiated data for each guild: its relevant settings, config etc
 * @author Paul
 *
 */
public class GuildObject {
	public static Map<String, GuildObject> guildMap = new HashMap<>();

	private Map<String, Integer> userTimerMap = ExpiringMap.builder()
			.expiration(15, TimeUnit.SECONDS)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();
	private PropertiesConfiguration config;
	private SpecialChannels specialChannels;
	private HistoricalSearches historicalSearches;
	private ServerConfiguration serverConfiguration;
	private GuildMusicManager musicManager;

	private List<PlaylistEntity> musicPlaylist = new ArrayList<>();
	private Set<String> joinableRoles = new HashSet<>();
	private HashMap<String, Boolean> commandStatus = new HashMap<>();

	/**
	 * Initialize this guild and add it to the guildMap
	 * <p>
	 * Calling this constructor adds the guild's settings
	 * @param g Guild to initialize
	 */
	public GuildObject(Guild g) {
		try {
			// Read data from this file
			this.config = new PropertiesConfiguration("resources/guilds/" + g.getId() + "/GuildProperties.properties");
			this.config.setAutoSave(true);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		this.specialChannels = new SpecialChannels(config.getString("WelcomeChannelId", ""),
				config.getString("MusicChannelId", ""), config.getString("LogChannelId", ""),
				config.getString("MusicVoiceChannelId", ""));
		this.historicalSearches = new HistoricalSearches();
		// Hack to get rid of potential delimiters
		String welcomeMessage = Arrays.toString(config.getStringArray("NewUserWelcomeMessage"));
		welcomeMessage = welcomeMessage.substring(1, welcomeMessage.length() - 1);
		this.serverConfiguration = new ServerConfiguration(config.getString("ServerCommandPrefix", "$"), 
				config.getInt("MessagesPerFifteenSeconds", 0),
				config.getInt("CommandCooldown", 0), 
				welcomeMessage,
				config.getString("MutedRoleId", ""),
				config.getString("DjRoleID", ""),
				config.getBoolean("LimitToOneRole", false),
				config.getBoolean("FirstTime", true),
				config.getBoolean("DisableInvites", false),
				config.getBoolean("PMWelcomeMessage", false),
				config.getBoolean("AdvancedLogging", false));
		// Load up enabled & disabled commands
		String[] enabledCommands = config.getStringArray("EnabledCommands");
		String[] disabledCommands = config.getStringArray("DisabledCommands");
		for(String s : enabledCommands) {
			if (CommandHandler.getCommand(s) != null)
				this.commandStatus.put(s, true);
		}
		for(String s : disabledCommands) {
			if (CommandHandler.getCommand(s) != null)
				this.commandStatus.put(s, false);
		}
		// Load up joinable roles
		String[] joinableRolesP = config.getStringArray("JoinableRoles");
		for(String s : joinableRolesP) {
			if(s.equals(""))
				continue;
			if(g.getRoleById(s) == null)
				continue;
			this.joinableRoles.add(s);
		}

		// Load up guild playlist
		try {
			Gson gson = new Gson();
			String input = FileUtils
					.readFileToString(new File("resources/guilds/" + g.getId() 
					+ "/IdlePlaylist.json"), "UTF-8");
			if (!input.isEmpty()) {
				Type collectionType = new TypeToken<Collection<PlaylistEntity>>(){}.getType();
				Collection<PlaylistEntity> playlist = gson.fromJson(input, collectionType);
				this.musicPlaylist.addAll(playlist);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save this guild's music playlist to file
	 */
	public void saveMusicPlaylist(String guildId) {
		Gson gson = new Gson();
		String s = gson.toJson(this.musicPlaylist);
		try {
			FileUtils.writeStringToFile(new File("resources/guilds/" + guildId
					+ "/IdlePlaylist.json"), s, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a joinable role by ID
	 * @param roleId Role ID
	 * @return True if success, false if already joinable
	 */
	public boolean addJoinableRole(String roleId) {
		if(this.joinableRoles.add(roleId)) {
			this.config.setProperty("JoinableRoles", this.joinableRoles);
			return true;
		}
		return false;
	}

	/**
	 * Remove a joinable role by ID
	 * @param roleId Role ID
	 * @return True if success, false if was not already joinable
	 */
	public boolean removeJoinableRole(String roleId) {
		if(this.joinableRoles.remove(roleId)) {
			this.config.setProperty("JoinableRoles", this.joinableRoles);
			return true;
		}
		return false;
	}

	/**
	 * Check if a role ID is joinable
	 * @param roleId Role ID to check
	 * @return True if joinable, false if not
	 */
	public boolean isJoinableRole(String roleId) {
		return this.joinableRoles.contains(roleId) ? true : false;
	}

	/**
	 * Get the set of joinable roles
	 * @return
	 */
	public Set<String> getJoinableRoles() {
		return this.joinableRoles;
	}

	/**
	 * Disable a command on this guild
	 * @param s Main name of command to disable
	 * @return True if disabled, false if it was already disabled
	 * @throws IllegalArgumentException Command doesn't exist
	 */
	public boolean disableCommand(String s) throws IllegalArgumentException {
		return editCommand(s, false);
	}

	/**
	 * Enable a command on this guild
	 * @param s Main name of command to enable
	 * @return True if enabled, false if it was already enabled
	 * @throws IllegalArgumentException Command doesn't exist
	 */
	public boolean enableCommand(String s) throws IllegalArgumentException {
		return editCommand(s, true);
	}

	/**
	 * Edit the status of a command (enabled/disabled)
	 * @param s Command to change
	 * @param toEnable True if you want to enable, false if not
	 * @return True if status changed, false if not
	 * @throws IllegalArgumentException Command doesn't exist
	 */
	private boolean editCommand(String s, boolean toEnable) throws IllegalArgumentException {
		s = CommandHandler.aliasToDefaultMap.get(s);
		if(!validCommandToEdit(s))
			throw new IllegalArgumentException();
		this.commandStatus.put(s, toEnable);
		List<String> enabled = config.getList("EnabledCommands").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		List<String> disabled = config.getList("DisabledCommands").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		if(toEnable) {
			if(enabled.contains(s))
				return false;
			disabled.remove(s);
			enabled.add(s);
		} else {
			if(disabled.contains(s))
				return false;
			enabled.remove(s);
			disabled.add(s);
		}
		enabled.remove("");
		disabled.remove("");
		config.setProperty("EnabledCommands", enabled);
		config.setProperty("DisabledCommands", disabled);
		return true;
	}

	/**
	 * Get the status (enabled/disabled) of a command
	 * @param input Command to check for
	 * @return
	 */
	public boolean getCommandStatus(String input) {
		Command c = CommandHandler.getCommand(input);
		if(c == null)
			return false;
		return this.commandStatus.get(input);
	}

	/**
	 * Check if the command exists
	 * @param s Command to check
	 * @return True if exists, false if not
	 */
	public boolean validCommandToEdit(String s) {
		for(String key : commandStatus.keySet()) {
			if(s.equalsIgnoreCase(key))
				return true;
		}
		return false;
	}

	/**
	 * Enable all commands
	 */
	public void enableAllCommands() {
		commandStatus.replaceAll((key, value) -> true);
		config.setProperty("EnabledCommands", commandStatus.keySet());
		config.setProperty("DisabledCommands", new ArrayList<String>());
	}
	/**
	 * Disable all commands
	 */
	public void disableAllCommands() {
		commandStatus.replaceAll((key, value) -> false);
		config.setProperty("DisabledCommands", commandStatus.keySet());
		config.setProperty("EnabledCommands", new ArrayList<String>());
	}

	/**
	 * Expiring map of user ID -> message counts
	 * @return User timer map
	 */
	public Map<String, Integer> getUserTimerMap() {
		return userTimerMap;
	}

	/**
	 * Initialize music manager if not found and set audio provider
	 * @param g Guild
	 * @return GuildMusicManager
	 */
	public GuildMusicManager getMusicManager(Guild g) {
		if (this.musicManager == null) {
			this.musicManager = new GuildMusicManager(AudioManager.getMasterManager(), g.getId());
			g.getAudioManager().setSendingHandler(this.musicManager.getSendHandler());
		}
		return this.musicManager;
	}

	/**
	 * No arg get music manager
	 * Precondition: musicManager has been initialized with getMusicManager(g : Guild)
	 * @return GuildMusicManager
	 */
	public GuildMusicManager getMusicManager() {
		return this.musicManager;
	}

	/**
	 * Get status of commands map
	 * @return CommandStatus map
	 */
	public Map<String, Boolean> getCommandStatus() {
		return this.commandStatus;
	}

	/**
	 * Get special designated channels for this guild
	 * @return Special channels object
	 */
	public SpecialChannels getSpecialChannels() {
		return this.specialChannels;
	}

	/**
	 * Get historical searches made in this guild
	 * @return Historical searches object
	 */
	public HistoricalSearches getHistoricalSearches() {
		return historicalSearches;
	}

	/**
	 * Get general configuration for this guild
	 * @return Server configuration object
	 */
	public ServerConfiguration getConfig() {
		return this.serverConfiguration;
	}


	public List<PlaylistEntity> getMusicPlaylist() {
		return musicPlaylist;
	}

	/**
	 * Pop off the top and queue it up again
	 * @return Next song in the playlist
	 */
	public PlaylistEntity getNextPlaylistSong() {
		PlaylistEntity toReturn = musicPlaylist.remove(0);
		musicPlaylist.add(toReturn);
		return toReturn;
	}


	public class ServerConfiguration {
		private String commandPrefix;
		private int messagesPerFifteen;
		private int commandCooldown;
		private String welcomeMessage;
		private boolean pmWelcomeMessage;
		private boolean limitToOneRole;
		private boolean firstTime;
		private boolean disableInvites;
		private boolean advancedLogging;
		private String mutedRoleId;
		private String djRoleId;

		ServerConfiguration(String commandPrefix, int messagesPerFifteen, int commandCooldown,
				String welcomeMessage, String mutedRoleId, String djRoleId, boolean limitToOneRole,
				boolean firstTime, boolean disableInvites,
				boolean pmWelcomeMessage, boolean advancedLogging) {
			this.commandPrefix = commandPrefix;
			this.messagesPerFifteen = messagesPerFifteen;
			this.commandCooldown = commandCooldown;
			this.welcomeMessage = welcomeMessage;
			this.mutedRoleId = mutedRoleId;
			this.djRoleId = djRoleId;
			this.limitToOneRole = limitToOneRole;
			this.firstTime = firstTime;
			this.disableInvites = disableInvites;
			this.pmWelcomeMessage = pmWelcomeMessage;
			this.advancedLogging = advancedLogging;
		}

		public String getCommandPrefix() {
			return commandPrefix;
		}
		public void setCommandPrefix(String commandPrefix) {
			this.commandPrefix = commandPrefix;
			config.setProperty("ServerCommandPrefix", commandPrefix);
		}
		public int getMessagesPerFifteen() {
			return messagesPerFifteen;
		}
		public void setMessagesPerFifteen(int messagesPerFifteen) {
			this.messagesPerFifteen = messagesPerFifteen;
			config.setProperty("MessagesPerFifteenSeconds", messagesPerFifteen);
		}
		public int getCommandCooldown() {
			return commandCooldown;
		}
		public void setCommandCooldown(int commandCooldown) {
			this.commandCooldown = commandCooldown;
			config.setProperty("CommandCooldown", commandCooldown);
		}
		public String getWelcomeMessage() {
			return welcomeMessage;
		}
		public void setWelcomeMessage(String welcomeMessage) {
			this.welcomeMessage = welcomeMessage;
			config.setProperty("NewUserWelcomeMessage", welcomeMessage);
		}
		public String getMutedRoleId() {
			return mutedRoleId;
		}

		public void setMutedRoleId(String mutedRoleId) {
			this.mutedRoleId = mutedRoleId;
			config.setProperty("MutedRoleID", mutedRoleId);
		}

		public String getDjRoleId() {
			return djRoleId;
		}

		public void setDjRoleId(String djRoleId) {
			this.djRoleId = djRoleId;
			config.setProperty("DjRoleID", djRoleId);
		}

		public boolean isFirstTime() {
			return firstTime;
		}
		
		public void setFirstTime(boolean firstTime) {
			this.firstTime = firstTime;
			config.setProperty("FirstTime", firstTime);
		}

		public boolean isLimitToOneRole() {
			return limitToOneRole;
		}

		public void setLimitToOneRole(boolean limitToOneRole) {
			this.limitToOneRole = limitToOneRole;
			config.setProperty("LimitToOneRole", limitToOneRole);
		}

		public boolean isDisableInvites() {
			return disableInvites;
		}

		public void setDisableInvites(boolean disableInvites) {
			this.disableInvites = disableInvites;
			config.setProperty("DisableInvites", disableInvites);
		}

		public boolean isPmWelcomeMessage() {
			return pmWelcomeMessage;
		}

		public void setPmWelcomeMessage(boolean pmWelcomeMessage) {
			this.pmWelcomeMessage = pmWelcomeMessage;
			config.setProperty("PMWelcomeMessage", pmWelcomeMessage);
		}

		public boolean isAdvancedLogging() {
			return advancedLogging;
		}

		public void setAdvancedLogging(boolean advancedLogging) {
			this.advancedLogging = advancedLogging;
			config.setProperty("AdvancedLogging", advancedLogging);
		}


	}

	public class HistoricalSearches {
		// This is used to play Themes.moe or Youtube results directly with $music
		private Map<Integer, String[]> historicalMusic;
		// This is used to do $theme #
		private Map<Integer, ArrayList<Theme>> historicalThemeSearchResults;

		HistoricalSearches() {
			this.historicalMusic = ExpiringMap.builder()
					.expiration(10, TimeUnit.MINUTES)
					.expirationPolicy(ExpirationPolicy.CREATED)
					.build();
			historicalThemeSearchResults = ExpiringMap.builder()
					.expiration(10, TimeUnit.MINUTES)
					.expirationPolicy(ExpirationPolicy.CREATED)
					.build();
		}

		public Map<Integer, ArrayList<Theme>> getHistoricalThemeSearchResults() {
			return historicalThemeSearchResults;
		}

		public void addHistoricalThemeSearchResult(int i, ArrayList<Theme> a) {
			this.historicalThemeSearchResults.put(i, a);
		}

		public Map<Integer, String[]> getHistoricalMusic() {
			return historicalMusic;
		}

		public void addHistoricalMusic(int i, String[] s) {
			this.historicalMusic.put(i, s);
		}

		public void clearHistoricalMusic() {
			this.historicalMusic.clear();
		}
	}

	public class SpecialChannels {
		private String welcome;
		private String music;
		private String log;
		private String musicVoice;
		SpecialChannels(String welcome, String music, String log, String musicVoice) {
			this.welcome = welcome;
			this.music = music;
			this.log = log;
			this.musicVoice = musicVoice;
		}


		public String getWelcome() {
			return welcome;
		}

		public void setWelcome(String welcome) {
			this.welcome = welcome;
			config.setProperty("WelcomeChannelId", welcome);
		}

		public String getMusic() {
			return music;
		}

		public void setMusic(String music) {
			this.music = music;
			config.setProperty("MusicChannelId", music);
		}

		public String getLog() {
			return log;
		}

		public void setLog(String log) {
			this.log = log;
			config.setProperty("LogChannelId", log);
		}

		public String getMusicVoice() {
			return this.musicVoice;
		}

		public void setMusicVoice(String musicVoice) {
			this.musicVoice = musicVoice;
			config.setProperty("MusicVoiceChannelId", musicVoice);
		}
	}
}
