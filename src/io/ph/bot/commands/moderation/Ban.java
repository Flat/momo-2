package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
/**
 * Ban a user
 * Can ban for a temporary amount of time
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "ban",
		aliases = {"b"},
		permission = Permission.BAN,
		description = "Ban a user. Can be temporary by using the \"temp\" parameter",
		example = "temp 1w2d username"
		)
public class Ban extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(Instant.now());
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
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
		Instant now = null;
		if (Util.getParam(msg).equalsIgnoreCase("temp")) {
			now = Util.resolveInstantFromString(Util.getParam(t));
			String contents = Util.getCommandContents(Util.getCommandContents(t));
			target = Util.resolveMemberFromMessage(contents, msg.getGuild());
			if (target == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("No user found for **" + contents + "**");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			em.setDescription(target.getEffectiveName() + " has been banned until...")
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
			em.setDescription(target.getEffectiveName() + " has been banned");
		}
		try {
			if (msg.getGuild().getController().getBans().complete(true).contains(target)) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(target.getEffectiveName() + " is already banned");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}

			msg.getGuild().getController().ban(target, 0).queue(success -> {
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
					stmt.setString(5, "ban");
					stmt.execute();
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					SQLUtils.closeQuietly(stmt);
					SQLUtils.closeQuietly(conn);
				}
			}
		} catch (RateLimitedException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Please try again");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
	}

}
