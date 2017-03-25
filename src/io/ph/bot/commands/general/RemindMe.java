package io.ph.bot.commands.general;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Set a reminder to be PM'd a message by the bot
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "remindme",
		aliases = {"remind"},
		permission = Permission.NONE,
		description = "Designate a time in the future to be PM'd a reminder\n"
				+ "This requires a time in the format of #w#d#h#m. "
				+ "For example, 3d2h5m will set a reminder for 3 days, 2 hours, and 5 minutes from now\n"
				+ "Reminders are accurate to within 20 seconds",
		example = "1d2h It is now 1 day and 2 hours from when I set this reminder!"
		)
public class RemindMe extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		if(msg.getContent().split(" ").length < 3) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		String reminderContents = Util.getCommandContents(Util.getCommandContents(msg));
		if(reminderContents.length() > 500) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Maximum reminder length is 500 characters. Yours is " 
			+ reminderContents.length());
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		String param = Util.getParam(msg);
		Instant now = Instant.now();
		Instant target = Util.resolveInstantFromString(param);
		
		long months = ChronoUnit.MONTHS.between(LocalDateTime.ofInstant(now, ZoneId.systemDefault()), 
				LocalDateTime.ofInstant(target, ZoneId.systemDefault()));
		if(months > 5) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("You can only set reminders up to 6 months in the future");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			String sql = "INSERT INTO `global_reminders` (user_id, guild_name, reminder, remind_time) VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, msg.getAuthor().getId());
			stmt.setString(2, msg.getGuild().getName());
			stmt.setString(3, reminderContents);
			stmt.setString(4, target.toString());
			stmt.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
		em.setTitle("Success", null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
		.setDescription("You will be reminded at...");
		em.setTimestamp(target)
		.setFooter("Local time", null);
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
