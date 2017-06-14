package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.bot.audio.stream.StreamSource;
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
	
	/**
	 * Possible null StreamSource of this track
	 * @return null if track isn't stream, StreamSource if it is a stream
	 */
	public StreamSource getStreamSource() {
		if (!track.getInfo().isStream) {
			return null;
		}
		if (track.getInfo().uri.contains("listen.moe")) {
			return StreamSource.LISTEN_MOE;
		}
		if (track.getInfo().uri.contains("youtube")) {
			return StreamSource.YOUTUBE;
		}
		return null;
	}

}
