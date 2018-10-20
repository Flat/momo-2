package io.ph.bot.commands.fun;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Pick between options separated by delimiter "or"
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "pick",
		aliases = {"choose"},
		category = CommandCategory.FUN,
		permission = Permission.NONE,
		description = "Pick between multiple choices",
		example = "play games or sleep or do homework"
		)
public class Pick extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String[] splitMessage = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContentDisplay().split(" "))).split(" or ");
		if(splitMessage.length < 2) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
		} else {
			int rand = ThreadLocalRandom.current().nextInt(0, splitMessage.length);
			em.setTitle("I choose " + splitMessage[rand], null)
			.setColor(Color.CYAN);
			msg.getChannel().sendMessage(em.build()).queue();
		}
	}

}
