package io.ph.bot.audio;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import io.ph.bot.Bot;
import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class GuildTrackManager extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<TrackDetails> queue;
	private TrackDetails currentSong;
	private String guildId;

	/**
	 * @param player The audio player this scheduler uses
	 */
	public GuildTrackManager(AudioPlayer player, String guildId) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.guildId = guildId;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(TrackDetails track) {
		if(this.player.getPlayingTrack() == null) {
			this.currentSong = track;
			player.startTrack(track.getTrack(), false);
		} else {
			queue.offer(track);
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		player.startTrack((currentSong = queue.poll()).getTrack(), false);
	}

	/**
	 * Next track if queue isn't empty, stop if not
	 */
	public void skipTrack() {
		player.stopTrack();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		Guild guild = Bot.getInstance().shards.getGuildById(this.guildId);
		GuildObject g = GuildObject.guildMap.get(this.guildId);
		g.getMusicManager().getSkipVoters().clear();
		if (!queue.isEmpty()) {
			// Kill queue and leave channel if no one is in
			if (guild.getAudioManager().getConnectedChannel().getMembers().size() == 1) {
				guild.getAudioManager().closeAudioConnection();
				g.getMusicManager().reset();
				return;
			}
			nextTrack();
		} else {
			TextChannel ch;
			if(this.currentSong != null && (ch = Bot.getInstance().shards
					.getTextChannelById(g.getSpecialChannels().getMusic())) != null) {
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Queue finished!", null)
				.setColor(Color.MAGENTA)
				.setDescription("Your queue is all dried up");
				ch.sendMessage(em.build()).queue();
			}
			guild.getAudioManager().closeAudioConnection();
			currentSong = null;
		}
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		TextChannel ch;
		if((ch = Bot.getInstance().shards
				.getTextChannelById(GuildObject.guildMap.get(this.guildId)
						.getSpecialChannels().getMusic())) != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.setTitle("New track: " + track.getInfo().title, track.getInfo().uri)
			.setColor(Color.MAGENTA)
			.setDescription(String.format("%s, **%s** is now playing\n"
					+ "%s", this.currentSong.getQueuer().getAsMention(),
					track.getInfo().title, track.getInfo().uri));
			ch.sendMessage(em.build()).queue();
		}
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		if(!queue.isEmpty())
			nextTrack();
	}

	public BlockingQueue<TrackDetails> getQueue() {
		return this.queue;
	}

	/**
	 * Returns the duration of the queue
	 * @return Duration of queue
	 */
	public long getDurationOfQueue() {
		return this.queue.stream().mapToLong(t -> t.getTrack().getDuration()).sum();
	}

	/**
	 * Get the true queue size
	 * @return Current song + rest of queue
	 */
	public int getQueueSize() {
		return this.queue.size() + (currentSong == null ? 0 : 1);
	}

	/**
	 * Check if empty. Entails checking if current song is null & queue is empty
	 * @return True if nothing playing, false if something is playing or is queued
	 */
	public boolean isEmpty() {
		return this.currentSong == null && this.queue.isEmpty();
	}

	/**
	 * Get the current song that is playing
	 * @return TrackDetails of current song
	 */
	public TrackDetails getCurrentSong() {
		return currentSong;
	}

	/**
	 * Clear the current song without actually skipping
	 */
	public void clearCurrentSong() {
		this.currentSong = null;
	}
}