package io.ph.web.beans;

public class BotStatsBean {
	private int users;
	private int messageCount;
	private int commandCount;
	private int memoryUsage;
	private int guilds;
	private int uptimeMinutes;
	private int uptimeHours;
	
	public BotStatsBean(int users, int messageCount, int commandCount, int memoryUsage, int guilds, int uptimeMinutes,
			int uptimeHours) {
		this.users = users;
		this.messageCount = messageCount;
		this.commandCount = commandCount;
		this.memoryUsage = memoryUsage;
		this.guilds = guilds;
		this.uptimeMinutes = uptimeMinutes;
		this.uptimeHours = uptimeHours;
	}
	
	public BotStatsBean() {
		this.users = 0;
		this.messageCount = 0;
		this.commandCount = 0;
		this.memoryUsage = 0;
		this.guilds = 0;
		this.uptimeHours = 0;
		this.uptimeMinutes = 0;
	}

	public int getUsers() {
		return users;
	}
	public BotStatsBean setUsers(int users) {
		this.users = users;
		return this;
	}
	public int getMessageCount() {
		return messageCount;
	}
	public BotStatsBean setMessageCount(int messageCount) {
		this.messageCount = messageCount;
		return this;
	}
	public int getCommandCount() {
		return commandCount;
	}
	public BotStatsBean setCommandCount(int commandCount) {
		this.commandCount = commandCount;
		return this;
	}
	public int getMemoryUsage() {
		return memoryUsage;
	}
	public BotStatsBean setMemoryUsage(int memoryUsage) {
		this.memoryUsage = memoryUsage;
		return this;
	}
	public int getGuilds() {
		return guilds;
	}
	public BotStatsBean setGuilds(int guilds) {
		this.guilds = guilds;
		return this;
	}
	public int getUptimeMinutes() {
		return uptimeMinutes;
	}
	public BotStatsBean setUptimeMinutes(int uptimeMinutes) {
		this.uptimeMinutes = uptimeMinutes;
		return this;
	}
	public int getUptimeHours() {
		return uptimeHours;
	}
	public BotStatsBean setUptimeHours(int uptimeHours) {
		this.uptimeHours = uptimeHours;
		return this;
	}
	
}
