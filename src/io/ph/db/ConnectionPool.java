package io.ph.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class ConnectionPool {
	
	/**
	 * Get connection to the global database, GlobalDB.db
	 * @return Connection to this database
	 */
	public static Connection getGlobalDatabaseConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:resources/database/GlobalDB.db");
		} catch(SQLTimeoutException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get connection to the Twitch.tv Database, TwitchTV.db
	 * @return Connection to this database
	 * @throws SQLException
	 */
	public static Connection getTwitchDatabase() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:resources/database/TwitchTV.db");
		} catch(SQLTimeoutException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get connection to a specific guild's database, guildID/Data.db
	 * @param guildId Guild ID to connect to
	 * @return Connection to this guild's database
	 */
	public static Connection getConnection(String guildId) throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:resources/guilds/" + guildId + "/Data.db");
		} catch(SQLTimeoutException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
