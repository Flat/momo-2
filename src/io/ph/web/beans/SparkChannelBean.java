package io.ph.web.beans;

import java.util.ArrayList;
import java.util.List;

import io.ph.bot.Bot;
import net.dv8tion.jda.core.entities.Channel;

public class SparkChannelBean {
	private String id;
	private String name;
	private int position;

	private SparkChannelBean(Channel ch) {
		this.id = ch.getId();
		this.name = ch.getName();
		this.position = ch.getPosition();
	}
	public static List<SparkChannelBean> getChannels(String guildId) {
		List<SparkChannelBean> toReturn = new ArrayList<SparkChannelBean>();
		Bot.getInstance().shards.getGuildById(guildId).getTextChannels().stream()
			.forEach(ch -> toReturn.add(new SparkChannelBean(ch)));
		return toReturn;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
}
