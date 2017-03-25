package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.events.UserMutedEvent;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
/**
 * Mute a user
 * Can mute for a temporary amount of time
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "mute",
		aliases = {},
		permission = Permission.KICK,
		description = "Assign the Muted role to a user. Can be temporary by using the \"temp\" parameter",
		example = "temp 1d2h target"
		)
public class Mute extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(OffsetDateTime.now());
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if (GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getMutedRoleId().isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like this server doesn't have a designated muted role.\n"
					+ "You can generate one automatically by running `" 
					+ g.getConfig().getCommandPrefix() + "setup` if you have "
					+ "the Manage Servers permission");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		if (Util.getCommandContents(msg).isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No target specified");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		String t = Util.getCommandContents(msg);
		if (t.equals("") || (Util.getParam(msg).equalsIgnoreCase("temp") && t.split(" ").length < 3)) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		Member target;
		String targetS = null;
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
		Instant now = null;
		if (Util.getParam(msg).equalsIgnoreCase("temp")) {
			now = Util.resolveInstantFromString(Util.getParam(t));
			if (msg.getMentionedUsers().isEmpty()) {
				targetS = Util.getCommandContents(Util.getCommandContents(t));
				target = Util.resolveMemberFromMessage(targetS, msg.getGuild());
			} else
				target = msg.getGuild().getMember(msg.getMentionedUsers().get(0));
			if (target == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("No user found for **" + targetS == null ? target.getEffectiveName() : targetS + "**");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			em.setDescription(target.getEffectiveName() + " has been muted until...")
			.setTimestamp(now)
			.setFooter("Local time", null);
		} else {
			target = Util.resolveMemberFromMessage(msg);
			if (target == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("No user found for **" + t + "**");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			em.setDescription(target.getEffectiveName() + " has been muted");
		}
		Role targetRole = msg.getGuild()
				.getRoleById(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getMutedRoleId());

		if (msg.getGuild().getMember(msg.getAuthor()).getRoles().contains(targetRole)) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(target.getEffectiveName() + " is already muted!");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}

		target.getGuild().getController().addRolesToMember(target, targetRole).queue(success -> {
			Bot.getInstance().getEventDispatcher()
			.dispatch(new UserMutedEvent(msg.getGuild(), msg.getAuthor(), target.getUser()));
			msg.getChannel().sendMessage(em.build()).queue();
		}, failure -> {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(failure.getMessage());
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		});
		if (Util.getParam(msg).equalsIgnoreCase("temp")) {
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = ConnectionPool.getGlobalDatabaseConnection();
				String sql = "INSERT INTO `global_punish` (muted_id, muter_id, guild_id, unmute_time, type) VALUES (?,?,?,?,?)";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, target.getUser().getId());
				stmt.setString(2, msg.getAuthor().getId());
				stmt.setString(3, msg.getGuild().getId());
				stmt.setString(4, now.toString());
				stmt.setString(5, "mute");
				stmt.execute();
			} catch(SQLException e) {
				if (e.getErrorCode() == 19) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription(target.getEffectiveName() + " is already under a temporary mute");
					msg.getChannel().sendMessage(em.build()).queue();
				} else
					e.printStackTrace();
				return;
			} finally {
				SQLUtils.closeQuietly(stmt);
				SQLUtils.closeQuietly(conn);
			}
		}

	}
}
