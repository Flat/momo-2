package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Get a rating for an input string
 * @author Paul
 */
@CommandData (
		defaultSyntax = "rate",
		aliases = {"ratewaifu"},
		permission = Permission.NONE,
		description = "Rate something (maybe someone's waifu)",
		example = "Tohsaka Rin"
		)
public class Rate extends Command {

	@Override
	public void executeCommand(Message msg) {
		String s = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		if(s.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		em.setTitle("Here's my verdict", null)
		.setColor(Color.CYAN)
		.setDescription("**" + Math.abs((s.toLowerCase().hashCode() % 10) + 1) + "/10**");
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
