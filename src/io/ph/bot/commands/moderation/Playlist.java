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
		description = "Create and manage the guild's idle playlist",
		example = "" // TODO: examples/documentation
		)
public class Playlist extends Command {

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
			if (g.getMusicPlaylist().size() <= 50) {
				g.getMusicPlaylist().add(new PlaylistEntity(title, url));
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
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
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("You need to specify a number to delete from your playlist.\n`"
						+ g.getConfig().getCommandPrefix() + "playlist list` will show you your songs");
			} else if ((index = Integer.parseInt(split[1])) > g.getMusicPlaylist().size() || index < 1) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Given input is out of bounds of 1 - " + g.getMusicPlaylist().size());
			} else {
				PlaylistEntity e = g.getMusicPlaylist().remove(index);
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("Removed " + e.getTitle() + " from your playlist");
				g.saveMusicPlaylist(msg.getGuild().getId());
			}
		} else if (param.equals("list")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < g.getMusicPlaylist().size(); i++) {
				sb.append((i + 1) + ": " + g.getMusicPlaylist().get(i).getTitle() + "\n");
			}
			em.setTitle("Playlist", null)
			.setColor(Color.MAGENTA)
			.setDescription(sb.toString());
			msg.getChannel().sendMessage(em.build()).queue();
		} else if (param.equals("play")) {
			// Just queue up all the songs I guess
			// TODO: this and test
		}

	}

}
