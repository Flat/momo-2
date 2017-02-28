package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Member;

public class TrackDetails {
	private String url;
	private Member queuer;
	private AudioTrack track;
	private String guildId;
	private String title;

	public TrackDetails(String url, String title, Member queuer, AudioTrack track, String guildId) {
		this.url = url;
		this.title = title;
		this.queuer = queuer;
		this.track = track;
		this.guildId = guildId;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return the queuer
	 */
	public Member getQueuer() {
		return queuer;
	}
	
	/**
	 * @return the audio track
	 */
	public AudioTrack getTrack() {
		return this.track;
	}
	
	/**
	 * @return
	 */
	public String getGuildId() {
		return this.guildId;
	}

	@Override
	public String toString() {
		return "TrackDetails [url=" + url + ", queuer=" + queuer + ", track=" + track + ", guildId=" + guildId + "]";
	}

	public String getTitle() {
		return title;
	}

}
