package io.ph.web.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.feed.TwitchEventListener;
import io.ph.bot.feed.TwitchFeedObserver;
import net.dv8tion.jda.core.entities.TextChannel;

public class SparkTwitchBean {
	private String channelId;
	private String channelName;
	private String twitchHandle;

	private SparkTwitchBean(TextChannel ch, String twitchHandle) {
		this.channelId = ch.getId();
		this.channelName = ch.getName();
		this.twitchHandle = twitchHandle;
	}

	public static List<SparkTwitchBean> getTwitchForguild(String guildId) {
		List<SparkTwitchBean> toReturn = new ArrayList<SparkTwitchBean>();
		for(List<TwitchFeedObserver> list : TwitchEventListener.getFeed().values()) {
			list.stream()
			.filter(t -> Bot.getInstance().shards.getTextChannelById(t.getDiscoChannelId()) != null && 
			Bot.getInstance().shards.getTextChannelById(t.getDiscoChannelId()).getGuild().getId().equals(guildId))
			.forEach(t -> toReturn.add(new SparkTwitchBean(t.getDiscoChannel(), t.getUsername())));
		}
		Collections.sort(toReturn, (t1, t2) -> 
		Bot.getInstance().shards.getTextChannelById(t1.getChannelId()).getPosition() 
		- Bot.getInstance().shards.getTextChannelById(t2.getChannelId()).getPosition());
		return toReturn;
	}

	public static List<SparkTwitchBean> getTwitchForChannel(String channelId) {
		List<SparkTwitchBean> toReturn = new ArrayList<SparkTwitchBean>();
		for(List<TwitchFeedObserver> list : TwitchEventListener.getFeed().values()) {
			list.stream()
			.filter(t -> t.getDiscoChannelId().equals(channelId))
			.forEach(t -> toReturn.add(new SparkTwitchBean(t.getDiscoChannel(), t.getUsername())));
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
	 * @return the twitch username
	 */
	public String getTwitchHandle() {
		return twitchHandle;
	}

}
