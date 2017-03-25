package io.ph.bot.commands.moderation;

import java.awt.Color;

import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.feed.TwitchEventListener;
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
		defaultSyntax = "removetwitch",
		aliases = {"untwitch"},
		permission = Permission.KICK,
		description = "Remove a Twitch.tv feed from this channel",
		example = "TSM_TheOddOne"
		)
public class RemoveTwitchFeed extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		try {
			if(TwitchEventListener.removeTwitchFeed(TwitchEventListener
					.resolveUserIdFromUsername(contents), msg.getGuild())) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Removed **" + contents + "** from your Twitch.tv feeds");
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("**" + contents + "** is not a current Twitch.tv feed...");
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(contents + " is not a valid Twitch username!");
		} catch (UnirestException e) {
			e.printStackTrace();
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Error connecting to Twitch.tv servers!");
		} catch (NoAPIKeyException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No Twitch.tv API keys set!");
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
