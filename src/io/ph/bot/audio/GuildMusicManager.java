package io.ph.bot.audio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.bot.Bot;
import io.ph.bot.model.GuildObject;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public class GuildMusicManager {
	private AudioPlayer audioPlayer;
	private GuildTrackManager trackManager;
	private Set<String> skipVoters;

	public GuildMusicManager(AudioPlayerManager manager, String guildId) {
		this.audioPlayer = manager.createPlayer();
		this.trackManager = new GuildTrackManager(audioPlayer, guildId);
		this.audioPlayer.addListener(trackManager);
		this.skipVoters = new HashSet<String>();
	}

	public static void loadGuildPlaylist(TextChannel channel, Member member) {
		GuildObject g = GuildObject.guildMap.get(channel.getGuild().getId());
		for (PlaylistEntity p : g.getMusicPlaylist()) {
			loadAndPlay(channel, p.getUrl(), null, member, false);
		}
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(member, Color.GREEN))
		.setDescription("Queued up your playlist!");
		channel.sendMessage(em.build()).queue();
	}

	public static void loadAndPlay(final TextChannel channel, final String trackUrl, 
			final String titleOverride, final Member member, boolean announce) {
		GuildMusicManager musicManager = AudioManager.getGuildManager(channel.getGuild());
		AudioManager.getMasterManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			EmbedBuilder em = new EmbedBuilder();
			@Override
			public void trackLoaded(AudioTrack track) {
				if(track.getDuration() / 1000 > (Bot.getInstance().getConfig().getMaxSongLength() * 60)
						&& announce) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription(String.format("Song duration too long. Please keep length under %d minutes",
							Bot.getInstance().getConfig().getMaxSongLength()));
					channel.sendMessage(em.build()).queue();
					return;
				}
				if (announce) {
					em.setTitle("Music queued", null)
					.setColor(Util.resolveColor(member, Color.GREEN))
					.setDescription(String.format("%s was queued by %s",  
							(titleOverride == null || titleOverride.startsWith("ytsearch"))
							? track.getInfo().title : titleOverride,
									member.getEffectiveName()))
					.setFooter(String.format("Place in queue: %d | Time until play: %s",
							AudioManager.getGuildManager(channel.getGuild())
							.getTrackManager().getQueueSize(),
							Util.formatTime(AudioManager.getGuildManager(channel.getGuild())
									.getTrackManager().getDurationOfQueue() + 
									(musicManager.getAudioPlayer().getPlayingTrack() == null 
									? 0 : (musicManager.getAudioPlayer().getPlayingTrack().getDuration()
											- musicManager.getAudioPlayer().getPlayingTrack().getPosition())))), null);
					channel.sendMessage(em.build()).queue();
				}
				play(channel.getGuild(), track, trackUrl, titleOverride, member);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
				if (playlist.isSearchResult()) {
					trackLoaded(playlist.getTracks().get(0));
					return;
				}
				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				} else {
					trackLoaded(firstTrack);
					return;
				}

				em.setTitle("Playlist queued", null)
				.setColor(Util.resolveColor(member, Color.GREEN))
				.setDescription("Playlist *" + playlist.getName() + "* queued by " + member.getEffectiveName())
				.setFooter(String.format("Playlist size: %d | Queue size: %d",
						playlist.getTracks().size(),
						AudioManager.getGuildManager(channel.getGuild()).getTrackManager().getQueueSize()
						+ playlist.getTracks().size()), null);
				channel.sendMessage(em.build()).queue();
				playlist.getTracks().stream()
				.forEach(t -> {
					if(t.getDuration() / 1000 < (Bot.getInstance().getConfig().getMaxSongLength() * 60))
						play(channel.getGuild(), t, trackUrl, titleOverride, member);
				});
			}

			@Override
			public void noMatches() {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Error queueing your track - not found");
				channel.sendMessage(em.build()).queue();
			}

			@Override
			public void loadFailed(FriendlyException e) {
				em.setTitle("Error loading", null)
				.setColor(Color.RED)
				.setDescription("Error loading and playing: " + e.getMessage());
				e.printStackTrace();
				channel.sendMessage(em.build()).queue();
			}
		});
	}

	private static void play(Guild guild, AudioTrack track, String trackUrl, String titleOverride, Member member) {
		TrackDetails details = new TrackDetails(trackUrl, titleOverride, member, track, guild.getId());
		AudioManager.getGuildManager(guild).trackManager.queue(details);
	}

	public GuildTrackManager getTrackManager() {
		return this.trackManager;
	}

	public AudioPlayer getAudioPlayer() {
		return this.audioPlayer;
	}

	public int getSkipVotes() {
		return skipVoters.size();
	}

	public Set<String> getSkipVoters() {
		return skipVoters;
	}

	public void reset() {
		this.skipVoters.clear();
		this.getTrackManager().getQueue().clear();
		this.audioPlayer.stopTrack();
	}

	/**
	 * Shuffle the queue
	 */
	public void shuffle() {
		List<TrackDetails> temp = new ArrayList<TrackDetails>();
		this.trackManager.getQueue().drainTo(temp);
		Collections.shuffle(temp);
		temp.stream().forEach(t -> this.trackManager.getQueue().offer(t));
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(audioPlayer);
	}
}
