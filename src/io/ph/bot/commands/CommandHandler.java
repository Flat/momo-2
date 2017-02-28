package io.ph.bot.commands;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;

/**
 * A centralized class that manages all commands available across servers
 * Every command class created needs to have a public, default constructor
 * as well as have the correct annotations to follow CommandSyntax
 * 
 * A command needs to override only run(Message msg)
 * and will follow the requirements outlined in the CommandSyntax annotation
 * 
 * TODO: Rewrite this to not rely on a global
 * @author Paul
 *
 */
public class CommandHandler {

	private static Map<String, Command> commandMap = new HashMap<String, Command>();

	public static Map<String, String> aliasToDefaultMap = new HashMap<String, String>();

	/**
	 * Reflect through the commands package and initialize commands
	 */
	public static void initCommands() {
		Reflections reflections = new Reflections("io.ph.bot.commands");    
		Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
		classes.addAll(reflections.getSubTypesOf(ProceduralCommand.class));
		for(Class<? extends Command> c : classes) {
			if(c.isInterface() || !c.isAnnotationPresent(CommandData.class))
				continue;
			Annotation[] a = c.getAnnotations();
			try {
				Command instance = (Command) Class.forName(c.getName()).newInstance();
				for(Annotation a2 : a) {
					if(a2 instanceof CommandData) {
						String defaultCmd = ((CommandData) a2).defaultSyntax();
						commandMap.put(defaultCmd, instance);
						aliasToDefaultMap.put(defaultCmd, defaultCmd);
						for(String s : ((CommandData) a2).aliases()) {
							aliasToDefaultMap.put(s, defaultCmd);
						}
					}
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			normalizeCommands(commandMap.values());
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(2);
		}
		LoggerFactory.getLogger(CommandHandler.class).info("Initialized commands");
	}

	/**
	 * Entry point for command handling
	 * If the user doesn't have at least kick privileges and the command is disabled,
	 * the command is rejected without a message.
	 * @param msg Message to parse
	 */
	public static void processCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if(msg.getContent().contains(" ")) {
			String cmd = aliasToDefaultMap.get(msg.getContent().substring(g.getConfig().getCommandPrefix().length(),
					msg.getContent().indexOf(" ")));
			if(cmd == null)
				return;
			if(getCommand(cmd).hasPermissions(msg)) {
				if(g.getCommandStatus(cmd)
						|| Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)) {
					Command c = getCommand(cmd);
					c.incrementCommandCount();
					Util.setTimeout(() -> c.executeCommand(msg), 0, true);
				}
			}
		} else {
			String cmd = aliasToDefaultMap.get(msg.getContent().substring(g.getConfig().getCommandPrefix().length(),
					msg.getContent().length()));
			if(cmd == null)
				return;
			if(getCommand(cmd).hasPermissions(msg)) {
				if(g.getCommandStatus(cmd)
						|| Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)) {
					Command c = getCommand(cmd);
					c.incrementCommandCount();
					Util.setTimeout(() -> c.executeCommand(msg), 0, true);
				}
			}
		}
	}

	/**
	 * Normalize all guild commands
	 * @param collection All commands
	 * @throws ConfigurationException If apache config throws an exception
	 */
	private static void normalizeCommands(Collection<Command> collection) throws ConfigurationException {
		Collection<File> found = FileUtils.listFiles(new File("resources/guilds"),
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		found.add(new File("resources/guilds/template.properties"));
		for (File f : found) {
			if (f.getName().equals("GuildProperties.properties") || f.getName().equals("template.properties")) {
				PropertiesConfiguration config = new PropertiesConfiguration(f);
				List<String> enabledCommands = config.getList("EnabledCommands").stream()
						.map(object -> Objects.toString(object, null))
						.collect(Collectors.toList());
				List<String> disabledCommands = config.getList("DisabledCommands").stream()
						.map(object -> Objects.toString(object, null))
						.collect(Collectors.toList());
				for (Command c : collection) {
					if (!enabledCommands.contains(c.toString()) && !disabledCommands.contains(c.toString())) {
						enabledCommands.add(c.toString());
					}
				}
				config.setProperty("EnabledCommands", enabledCommands);
				config.save();
			}
		}
	}
	
	/**
	 * Get a command by its true name or alias
	 * @param name Name of command
	 * @return Command you're looking for
	 */
	public static Command getCommand(String name) {
		return CommandHandler.commandMap.get(aliasToDefaultMap.get(name));
	}

	/**
	 * Get all loaded commands
	 * @return Collection of all commands
	 */
	public static Collection<Command> getAllCommands() {
		return new ArrayList<Command>(CommandHandler.commandMap.values());
	}

}
