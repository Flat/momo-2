package io.ph.web.beans;

import java.util.ArrayList;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.Permission;

public class SparkCommandBean {
	private String command;
	private String[] aliases;
	private String description;
	private String example;
	private Permission permission;

	private SparkCommandBean(Command cmd) {
		this.command = cmd.getDefaultCommand();
		this.aliases = cmd.getAliases();
		this.description = cmd.getDescription();
		this.example = cmd.getExample();
		this.permission = cmd.getPermission();
	}
	public static List<SparkCommandBean> getCommands() {
		List<SparkCommandBean> toReturn = new ArrayList<SparkCommandBean>();
		CommandHandler.getAllCommands().stream().forEach(cmd -> toReturn.add(new SparkCommandBean(cmd)));
		toReturn.sort((f, s) -> {
			if(f.getPermission().compareTo(s.getPermission()) != 0)
				return f.getPermission().compareTo(s.getPermission());
			return f.getCommand().compareTo(s.getCommand());
		});
		return toReturn;
	}

	public String getCommand() {
		return command;
	}
	public String[] getAliases() {
		return aliases;
	}
	public String getDescription() {
		return description;
	}
	public String getExample() {
		return example;
	}
	public Permission getPermission() {
		return permission;
	}

}
