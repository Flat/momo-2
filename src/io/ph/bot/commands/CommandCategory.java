package io.ph.bot.commands;

/**
 * Enum for command categories to cleanup help commands
 * @author Paul
 *
 */
public enum CommandCategory {
	MUSIC("Music", "Commands related to music"),
	FUN("Fun", "Miscellaneous commands"),
	GAMES("Games", "Commands related to video games"),
	FEED("Feed", "Commands related to feeds (such as reddit)"),
	BOT_OWNER("Bot owner", "Commands for the owner of the bot"),
	ADMINISTRATION("Administration", "Commands related to bot administration"),
	MODERATION("Moderation", "Moderation commands"),
	ANIME("Anime", "Commands related to anime"),
	UTILITY("Utility", "Utility, other commands");
	
	private String readable;
	private String desc;
	
	private CommandCategory(String readable, String desc) {
		this.readable = readable;
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return this.readable;
	}
	
	public String getDescription() {
		return this.desc;
	}
}
