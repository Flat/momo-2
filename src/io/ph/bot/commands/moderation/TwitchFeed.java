package io.ph.bot.commands.moderation;

import java.awt.Color;

import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.TwitchEventListener;
import io.ph.bot.feed.TwitchFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Set twitch feed from given channel
 * @author Paul
 */
@CommandData (
		defaultSyntax = "twitch",
		aliases = {"twitchfeed"},
		permission = Permission.KICK,
		description = "Set this channel as a Twitch feed for a given username.\n"
				+ "Use the untwitch command to remove and listtwitch command to show all feeds\n",
				example = "TSM_TheOddOne"
		)
public class TwitchFeed extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		String userId;
		try {
			userId = TwitchEventListener.resolveUserIdFromUsername(contents);
			if (TwitchEventListener.getObserver(userId, msg.getGuild()) != null) {
				if (RedditEventListener.getObserver(contents, msg.getGuild())
						.getDiscoChannel().equals(msg.getTextChannel())) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("The Twitch user **" + contents +"** is already feeding to this channel");
					msg.getChannel().sendMessage(em.build()).queue();
					return;
				} else {
					// Moving channels
					TwitchEventListener.removeTwitchFeed(userId, msg.getGuild());
				}
			}
			new TwitchFeedObserver(msg.getChannel().getId(), contents.toLowerCase(), userId);
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription("Registered " + contents + " to feed to this channel");
		} catch (UnirestException e1) {
			e1.printStackTrace();
		} catch (NoAPIKeyException e1) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You do not have Twitch.tv API key setup!");
		} catch (IllegalArgumentException e1) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(e1.getMessage());
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
