package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Role a die or many
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "roll",
		aliases = {"dice"},
		permission = Permission.NONE,
		description = "Roll a die! (or many dice with the format #d#)",
		example = "2d6"
		)
public class Roll extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em.setTitle(msg.getGuild().getMember(msg.getAuthor()).getEffectiveName() + " rolled " 
			+ (ThreadLocalRandom.current().nextInt(6) + 1) + " out of 6", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
		} else if(contents.contains("d")) {
			String[] split = contents.split("d");
			if(split.length == 2 && Util.isInteger(split[0]) && Util.isInteger(split[1])) {
				int max = Integer.parseInt(split[0]) * Integer.parseInt(split[1]);
				int roll = ThreadLocalRandom.current().nextInt(Integer.parseInt(split[0]), max + 1);
				em.setTitle(msg.getGuild().getMember(msg.getAuthor()).getEffectiveName() + " rolled " 
						+ roll + " with a " + split[0] + "d" + split[1], null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
				
			}
		} else {
			if(Util.isInteger(contents)) {
				em.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setTitle(msg.getGuild().getMember(msg.getAuthor()).getEffectiveName() + " rolled "
						+ (ThreadLocalRandom.current().nextInt(Integer.parseInt(contents)) + 1) 
						+ " out of " + Integer.parseInt(contents), null);
			} else {
				MessageUtils.sendIncorrectCommandUsage(msg, this);
				return;
			}
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
