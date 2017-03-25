package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.events.UserUnmutedEvent;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Unmute a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "unmute",
		aliases = {},
		permission = Permission.KICK,
		description = "Remove the muted role from a user",
		example = "target"
		)
public class Unmute extends Command {

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
		if(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getMutedRoleId().equals("")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like this server doesn't have a designated muted role.\n"
					+ "You can generate one automatically by running `" 
					+ GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix()
					+ "setup` if you have the Manage Servers permission");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Role role = msg.getGuild().getRoleById(GuildObject.guildMap
				.get(msg.getGuild().getId()).getConfig().getMutedRoleId());
		Member target = Util.resolveMemberFromMessage(msg);
		if(target == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No user found for **" + content + "**");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		if(!target.getRoles().contains(role)) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(target.getEffectiveName() + " is not muted");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			String sql = "DELETE FROM `global_punish` WHERE muted_id = ? AND guild_id = ? AND type = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, target.getUser().getId());
			stmt.setString(2, msg.getGuild().getId());
			stmt.setString(3, "mute");
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
		msg.getGuild().getController().removeRolesFromMember(target, role).queue(success -> {
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription(target.getEffectiveName() + " has been unmuted")
			.setFooter("Local time", null);
			msg.getChannel().sendMessage(em.build()).queue();
			
			Bot.getInstance().getEventDispatcher()
			.dispatch(new UserUnmutedEvent(msg.getGuild(), target.getUser()));
		}, failure -> {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(failure.getMessage());
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		});
		

	}

}
