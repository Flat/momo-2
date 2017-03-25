package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Remove a twitter feed registration
 * @author Paul
 */
@CommandData (
		defaultSyntax = "removetwitter",
		aliases = {"untwitter"},
		permission = Permission.KICK,
		description = "Remove a twitter feed from this channel",
		example = "FF_XIV_EN"
		)
public class RemoveTwitterFeed extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		try {
			User u = TwitterEventListener.twitterClient.lookupUsers(new String[]{contents}).get(0);
			if(TwitterEventListener.removeTwitterFeed(u.getId(), msg.getGuild())) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Removed **" + contents + "** from your Twitter feeds");
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("**" + contents + "** is not a current Twitter feed...");
			}
		} catch(TwitterException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED);
			if(e.getErrorCode() == 17) {
				em.setDescription(String.format("**%s** is not a valid Twitter username", contents));
			} else {
				em.setDescription("Something went wrong accessing Twitter!");
			}
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
