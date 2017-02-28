package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
/**
 * Prune x messages (default 40)
 * @author Paul
 */
@CommandData (
		defaultSyntax = "prune",
		aliases = {},
		permission = Permission.KICK,
		description = "Prune n messages (default 40, up to 75). Can prune a specific user's messages with a mention.\n",
		example = "25 @target\n"
				+ "50\n"
				+ "@target"
		)
public class Prune extends Command {
	private static final int MAX_PRUNE = 75;
	private static final int DEFAULT_PRUNE = 40;

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(Instant.now());
		String t = Util.getCommandContents(msg);
		int i = 0;
		Member target = null;

		if (t.length() == 0) {
			// Prune the default amount
			msg.getTextChannel().deleteMessages(msg.getTextChannel()
					.getHistory().retrievePast(DEFAULT_PRUNE + 1).complete());
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("Pruned " + DEFAULT_PRUNE + " messages");
		} else if (Util.isInteger(Util.getParam(msg))) {
			// User specified a number as the first parameter
			int num = Integer.parseInt(Util.getParam(msg)) > MAX_PRUNE ? MAX_PRUNE : Integer.parseInt(Util.getParam(msg));
			num = num < 1 ? 1 : num;
			if (t.split(" ").length > 1) {
				// User specified a target after the #
				if (msg.getMentionedUsers().size() == 0)
					target = Util.resolveMemberFromMessage(t.substring(t.indexOf(" ") + 1), msg.getGuild());
				else
					target = msg.getGuild().getMember(msg.getMentionedUsers().get(0));
				if (target == null) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("User " + t.substring(t.indexOf(" ") + 1) + " does not exist");
				}
			}
			if (target == null) {
				// User didn't specify a target, just prune the #
				msg.getTextChannel().deleteMessages(msg.getTextChannel()
						.getHistory().retrievePast(num + 1).complete());
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("Pruned " + (i - 2) + " messages");
			} else {
				// Target specified, only their messages
				int targetCounter = 0;
				List<Message> toPrune = new ArrayList<>();
				for (Message m : msg.getTextChannel().getHistory().retrievePast(MAX_PRUNE).complete()) {
					if (i++ == MAX_PRUNE || targetCounter == num)
						break;
					if (m.getAuthor().equals(target.getUser())) {
						targetCounter++;
						toPrune.add(m);
					}
				}
				msg.getTextChannel().deleteMessages(toPrune);
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("Pruned " + targetCounter + " of **" 
						+ target.getEffectiveName() + "**'s messages");
			}
		} else if ((target = Util.resolveMemberFromMessage(msg)) != null) {
			// User only specified a target
			int targetCounter = 0;
			List<Message> toPrune = new ArrayList<>();
			for (Message m : msg.getTextChannel().getHistory().retrievePast(MAX_PRUNE).complete()) {
				if (i++ == MAX_PRUNE + 1 || targetCounter == DEFAULT_PRUNE)
					break;
				if (m.getAuthor().equals(target.getUser())) {
					targetCounter++;
					toPrune.add(m);
				}
			}
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("Pruned " + targetCounter + " of **" 
					+ target.getEffectiveName() + "**'s messages");
		} else if (Util.isInteger(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1])) {
			// There's a possibility they did $prune username #
			String targ = Util.combineStringArray(Util.removeLastArrayEntry(Util.getCommandContents(msg).split(" ")));
			if (!msg.getMentionedUsers().isEmpty()) {
				target = msg.getGuild().getMember(msg.getMentionedUsers().get(0));
			} else {
				target = Util.resolveMemberFromMessage(targ, msg.getGuild());
			}
			if (target != null) {
				int num = Integer.parseInt(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1]) > MAX_PRUNE ? 
						MAX_PRUNE : Integer.parseInt(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1]);
				num = num < 1 ? 1 : num;
				int targetCounter = 0;
				List<Message> toPrune = new ArrayList<>();
				for (Message m : msg.getTextChannel().getHistory().retrievePast(MAX_PRUNE).complete()) {
					if (i++ == MAX_PRUNE || targetCounter == num)
						break;
					if (m.getAuthor().equals(target.getUser())) {
						targetCounter++;
						toPrune.add(m);
					}
				}
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("Pruned " + targetCounter + " of **" 
						+ target.getEffectiveName() + "**'s messages");
			}
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
