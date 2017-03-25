package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Remove a registration for this server from a reddit feed
 * @author Paul
 */
@CommandData (
		defaultSyntax = "removereddit",
		aliases = {"unreddit", "unsubreddit", "removeredditfeed"},
		permission = Permission.KICK,
		description = "Remove a reddit feed from this channel",
		example = "awwnime"
		)
public class RemoveRedditFeed extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(RedditEventListener.removeRedditFeed(contents, msg.getGuild())) {
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription("Removed /r/**" + contents + "** from your reddit feeds.\n"
					+ "Changes will take place in about 30 seconds");
		} else {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("/r/**" + contents + "** is not a current feed...");
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
