package io.ph.bot.jobs;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import net.dv8tion.jda.core.EmbedBuilder;

/**
 * Periodically remind users of things they set for the future
 * @author Paul
 *
 */
public class ReminderJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Instant start = Instant.now();
		//LocalDateTime now = LocalDateTime.now(); 
		Connection conn = null;
		PreparedStatement prep = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT user_id, guild_name, reminder, remind_time FROM `global_reminders`");
			while(rs.next()) {
				//LocalDateTime dueDate = LocalDateTime.parse(rs.getString(4));
				Instant dueDate = Instant.parse(rs.getString(4));
				if(start.isBefore(dueDate))
					continue;

				String userId = rs.getString(1);
				String guildName = rs.getString(2);
				String reminder = rs.getString(3);
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("Reminder from " + guildName, null)
				.setColor(Color.CYAN)
				.setDescription(reminder)
				.setTimestamp(Instant.now());
				try {
					Bot.getInstance().shards.getUserById(userId).openPrivateChannel().queue(ch -> {
						ch.sendMessage(em.build()).queue();
					});
				} catch (Exception e) { }
				String sql = "DELETE FROM `global_reminders` WHERE user_id = ? AND remind_time = ?";
				prep = conn.prepareStatement(sql);
				prep.setString(1, userId);
				prep.setString(2, rs.getString(4));
				prep.execute();
				SQLUtils.closeQuietly(prep);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(prep);
			SQLUtils.closeQuietly(conn);
			long gap = TimeUnit.MILLISECONDS.toSeconds(Duration.between(start, Instant.now()).toMillis());
			if(Bot.DEBUG)
				LoggerFactory.getLogger(ReminderJob.class).info("Checked global reminders. Duration: {} seconds", gap);
		}

	}

}
