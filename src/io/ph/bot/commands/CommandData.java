package io.ph.bot.commands;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.ph.bot.model.Permission;
/**
 * Syntax for commands
 * @author Paul
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandData {
	/**
	 * Default command usage
	 * @return
	 */
	String defaultSyntax();
	/**
	 * String array of aliases
	 * @return Aliases
	 */
	String[] aliases();
	/**
	 * Command category as outlined in CommandCategory
	 * @see io.ph.bot.commands.CommandCategory
	 * @return Category
	 */
	CommandCategory category();
	/**
	 * Description of the command
	 * @return Description
	 */
	String description();
	/**
	 * Permission needed
	 * @return Permission
	 */
	Permission permission();
	/**
	 * Example WITHOUT default syntax OR server prefix
	 * Those get added in the help command
	 * @return Example of parameters + contents
	 */
	String example();
}
