package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Change music channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "welcomechannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the welcome channel for server.\n"
				+ "If this and a welcome message are set, the bot will send the welcome message to the designated channel",
		example = "(no parameters)"
		)
public class SetWelcomeChannel extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		String currentChannel = msg.getChannel().getId();
		EmbedBuilder em = new EmbedBuilder().setTitle("Success", null);
		if(currentChannel.equals(g.getSpecialChannels().getWelcome())) {
			em.setColor(Color.CYAN)
			.setDescription("Removed **" + msg.getChannel().getName() + "** as welcome channel");
			g.getSpecialChannels().setWelcome("");
		} else {
			em.setColor(Color.GREEN)
			.setDescription("Set **" + msg.getChannel().getName() + "** as welcome channel");
			g.getSpecialChannels().setWelcome(currentChannel);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
