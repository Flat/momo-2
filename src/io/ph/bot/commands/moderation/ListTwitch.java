package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitchEventListener;
import io.ph.bot.feed.TwitchFeedObserver;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * List all Twitch feeds for this guild
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "listtwitch",
		aliases = {"twitchlist"},
		permission = Permission.KICK,
		description = "List all Twitch feeds for your server",
		example = "(no parameters)"
		)
public class ListTwitch extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		List<TwitchFeedObserver> thisGuilds = new ArrayList<TwitchFeedObserver>();
		for (List<TwitchFeedObserver> list : TwitchEventListener.getFeed().values()) {
			for (TwitchFeedObserver observer : list) {
				if (observer.getDiscoChannel() != null && observer.getDiscoChannel().getGuild().equals(msg.getGuild())) {
					thisGuilds.add(observer);
				}
			}
		}
		if (thisGuilds.isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Your server does not have any Twitch.tv feeds setup");
		} else {
			Collections.sort(thisGuilds, (f, s) -> {
				return f.getDiscoChannel().getName().compareTo(s.getDiscoChannel().getName());
			});
			StringBuilder sb = new StringBuilder();
			String prevChannelName = "";
			for (TwitchFeedObserver observer : thisGuilds) {
				if (observer.getDiscoChannel() == null)
					continue;
				if (!prevChannelName.equals(observer.getDiscoChannel().getName())) {
					if (prevChannelName.length() > 0)
						em.addField("\\#" + prevChannelName, sb.toString(), false);
					sb.setLength(0);
					prevChannelName = observer.getDiscoChannel().getName();
				}
				sb.append(observer.getUsername() + "\n");
			}
			em.addField("\\#" + prevChannelName, sb.toString(), false)
			.setTitle("Twitch.tv feed list", null)
			.setColor(Color.CYAN);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
