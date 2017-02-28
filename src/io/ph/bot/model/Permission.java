package io.ph.bot.model;

/**
 * Enum wrapper class to provide readable strings and support for 
 * none and bot owner permissions
 * @author Paul
 *
 */
public enum Permission {
	NONE("No permissions", net.dv8tion.jda.core.Permission.MESSAGE_READ),
	KICK("Kick", net.dv8tion.jda.core.Permission.KICK_MEMBERS),
	BAN("Ban", net.dv8tion.jda.core.Permission.BAN_MEMBERS),
	MANAGE_ROLES("Manage roles", net.dv8tion.jda.core.Permission.MANAGE_ROLES),
	MANAGE_CHANNELS("Manage channels", net.dv8tion.jda.core.Permission.MANAGE_CHANNEL),
	MANAGE_SERVER("Manage server", net.dv8tion.jda.core.Permission.MANAGE_SERVER),
	ADMINISTRATOR("Administrator", net.dv8tion.jda.core.Permission.ADMINISTRATOR),
	BOT_OWNER("Bot owner", null);
	
	private final String readable;
	private final net.dv8tion.jda.core.Permission jdaPerm;
	
	private Permission(String readable, net.dv8tion.jda.core.Permission jdaPerm) {
		this.readable = readable;
		this.jdaPerm = jdaPerm;
	}
	
	public net.dv8tion.jda.core.Permission getJdaPerm() {
		return this.jdaPerm;
	}
	
	@Override
	public String toString() {
		return this.readable;
	}

}
