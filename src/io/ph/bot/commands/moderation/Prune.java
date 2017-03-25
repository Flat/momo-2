package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GenericContainer;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
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
			
			msg.getTextChannel().getHistory().retrievePast(DEFAULT_PRUNE + 1).queue(list -> {
				msg.getTextChannel().deleteMessages(list).queue(success -> {
					em.setTitle("Success", null)
					.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
					.setDescription("Pruned " + DEFAULT_PRUNE + " messages");
					msg.getChannel().sendMessage(em.build()).queue();
				}, failure -> {
					msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
				});
			}, failure -> {
				msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
			});
			
		} else if (Util.isInteger(Util.getParam(msg))) {
			// User specified a number as the first parameter
			int num = Integer.parseInt(Util.getParam(msg)) > MAX_PRUNE ? MAX_PRUNE : Integer.parseInt(Util.getParam(msg));
			num = num < 1 ? 1 : num;
			if (t.split(" ").length > 1) {
				// User specified a target after the #
				if (msg.getMentionedUsers().size() == 0) {
					target = Util.resolveMemberFromMessage(t.substring(t.indexOf(" ") + 1), msg.getGuild());
				} else {
					target = msg.getGuild().getMember(msg.getMentionedUsers().get(0));
				}
				if (target == null) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("User " + t.substring(t.indexOf(" ") + 1) + " does not exist");
					msg.getChannel().sendMessage(em.build()).queue();
				}
			}
			if (target == null) {
				// User didn't specify a target, just prune the #
				final int i2 = num;
				msg.getTextChannel().getHistory().retrievePast(num + 2).queue(msgs -> {
					msg.getTextChannel().deleteMessages(msgs).queue(success -> {
						em.setTitle("Success", null)
						.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
						.setDescription("Pruned " + (i2) + " messages");
						msg.getChannel().sendMessage(em.build()).queue();
					}, failure -> {
						msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
					});
				}, failure -> {
					msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
				});
				
			} else {
				// Target specified, only their messages
				GenericContainer<Integer> num2 = new GenericContainer<>(num);
				final Member target2 = target;
				msg.getTextChannel().getHistory().retrievePast(MAX_PRUNE).queue(list -> {
					List<Message> toPrune = new ArrayList<>();
					GenericContainer<Integer> tCounter = new GenericContainer<>(0);
					int i2 = 0;
					for (Message m : list) {
						if (i2++ == MAX_PRUNE || tCounter.getVal() == num2.getVal())
							break;
						if (m.getAuthor().equals(target2.getUser())) {
							tCounter.setVal(tCounter.getVal() + 1);
							toPrune.add(m);
						}
					}
					msg.getTextChannel().deleteMessages(toPrune).queue(success -> {
						em.setTitle("Success", null)
						.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
						.setDescription("Pruned " + tCounter.getVal() + " of **" 
								+ target2.getEffectiveName() + "**'s messages");
						msg.getChannel().sendMessage(em.build()).queue();
					}, failure -> {
						msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
					});
				}, failure -> {
					msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();
				});
				
				
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
			final int targetCounter2 = targetCounter;
			final Member target2 = target;
			msg.getTextChannel().deleteMessages(toPrune).queue(success -> {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Pruned " + targetCounter2 + " of **" 
						+ target2.getEffectiveName() + "**'s messages");
				msg.getChannel().sendMessage(em.build()).queue();
			}, failure -> {
				msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();;
			});
			
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
				final int targetCounter2 = targetCounter;
				final Member target2 = target;
				msg.getTextChannel().deleteMessages(toPrune).queue(success -> {
					em.setTitle("Success", null)
					.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
					.setDescription("Pruned " + targetCounter2 + " of **" 
							+ target2.getEffectiveName() + "**'s messages");
					msg.getChannel().sendMessage(em.build()).queue();
				}, failure -> {
					msg.getChannel().sendMessage(MessageUtils.handleFailure(failure)).queue();;
				});
			}
		}
	}

}
