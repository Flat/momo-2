package io.ph.bot.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import io.ph.bot.Bot;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Member;

public class MacroObject {
	private String fallbackUsername;
	private LocalDate date;
	private String macroName;
	private String macroContent;
	private int hits;
	private String userId;
	private String guildId;

	/**
	 * Constructor for MacroObject
	 * @param fallbackUsername User#getName()
	 * @param macroName Name of macro
	 * @param macroContent Contents of macro
	 * @param hits Hits of macro (set to 0 when creating a new macro)
	 * @param userId UserID of creator
	 * @param guildId guildID of the guild this was created in
	 */
	public MacroObject(String fallbackUsername, String macroName, String macroContent, int hits,
			String userId, String guildId) {
		this.fallbackUsername = fallbackUsername;
		this.date = LocalDate.now(ZoneId.of("America/New_York"));
		this.macroName = macroName.toLowerCase();
		this.macroContent = macroContent;
		this.hits = hits;
		this.userId = userId;
		this.guildId = guildId;
	}

	public MacroObject(String fallbackUsername, String macroName, String macroContent, int hits,
			String userId, String guildId, LocalDate date) {
		this.fallbackUsername = fallbackUsername;
		this.date = date;
		this.macroName = macroName.toLowerCase();
		this.macroContent = macroContent;
		this.hits = hits;
		this.userId = userId;
		this.guildId = guildId;
	}
	public static MacroObject forName(String name, String guildId) throws IllegalArgumentException {
		return forName(name, guildId, false);
	}

	/**
	 * Returns top macro and hits
	 * @param guildId
	 * @return Object array with index 0: hits 1: macro name 2: userid
	 * @throws NoMacroFoundException
	 */
	public static Object[] topMacro(String guildId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionPool.getConnection(guildId);
			stmt = conn.prepareStatement("SELECT hits, macro, user_id FROM `discord_macro` ORDER BY hits DESC LIMIT 1");
			rs = stmt.executeQuery();
			if(!rs.isBeforeFirst())
				return null;
			rs.next();
			return new Object[] {rs.getInt(1), rs.getString(2), rs.getString(3)};
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
		return null;
	}


	/**
	 * Get and return a macro for given macroName
	 * If found, increment the hits
	 * @param name Macro name to search for
	 * @param guildId Guild ID to search in
	 * @return Macro if found
	 * @throws NoMacroFoundException if no macro is found
	 */
	public static MacroObject forName(String name, String guildId, boolean hit) throws IllegalArgumentException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionPool.getConnection(guildId);
			String sql = "SELECT user_created, date_created, content, hits, user_id FROM `discord_macro` WHERE macro = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name.toLowerCase());
			rs = stmt.executeQuery();
			if(!rs.isBeforeFirst()) {
				throw new IllegalArgumentException("No macro found for " + name);
			}
			rs.next();
			if(hit) {
				sql = "UPDATE `discord_macro` SET hits = hits+1 WHERE macro = ?";
				PreparedStatement stmt2 = conn.prepareStatement(sql);
				stmt2.setString(1, name.toLowerCase());
				stmt2.execute();
				SQLUtils.closeQuietly(stmt2);
			}
			return new MacroObject(rs.getString(1), name, rs.getString(3), rs.getInt(4), rs.getString(5),
					guildId, LocalDate.parse(rs.getObject(2).toString()));
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
		return null;
	}

	/**
	 * Delete a macro that is in the database
	 * @param requesterId The user that is requesting the delete
	 * @return True if deleted, false if user doesn't have permissions
	 * Prerequisite: Macro is in the database
	 */
	public boolean delete(String requesterId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql;
			conn = ConnectionPool.getConnection(this.guildId);
			Member m = Bot.getInstance().shards.getGuildById(this.guildId).getMemberById(requesterId);
			//If user isn't a mod, need to check that they made this
			if (!Util.memberHasPermission(m, Permission.KICK)) {
				sql = "SELECT hits FROM `discord_macro` WHERE macro = ? AND user_id = ?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, this.macroName);
				stmt.setString(2, requesterId);
				try {
					rs = stmt.executeQuery();
					if(!rs.isBeforeFirst())
						return false;
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					SQLUtils.closeQuietly(rs);
					SQLUtils.closeQuietly(stmt);
				}
			}

			sql = "DELETE FROM `discord_macro` WHERE macro = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.macroName);
			stmt.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}

	/**
	 * Edit a macro that is in the database
	 * @param requesterId The user that is requesting the edit
	 * @return True if deleted, false if user doesn't have permissions
	 * Prerequisite: Macro is in the database
	 */
	public boolean edit(String requesterId, String newContent) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql;
			conn = ConnectionPool.getConnection(this.guildId);
			Member m = Bot.getInstance().shards.getGuildById(this.guildId).getMemberById(requesterId);
			//If user isn't a mod, need to check that they made this
			if (!Util.memberHasPermission(m, Permission.KICK)) {
				sql = "SELECT hits FROM `discord_macro` WHERE macro = ? AND user_id = ?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, this.macroName);
				stmt.setString(2, requesterId);
				try {
					rs = stmt.executeQuery();
					if(!rs.isBeforeFirst())
						return false;
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					SQLUtils.closeQuietly(rs);
					SQLUtils.closeQuietly(stmt);
				}
			}

			sql = "UPDATE `discord_macro` SET content = ? WHERE macro = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, newContent);
			stmt.setString(2, this.macroName);
			stmt.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}

	/**
	 * Finalize this macro and insert it into the database
	 * @return True if successful, false if key conflict
	 * @throws SQLException  Something broke - check stacktrace
	 */
	public boolean create() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getConnection(this.guildId);
			String sql = "INSERT INTO `discord_macro` (macro, user_created, date_created, content, hits, user_id) VALUES (?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.macroName);
			stmt.setString(2, this.fallbackUsername);
			stmt.setObject(3, this.date);
			stmt.setString(4, this.macroContent);
			stmt.setInt(5, 0);
			stmt.setString(6, this.userId);
			stmt.execute();
			return true;
		} catch(SQLException e) {
			if(e.getErrorCode() == 19) {
				return false;
			}
			throw e;
		} finally {
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}

	/**
	 * Search for a macro by name given the guild ID
	 * @param name Name to wildcard
	 * @param guildId GuildID to search in
	 * @return Null if no results, populated string array of macro names if results
	 */
	public static String[] searchForName(String name, String guildId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> toReturn = new ArrayList<String>(10);
		try {
			conn = ConnectionPool.getConnection(guildId);
			String sql = "SELECT macro FROM `discord_macro` WHERE macro LIKE ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "%" + name + "%");
			rs = stmt.executeQuery();
			if(!rs.isBeforeFirst())
				return null;
			while(rs.next()) {
				toReturn.add(rs.getString(1));
			}
			return toReturn.toArray(new String[0]);
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}

	}

	/**
	 * Search for macros by user and given guild
	 * @param userId UserID to search for
	 * @param guildId GuildID to search in
	 * @return Null if no results, populated string array of macro names if results
	 */
	public static String[] searchByUser(String userId, String guildId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> toReturn = new ArrayList<String>(10);
		try {
			conn = ConnectionPool.getConnection(guildId);
			String sql = "SELECT macro FROM `discord_macro` WHERE user_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			rs = stmt.executeQuery();
			if(!rs.isBeforeFirst())
				return null;
			while(rs.next()) {
				toReturn.add(rs.getString(1));
			}
			return toReturn.toArray(new String[0]);
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}

	public String getFallbackUsername() {
		return fallbackUsername;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getMacroName() {
		return macroName;
	}

	public String getMacroContent() {
		return macroContent;
	}

	public int getHits() {
		return hits;
	}

	public String getUserId() {
		return userId;
	}

	public String getGuildId() {
		return guildId;
	}
}
