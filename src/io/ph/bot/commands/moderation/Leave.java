package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.time.Instant;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.managers.AudioManager;
/**
 * Force Momo to leave your server's voice channel if she's in it
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "leave",
		aliases = {},
		category = CommandCategory.MUSIC,
		permission = Permission.KICK,
		description = "Force Momo to leave voice channel. This also kills the queue",
		example = "(no parameters)"
		)
public class Leave extends Command {
	
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(Instant.now());
		AudioManager audio = msg.getGuild().getAudioManager();
		audio.closeAudioConnection();
		GuildObject.guildMap.get(msg.getGuild().getId()).getMusicManager().reset();
		
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
		.setDescription("Left your voice channel and cleared the queue");
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
