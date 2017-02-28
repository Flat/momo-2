package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Change log channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "logchannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the log channel for server.\n"
				+ "If this is set, the bot will send messages detailing certain events (people leaving/joining, bans etc)",
		example = "(no parameters)"
		)
public class SetLogChannel extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		String currentChannel = msg.getChannel().getId();
		EmbedBuilder em = new EmbedBuilder().setTitle("Success", null);
		if(currentChannel.equals(g.getSpecialChannels().getLog())) {
			em.setColor(Color.CYAN)
			.setDescription("Removed **" + msg.getChannel().getName() + "** as log channel");
			g.getSpecialChannels().setLog("");
		} else {
			em.setColor(Color.GREEN)
			.setDescription("Set **" + msg.getChannel().getName() + "** as log channel");
			g.getSpecialChannels().setLog(currentChannel);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
