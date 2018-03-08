package io.ph.web.routes;

import static io.ph.web.WebServer.getConfiguration;
import static spark.Spark.get;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.ph.bot.Bot;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.Permission;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
public class PageRoute {
	public static void initialize() {
		FreeMarkerEngine fme = new FreeMarkerEngine();
		fme.setConfiguration(getConfiguration());
		get("/", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			return new ModelAndView(attributes, "index.ftl");
		}, fme);
		get("/changelog", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			return new ModelAndView(attributes, "changelog.ftl");
		}, fme);
		get("/commands", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			attributes.put("commandsEveryone", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.NONE))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			attributes.put("commandsKick", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.KICK))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			attributes.put("commandsBan", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.BAN))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			attributes.put("commandsManageRoles", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.MANAGE_ROLES))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			attributes.put("commandsManageServer", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.MANAGE_SERVER))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			attributes.put("commandsBotOwner", CommandHandler.getAllCommands().stream()
					.filter(cmd -> cmd.getPermission().equals(Permission.BOT_OWNER))
					.sorted((f, s) -> f.getDefaultCommand().compareTo(s.getDefaultCommand()))
					.collect(Collectors.toList()));
			return new ModelAndView(attributes, "commands.ftl");
		}, fme);
		get("/status", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			return new ModelAndView(attributes, "status.ftl");
		}, fme);
	}
}
