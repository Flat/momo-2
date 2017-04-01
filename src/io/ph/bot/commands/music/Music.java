package io.ph.bot.commands.music;

import java.awt.Color;
import java.util.Optional;

import io.ph.bot.audio.AudioManager;
import io.ph.bot.audio.GuildMusicManager;
import io.ph.bot.audio.TrackDetails;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Play music in designated channel
 * @author Paul
 */
@CommandData (
		defaultSyntax = "music",
		aliases = {"play"},
		permission = Permission.NONE,
		description = "Play or get information on the music playlist\n"
				+ "You can also play your guild's music playlist\n"
				+ "If your server has a DJ role, this command is restricted to those in that role or mod+",
				example = "https://youtu.be/dQw4w9WgXcQ\n"
						+ "playlist (plays your guild's playlist)\n"
						+ "now\n"
						+ "next\n"
						+ "skip (kick+ force skips)\n"
						+ "volume (requires kick+)\n"
						+ "shuffle (requires kick+)\n"
						+ "stop (requires kick+)"
		)
public class Music extends Command {
	@Override
	public void executeCommand(Message msg) {
		final EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		String titleOverride = null;
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		net.dv8tion.jda.core.managers.AudioManager audio = msg.getGuild().getAudioManager();
		boolean djSet = !g.getConfig().getDjRoleId().isEmpty();
		if ((djSet && !msg.getGuild().getMember(msg.getAuthor())
				.getRoles().contains(msg.getGuild().getRoleById(g.getConfig().getDjRoleId())))
				&& !Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)
				&& !(contents.startsWith("now") || contents.startsWith("next")
						|| contents.startsWith("list"))) {
			// Opting to fail silently here
			return;
		}
		Optional<VoiceChannel> opt;
		// First, check if the guild has a designated music channel
		if (!g.getSpecialChannels().getMusicVoice().isEmpty() 
				&& msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()) != null) {
			audio.openAudioConnection(msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()));
		} else if (!audio.isConnected() && !audio.isAttemptingToConnect()) {
			if ((opt = msg.getGuild().getVoiceChannels().stream()
					.filter(v -> v.getMembers().contains(msg.getGuild().getMember(msg.getAuthor())))
					.findAny()).isPresent()) {
				// User is in a channel, calling the play method
				audio.openAudioConnection(opt.get());
			} else {
				// User isn't in a channel, yell at them
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("You must be in a voice channel so I know where to go!");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
		}
		// At this point, we're connected to a voice channel

		// Didn't specify any song, url, attachment
		if (contents.equals("") && msg.getAttachments().isEmpty()) {
			String prefix = g.getConfig().getCommandPrefix();
			em.setTitle(prefix + "music [URL|attachment]", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA))
			.setDescription(String.format("*%s [URL|attachment]* - play a song\n"
					+ "*%<s now* - show current song\n"
					+ "*%<s next* - show playlist of songs\n"
					+ "*%<s skip* - cast a vote to skip this song", prefix + "music"));
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		GuildMusicManager m = g.getMusicManager();
		if (contents.startsWith("skip")) {
			skip(msg, djSet);
			return;
		} else if (contents.startsWith("now")) {
			now(msg);
			return;
		} else if (contents.startsWith("next") || contents.startsWith("list")) {
			next(msg);
			return;
		} else if (contents.startsWith("stop") || contents.startsWith("clear")
				|| contents.startsWith("reset")) {
			stop(msg, djSet);
			return;
		} else if (contents.startsWith("shuffle")) {
			shuffle(msg, djSet);
			return;
		} else if (contents.startsWith("volume")) {
			volume(msg, Util.getCommandContents(Util.getCommandContents(msg)), djSet);
			return;
		} else if (contents.startsWith("playlist")) {
			// Just queue up all the songs I guess
			if (!g.getSpecialChannels().getMusicVoice().isEmpty() 
					&& msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()) != null) {
				audio.openAudioConnection(msg.getJDA().getVoiceChannelById(g.getSpecialChannels().getMusicVoice()));
			} else if (!audio.isConnected() && !audio.isAttemptingToConnect()) {
				if ((opt = msg.getGuild().getVoiceChannels().stream()
						.filter(v -> v.getMembers().contains(msg.getGuild().getMember(msg.getAuthor())))
						.findAny()).isPresent()) {
					// User is in a channel, calling the play method
					audio.openAudioConnection(opt.get());
				} else {
					// User isn't in a channel, yell at them
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("You must be in a voice channel so I know where to go!");
					msg.getChannel().sendMessage(em.build()).queue();
					return;
				}
			}
			GuildMusicManager.loadGuildPlaylist(msg.getTextChannel(), msg.getGuild().getMember(msg.getAuthor()));
		} else if (Util.isInteger(contents)) {
			int index = Integer.parseInt(contents);
			if ((index) > g.getHistoricalSearches().getHistoricalMusic().size() || index < 1) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Giving a number will play music for a previous search. This # is too large");
				return;
			}
			String[] historicalResult = g
					.getHistoricalSearches().getHistoricalMusic().get(index);
			titleOverride = historicalResult[0];
			contents = historicalResult[1];
		} else if (!Util.isValidUrl(contents)) {
			contents = "ytsearch: " + contents;
			titleOverride = contents;
		}
		if (!msg.getAttachments().isEmpty()) {
			contents = msg.getAttachments().get(0).getUrl();
		}
		contents = contents.replaceAll("^<|>$", "");
		GuildMusicManager.loadAndPlay(msg.getTextChannel(), 
				contents, titleOverride, msg.getGuild().getMember(msg.getAuthor()), true);
	}
	
	public static void skip(Message msg, boolean...bs) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		net.dv8tion.jda.core.managers.AudioManager audio = msg.getGuild().getAudioManager();
		GuildMusicManager m = g.getMusicManager();
		EmbedBuilder em = new EmbedBuilder();
		boolean djSet = bs.length > 0 ? bs[0] : false;
		if (m.getSkipVoters().contains(msg.getAuthor().getId())) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You have already voted to skip!");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		if (m.getAudioPlayer().getPlayingTrack() == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No song is currently playing");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		if (!audio.getConnectedChannel().getMembers().contains(msg.getGuild().getMember(msg.getAuthor()))) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You cannot vote if you aren't listening");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		int current = audio.getConnectedChannel().getMembers().size();
		int currentVotes = m.getSkipVotes();
		if (current <= 0)
			current = 1;
		int maxVotes = (int) Math.floor(current/2);
		if (maxVotes > 5)
			maxVotes = 5;
		if (++currentVotes >= maxVotes 
				|| Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)
				|| djSet) {
			m.getSkipVoters().clear();
			if (currentVotes >= maxVotes) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Vote to skip passed");
			} else {
				em.setTitle("Force skip", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Force skipped by " + msg.getGuild().getMember(msg.getAuthor()).getEffectiveName());
			}
			m.getTrackManager().skipTrack();
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		} else {
			m.getSkipVoters().add(msg.getAuthor().getId());
			em.setTitle("Voted to skip", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription("Votes needed to pass: " + currentVotes + "/" + maxVotes);
			msg.getChannel().sendMessage(em.build()).queue();
		}
	}
	
	public static void now(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		GuildMusicManager m = g.getMusicManager();
		EmbedBuilder em = new EmbedBuilder();
		if (m.getAudioPlayer() == null 
				|| m.getAudioPlayer().getPlayingTrack() == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No song currently playing");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		em.setTitle("Current track", null)
		.setColor(Color.CYAN)
		.addField("Name", (m.getTrackManager().getCurrentSong().getTitle() == null
		|| m.getTrackManager().getCurrentSong().getTitle().contains("ytsearch")) ? 
				m.getAudioPlayer().getPlayingTrack().getInfo().title :
					m.getTrackManager().getCurrentSong().getTitle(), true)
		.addField("Progress", Util.formatTime(m.getAudioPlayer().getPlayingTrack().getPosition())
				+ "/" + Util.formatTime(m.getAudioPlayer().getPlayingTrack().getDuration()), true)
		.addField("Source", m.getAudioPlayer().getPlayingTrack().getInfo().uri, false);
		msg.getChannel().sendMessage(em.build()).queue();
	}
	
	public static void next(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		GuildMusicManager m = g.getMusicManager();
		EmbedBuilder em = new EmbedBuilder();
		
		if (m.getAudioPlayer() == null 
				|| m.getAudioPlayer().getPlayingTrack() == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No song currently playing");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		em.setTitle(String.format("Coming up - %d songs | %s total",
				m.getTrackManager().getQueue().size(),
				Util.formatTime(m.getTrackManager().getDurationOfQueue())), null)
		.setColor(Color.CYAN);
		int index = 0;
		for(TrackDetails t : AudioManager.getGuildManager(msg.getGuild()).getTrackManager().getQueue()) {
			if (index++ >= 10) {
				em.setFooter("Limited to 10 results", null);
				break;
			}
			em.appendDescription(String.format("%d) **%s** - %s\n", 
					index,
					(t.getTitle() == null || t.getTitle().contains("ytsearch")) ? 
							t.getTrack().getInfo().title :
								t.getTitle(),
								Util.formatTime(t.getTrack().getDuration())));
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
	
	public static void stop(Message msg, boolean...bs) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		GuildMusicManager m = g.getMusicManager();
		EmbedBuilder em = new EmbedBuilder();
		boolean djSet = bs.length > 0 ? bs[0] : false;
		if (!Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)
				&& !djSet) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You need the kick+ permission to stop the queue");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		m.reset();
		em.setTitle("Music stopped", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
		.setDescription("Queue cleared");
		msg.getChannel().sendMessage(em.build()).queue();
	}
	
	public static void shuffle(Message msg, boolean...bs) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		GuildMusicManager m = g.getMusicManager();
		EmbedBuilder em = new EmbedBuilder();
		boolean djSet = bs.length > 0 ? bs[0] : false;
		if (!Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)
				&& !djSet) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You need the kick+ permission to shuffle the queue");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		m.shuffle();
		em.setTitle("Music shuffled", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
		.setDescription("Wow, kerfluffle");
		msg.getChannel().sendMessage(em.build()).queue();
	}
	
	public static void volume(Message msg, String volume, boolean...bs) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		boolean djSet = bs.length > 0 ? bs[0] : false;
		if (!Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)
				&& !djSet) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You need the kick+ permission to change the volume");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		int input;
		if (!Util.isInteger(volume) 
				|| (input = Integer.parseInt(volume)) > 100 || input < 0) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Please set volume between 0 and 100");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
		.setDescription("Set volume to " + input);
		msg.getChannel().sendMessage(em.build()).queue();
		g.getMusicManager().getAudioPlayer().setVolume(input);
	}
}
