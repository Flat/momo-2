package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.RedditFeedObserver;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * List the subreddits this guild is subscribed to
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "listreddit",
		aliases = {"listredditfeeds", "redditlist"},
		permission = Permission.KICK,
		description = "List all reddit feeds for your server",
		example = "(no parameters)"
		)
public class ListReddit extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		List<RedditFeedObserver> thisGuilds = new ArrayList<RedditFeedObserver>();
		for (List<RedditFeedObserver> list : RedditEventListener.getFeed().values()) {
			for (RedditFeedObserver observer : list) {
				if (observer.getDiscoChannel() != null && observer.getDiscoChannel().getGuild().equals(msg.getGuild())) {
					thisGuilds.add(observer);
				}
			}
		}
		if (thisGuilds.isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Your server does not have any reddit feeds setup");
		} else {
			Collections.sort(thisGuilds, (f, s) -> {
				return f.getDiscoChannel().getName().compareTo(s.getDiscoChannel().getName());
			});
			StringBuilder sb = new StringBuilder();
			String prevChannelName = "";
			for (RedditFeedObserver observer : thisGuilds) {
				if (observer.getDiscoChannel() == null)
					continue;
				if (!prevChannelName.equals(observer.getDiscoChannel().getName())) {
					if (prevChannelName.length() > 0)
						em.addField("\\#" + prevChannelName, sb.toString(), false);
					sb.setLength(0);
					prevChannelName = observer.getDiscoChannel().getName();
				}
				sb.append(String.format("/r/%s\n", observer.getSubreddit()));
			}
			em.addField("\\#" + prevChannelName, sb.toString(), false)
			.setTitle("Reddit feed list", null)
			.setColor(Color.CYAN);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
