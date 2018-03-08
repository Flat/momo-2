package io.ph.web.beans;

import java.util.List;

public class SparkSessionBean {
	private String userId;
	private String username;
	private String avatar;
	private List<String> userGuilds;
	
	public SparkSessionBean(String userId, String username, String avatar, List<String> authGuilds) {
		this.userId = userId;
		this.userGuilds = authGuilds;
		this.username = username;
		this.avatar = avatar;
	}

	/**
	 * @return the authGuilds
	 */
	public List<String> getAuthGuilds() {
		return userGuilds;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	public String getAvatar() {
		return avatar;
	}
}
