package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.entities.Guild;


/**
 * The main Audio manager class
 *
 */
public class AudioManager {
	private static final AudioPlayerManager playerManager;
	static {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public static AudioPlayerManager getMasterManager() {
		return playerManager;
	}

	public static GuildMusicManager getGuildManager(Guild guild) {
		return GuildObject.guildMap.get(guild.getId()).getMusicManager(guild);
	}
}
