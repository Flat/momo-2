package io.ph.web.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.RedditFeedObserver;
import net.dv8tion.jda.core.entities.Channel;

public class SparkRedditBean {
	private String channelId;
	private String channelName;
	private String subreddit;
	
	private SparkRedditBean(Channel ch, String subreddit) {
		this.channelId = ch.getId();
		this.channelName = ch.getName();
		this.subreddit = subreddit;
	}
	
	public static List<SparkRedditBean> getSubredditsForGuildId(String guildId) {
		List<SparkRedditBean> toReturn = new ArrayList<SparkRedditBean>();
		for(List<RedditFeedObserver> list : RedditEventListener.getFeed().values()) {
			list.stream()
			.filter(r -> Bot.getInstance().shards.getTextChannelById(r.getDiscoChannelId()) != null
			&& Bot.getInstance().shards.getTextChannelById(r.getDiscoChannelId()).getGuild().getId().equals(guildId))
			.forEach(r -> toReturn.add(new SparkRedditBean(r.getDiscoChannel(), r.getSubreddit())));
		}
		Collections.sort(toReturn, (r1, r2) -> 
				Bot.getInstance().shards.getTextChannelById(r1.getChannelId()).getPosition() 
				- Bot.getInstance().shards.getTextChannelById(r2.getChannelId()).getPosition());
		return toReturn;
	}
	
	public static List<SparkRedditBean> getSubredditsForChannelId(String channelId) {
		List<SparkRedditBean> toReturn = new ArrayList<SparkRedditBean>();
		for(List<RedditFeedObserver> list : RedditEventListener.getFeed().values()) {
			list.stream()
			.filter(r -> r.getDiscoChannelId().equals(channelId))
			.forEach(r -> toReturn.add(new SparkRedditBean(r.getDiscoChannel(), r.getSubreddit())));
		}
		return toReturn;
	}
	/**
	 * @return the channelId
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * @return the channelName
	 */
	public String getChannelName() {
		return channelName;
	}

	/**
	 * @return the subreddit
	 */
	public String getSubreddit() {
		return subreddit;
	}
}
