package io.ph.bot.commands;

import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;

public abstract class Command {
	private int commandCount = 0;
	/**
	 * Execute this command
	 * @param msg Original Message. Can infer guild, user, etc off of this
	 */
	public abstract void executeCommand(Message msg);
	
	/**
	 * Get default command syntax
	 * @return Default syntax
	 */
	public String getDefaultCommand() {
		return this.getClass().getAnnotation(CommandData.class).defaultSyntax();
	}
	
	/**
	 * Check if user has permissions
	 * @param msg Original message
	 * @return True if good to go, false if not
	 */
	public boolean hasPermissions(Message msg) {
		return Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), getPermission());
	}
	
	/**
	 * Get permission required
	 * @return Permission required
	 */
	public Permission getPermission() {
		return this.getClass().getAnnotation(CommandData.class).permission();
	}
	
	/**
	 * Get aliases of command
	 * @return Aliases of this command
	 */
	public String[] getAliases() {
		return this.getClass().getAnnotation(CommandData.class).aliases();
	}
	
	/**
	 * Get description
	 * @return Description of command
	 */
	public String getDescription() {
		return this.getClass().getAnnotation(CommandData.class).description();
	}
	
	/**
	 * Get example. Do not include the default command, as that is automatically appended
	 * If you have multiple examples, use a \n linebreak to designate. Again, do not include the command itself
	 * @return Example in annotation
	 */
	public String getExample() {
		return this.getClass().getAnnotation(CommandData.class).example();
	}
	
	/**
	 * Get the number of times this command has been called
	 * @return Number of times this command has been executed
	 */
	public int getCommandCount() {
		return this.commandCount;
	}
	
	/**
	 * Increment the command counter
	 */
	public void incrementCommandCount() {
		this.commandCount++;
	}
	@Override
	public String toString() {
		return getDefaultCommand().toLowerCase();
	}
}
