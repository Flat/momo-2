package io.ph.bot.commands.music;

import java.awt.Color;
import java.util.Optional;

import io.ph.bot.audio.GuildMusicManager;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

@CommandData (
		defaultSyntax = "listen.moe",
		aliases = {"listenmoe"},
		category = CommandCategory.MUSIC,
		permission = Permission.NONE,
		description = "Stream from Listen.moe to your server",
		example = ""
		)
public class ListenMoe extends Command {

	private static final String URL = "https://listen.moe/vorbis";

	@Override
	public void executeCommand(Message msg) {
		Optional<VoiceChannel> opt;
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		net.dv8tion.jda.core.managers.AudioManager audio = msg.getGuild().getAudioManager();
		boolean djSet = !g.getConfig().getDjRoleId().isEmpty();
		if ((djSet && !msg.getGuild().getMember(msg.getAuthor())
				.getRoles().contains(msg.getGuild().getRoleById(g.getConfig().getDjRoleId())))
				&& !Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)) {
			// Opting to fail silently here
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		// First, check if the guild has a designated music channel
		if (!g.getSpecialChannels().getMusicVoice().isEmpty() 
				&& msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()) != null) {
			audio.openAudioConnection(msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()));
		} else if (!audio.isConnected() && !audio.isAttemptingToConnect()) {
			if ((opt = msg.getGuild().getVoiceChannels().stream()
					.filter(v -> v.getMembers().contains(msg.getGuild().getMember(msg.getAuthor())))
					.findAny()).isPresent()) {
				// User is in a channel, calling the play method
				audio.openAudioConnection(opt.get());
			} else {
				// User isn't in a channel, yell at them
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("You must be in a voice channel so I know where to go!");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
		}
		// At this point, we're connected to a voice channel

		GuildMusicManager.loadAndPlay(msg.getTextChannel(), 
				URL, "Listen.moe", msg.getGuild().getMember(msg.getAuthor()), true);
	}
}
