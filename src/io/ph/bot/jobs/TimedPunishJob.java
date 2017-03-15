package io.ph.bot.jobs;

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
import io.ph.bot.events.UserUnmutedEvent;
import io.ph.bot.model.GuildObject;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * Periodically check to unban timed punishes (mute/bans)
 * @author Paul
 *
 */
public class TimedPunishJob implements Job {

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
			rs = stmt.executeQuery("SELECT muted_id, guild_id, unmute_time, type FROM `global_punish`");
			while(rs.next()) {
				//LocalDateTime dueDate = LocalDateTime.parse(rs.getString(4));
				Instant dueDate = Instant.parse(rs.getString(3));
				if(start.isBefore(dueDate))
					continue;

				String userId = rs.getString(1);
				String guildId = rs.getString(2);
				String type = rs.getString(4);
				Guild g = Bot.getInstance().shards.getGuildById(guildId);
				switch(type) {
				case "ban":
					g.getController().unban(userId).queue();
					break;
				case "mute":
					try {
						Member u = g.getMemberById(userId);
						if (u == null)
							break;
						u.getGuild().getController().removeRolesFromMember(u, g.getRoleById(GuildObject
								.guildMap.get(g.getId()).getConfig().getMutedRoleId())).queue(success -> {
									Bot.getInstance().getEventDispatcher()
									.dispatch(new UserUnmutedEvent(g, u.getUser()));
								});
					} catch (NullPointerException e) {

					}
					break;
				}

				String sql = "DELETE FROM `global_punish` WHERE muted_id = ? AND unmute_time = ?";
				prep = conn.prepareStatement(sql);
				prep.setString(1, userId);
				prep.setString(2, rs.getString(3));
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
				LoggerFactory.getLogger(TimedPunishJob.class)
				.info("Checked global timed mutes/bans. Duration: {} seconds", gap);
		}

	}

}
