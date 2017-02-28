package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Setup a music channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "setupmusic",
		aliases = {"initializemusic"},
		permission = io.ph.bot.model.Permission.MANAGE_SERVER,
		description = "Setup a music channel. This is useful"
				+ " if you want to contain the bot to a single channel",
				example = "(no parameters)"
		)
public class SetupMusic extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if((g.getSpecialChannels().getMusicVoice() != null 
				|| !g.getSpecialChannels().getMusicVoice().equals(""))
				&& msg.getGuild().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()) != null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like I already have a music channel here");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}

		msg.getGuild().getController().createVoiceChannel("music").queue(voiceChannel -> {
			Role everyone = msg.getGuild().getPublicRole();
			voiceChannel.createPermissionOverride(everyone).queue(override -> {
				override.getManager().deny(Permission.VOICE_SPEAK).queue(
						success1 -> {
							em.setTitle("Success", null)
							.setColor(Color.GREEN)
							.setDescription("Setup your music channel and permission overrides");
							g.getSpecialChannels().setMusicVoice(voiceChannel.getId());
							msg.getChannel().sendMessage(em.build()).queue();
						}, failure -> {
							em.setTitle("Error", null)
							.setColor(Color.RED)
							.setDescription(failure.getMessage());
							msg.getChannel().sendMessage(em.build()).queue();
						});
			}, failure -> {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(failure.getMessage());
				msg.getChannel().sendMessage(em.build()).queue();
			});
		}, failure -> {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(failure.getMessage());
			msg.getChannel().sendMessage(em.build()).queue();
		});
		
	}
}
