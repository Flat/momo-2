package io.ph.web.beans;

import java.util.ArrayList;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.GuildObject.SpecialChannels;

public class SparkVoiceChannelBean {
	private String id;
	private String name;
	private boolean musicChannel;
	private int position;
	
	private SparkVoiceChannelBean(String id, String name, int position) {
		this.id = id;
		this.name = name;
		this.position = position;
		this.musicChannel = false;
	}
	
	public static List<SparkVoiceChannelBean> getChannels(String guildId) {
		List<SparkVoiceChannelBean> toReturn = new ArrayList<SparkVoiceChannelBean>();
		SpecialChannels spc = GuildObject.guildMap.get(guildId).getSpecialChannels();
		Bot.getInstance().shards.getGuildById(guildId).getVoiceChannels().stream()
		.forEach(ch -> {
			SparkVoiceChannelBean bean = new SparkVoiceChannelBean(ch.getId(), ch.getName(), ch.getPosition());
			if(ch.getId().equals(spc.getMusicVoice())) {
				bean.setMusicChannel(true);
			}
			toReturn.add(bean);
		});
		return toReturn;
	}

	/**
	 * @return the musicChannel
	 */
	public boolean getMusicChannel() {
		return musicChannel;
	}

	/**
	 * @param musicChannel the music channel to set
	 */
	public void setMusicChannel(boolean musicChannel) {
		this.musicChannel = musicChannel;
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
