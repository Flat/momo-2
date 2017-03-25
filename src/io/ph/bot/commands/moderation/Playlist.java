package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.audio.PlaylistEntity;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Create and manage an idle playlist
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "playlist",
		aliases = {},
		permission = Permission.KICK,
		description = "Create and manage the guild's playlist\n"
				+ "Each server gets a playlist unique to it which you can add or remove songs to. "
				+ "Sources can range from direct URLs to Youtube to Soundcloud",
				example = "add link title-of-song (add a song with given link)\n"
						+ "list (list your playlist)\n"
						+ "remove # (remove a song from your playlist at given index)\n"
						+ "remove all (remove all songs from playlist)"
		)
public class Playlist extends Command {
	private static final int MAX_SIZE = 100;
	@Override
	public void executeCommand(Message msg) {
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		EmbedBuilder em = new EmbedBuilder();
		String param = split[0];
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if (param.equals("add")) {
			// Require "add link title"
			if (split.length < 3) {
				MessageUtils.sendIncorrectCommandUsage(msg, this);
				return;
			}
			String url = split[1];
			String title = Util.combineStringArray(Util
					.removeFirstArrayEntry(Util.removeFirstArrayEntry(split)));
			if (g.getMusicPlaylist().size() <= MAX_SIZE) {
				g.getMusicPlaylist().add(new PlaylistEntity(title, url));
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Added " + title + " to your playlist");
				g.saveMusicPlaylist(msg.getGuild().getId());
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Your playlist is at max capacity!");
			}
		} else if (param.equals("delete") || param.equals("remove")) {
			int index;
			if (!Util.isInteger(split[1])) {
				if (split[1].equalsIgnoreCase("all")) {
					em.setTitle("Success", null)
					.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
					.setDescription("Cleared your playlist");
					g.getMusicPlaylist().clear();
					g.saveMusicPlaylist(msg.getGuild().getId());
				} else {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("You need to specify a number to delete from your playlist.\n`"
							+ g.getConfig().getCommandPrefix() + "playlist list` will show you your songs");
				}
			} else if ((index = Integer.parseInt(split[1]) - 1) >= g.getMusicPlaylist().size() || index < 0) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Given input is out of bounds of 1 - " + g.getMusicPlaylist().size());
			} else {
				PlaylistEntity e = g.getMusicPlaylist().remove(index);
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Removed " + e.getTitle() + " from your playlist");
				g.saveMusicPlaylist(msg.getGuild().getId());
			}
		} else if (param.equals("list")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < g.getMusicPlaylist().size(); i++) {
				sb.append(String.format("**(%d)** %s\n", i + 1, g.getMusicPlaylist().get(i).getTitle()));
			}
			em.setTitle("Playlist", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA))
			.setDescription(sb.toString());
		}
		if (!em.isEmpty()) {
			msg.getChannel().sendMessage(em.build()).queue();
		}

	}

}
