package io.ph.bot.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VoiceChannelListeners extends ListenerAdapter {
	private static Logger log = LoggerFactory.getLogger(VoiceChannelListeners.class);
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
		if (e.getGuild().getAudioManager().getConnectedChannel() != null) {
			if (e.getChannelLeft().equals(e.getGuild().getAudioManager().getConnectedChannel())
					&& e.getChannelLeft().getMembers().size() == 1
					&& (GuildObject.guildMap.get(e.getGuild().getId()).getMusicManager() != null
							&& GuildObject.guildMap.get(e.getGuild().getId()).getMusicManager().getTrackManager().isEmpty())) {
				e.getGuild().getAudioManager().closeAudioConnection();
				GuildObject.guildMap.get(e.getGuild().getId()).getMusicManager().reset();
				log.info("Left a voice channel: {}", e.getGuild().getId());
			}
		}
	}
}
