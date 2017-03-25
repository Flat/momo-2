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
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
/**
 * Unban a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "unban",
		aliases = {},
		permission = Permission.BAN,
		description = "Unban a user",
		example = "target"
		)
public class Unban extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(Instant.now());
		if(Util.getCommandContents(msg).isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No target specified");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		String content = Util.getCommandContents(msg);
		User target = Util.resolveBannedUserFromString(content, msg.getGuild());
		if(target == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No banned user found for **" + content + "**");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			String sql = "DELETE FROM `global_punish` WHERE muted_id = ? AND guild_id = ? AND type = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, target.getId());
			stmt.setString(2, msg.getGuild().getId());
			stmt.setString(3, "ban");
			stmt.execute();
		} catch(SQLException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Something went wrong with the database.");
			msg.getChannel().sendMessage(em.build()).queue();
			e.printStackTrace();
			return;
		} finally {
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
		msg.getGuild().getController().unban(target).queue(success -> {
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription(target.getName() + " has been unbanned")
			.setTimestamp(Instant.now())
			.setFooter("Local time", null);
			msg.getChannel().sendMessage(em.build()).queue();
		}, failure -> {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(failure.getMessage());
			msg.getChannel().sendMessage(em.build()).queue();
		});

	}

}
