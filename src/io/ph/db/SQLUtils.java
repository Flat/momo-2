package io.ph.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SQL Utilities to close various SQL objects quietly
 * @author Paul
 *
 */
public class SQLUtils {
	public static void closeQuietly(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		}
		catch (Exception e) {
		}
	}

	public static void closeQuietly(Statement statement) {
		try {
			if (statement!= null) {
				statement.close();
			}
		}
		catch (Exception e) {
		}
	}

	public static void closeQuietly(PreparedStatement statement) {
		try {
			if (statement!= null) {
				statement.close();
			}
		}
		catch (Exception e) {
		}
	}

	public static void closeQuietly(ResultSet resultSet) {
		try {
			if (resultSet!= null) {
				resultSet.close();
			}
		}
		catch (Exception e) {
		}
	}
}
