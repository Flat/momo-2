package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Change music log channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "musiclogchannel",
		aliases = {"musiclog", "musicchannel"},
		permission = Permission.MANAGE_SERVER,
		description = "Change the announcement channel for music purposes.\n"
				+ "If set, the bot will ping the user who queued up the next song",
		example = "(no parameters)"
		)
public class SetMusicLogChannel extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		String currentChannel = msg.getChannel().getId();
		EmbedBuilder em = new EmbedBuilder().setTitle("Success", null);
		if(currentChannel.equals(g.getSpecialChannels().getMusic())) {
			em.setColor(Color.CYAN)
			.setDescription("Removed **" + msg.getChannel().getName() + "** as music channel");
			g.getSpecialChannels().setMusic("");
		} else {
			em.setColor(Color.GREEN)
			.setDescription("Set **" + msg.getChannel().getName() + "** as music channel");
			g.getSpecialChannels().setMusic(currentChannel);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
