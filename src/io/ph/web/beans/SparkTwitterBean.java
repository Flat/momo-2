package io.ph.web.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.feed.TwitterFeedObserver;
import net.dv8tion.jda.core.entities.Channel;

public class SparkTwitterBean {
	private String channelId;
	private String channelName;
	private String twitterHandle;

	private SparkTwitterBean(Channel ch, String twitterHandle) {
		this.channelId = ch.getId();
		this.channelName = ch.getName();
		this.twitterHandle = twitterHandle;
	}

	public static List<SparkTwitterBean> getTwitterForGuild(String guildId) {
		List<SparkTwitterBean> toReturn = new ArrayList<SparkTwitterBean>();
		for(List<TwitterFeedObserver> list : TwitterEventListener.getFeed().values()) {
			list.stream()
			.filter(t -> Bot.getInstance().shards.getTextChannelById(t.getDiscoChannelId()) != null && 
			Bot.getInstance().shards.getTextChannelById(t.getDiscoChannelId()).getGuild().getId().equals(guildId))
			.forEach(t -> toReturn.add(new SparkTwitterBean(t.getDiscoChannel(), t.getTwitterHandle())));
		}
		Collections.sort(toReturn, (t1, t2) -> 
		Bot.getInstance().shards.getTextChannelById(t1.getChannelId()).getPosition() 
		- Bot.getInstance().shards.getTextChannelById(t2.getChannelId()).getPosition());
		return toReturn;
	}

	public static List<SparkTwitterBean> getTwitterForChannel(String channelId) {
		List<SparkTwitterBean> toReturn = new ArrayList<SparkTwitterBean>();
		for(List<TwitterFeedObserver> list : TwitterEventListener.getFeed().values()) {
			list.stream()
			.filter(t -> t.getDiscoChannelId().equals(channelId))
			.forEach(t -> toReturn.add(new SparkTwitterBean(t.getDiscoChannel(), t.getTwitterHandle())));
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
	 * @return the twitterHandle
	 */
	public String getTwitterHandle() {
		return twitterHandle;
	}

}
