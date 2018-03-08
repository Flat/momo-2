package io.ph.web.routes;

import static io.ph.web.WebServer.getConfiguration;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.ph.bot.Bot;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.GuildObject.SpecialChannels;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import io.ph.web.WebServer;
import io.ph.web.beans.SparkRedditBean;
import io.ph.web.beans.SparkTwitterBean;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * 
 * @author p
 *
 */
public class DashboardRoute {
	public static void initialize() {

		FreeMarkerEngine fme = new FreeMarkerEngine();
		fme.setConfiguration(getConfiguration());
		get("/dash", (req, res) -> {
			if(WebServer.userToAuthGuilds.get(req.cookie("usession")) == null)
				res.redirect(WebServer.getOauthUrl());
			else
				res.redirect("/guilds");
			return null;
		});
		before("/dash/*", (req, res) -> {
			if(WebServer.userToAuthGuilds.get(req.cookie("usession")) == null)
				halt(401, "You are not authorized to view this page. Please login through the homepage");
		});
		before("/dash/*/", (req, res) -> {
			// User verifications are done in each path
			if(req.queryParams("guildId") == null || Bot.getInstance().shards
					.getGuildById(req.queryParams("guildId")) == null)
				halt(500, "Invalid request parameters");
		});
		before("/dash/guild/*", (req, res) -> {
			String id = req.splat()[0];
			if(id == null || Bot.getInstance().shards.getGuildById(id) == null)
				halt(500, "Invalid request parameters");
		});
		
		
		get("/dash/guild/*", (req, res) -> {
			String id = req.splat()[0];
			Map<String, Object> attributes = new HashMap<String, Object>();
			List<TextChannel> channelList = new ArrayList<>(Bot
					.getInstance().shards.getGuildById(id).getTextChannels());
			Collections.sort(channelList, (c1, c2) -> c1.getPosition() - c2.getPosition());
			List<VoiceChannel> voiceChannelList = new ArrayList<>(Bot
					.getInstance().shards.getGuildById(id).getVoiceChannels());
			Collections.sort(voiceChannelList, (c1, c2) -> c1.getPosition() - c2.getPosition());
			List<Role> roleList = new ArrayList<Role>(Bot
					.getInstance().shards.getGuildById(id).getRoles().stream()
					.filter(r -> r.getPosition() != -1)
					.collect(Collectors.toList()));
			Collections.sort(roleList, (r1, r2) -> r1.getPosition() - r2.getPosition());

			// Permission attributes
			if(Util.memberHasPermission(Bot.getInstance().shards.getGuildById(id)
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_SERVER)) {
				attributes.put("permissions", 3);
			} else if(Util.memberHasPermission(Bot.getInstance().shards.getGuildById(id)
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_ROLES)) {
				attributes.put("permissions", 2);
			} else if(Util.memberHasPermission(Bot.getInstance().shards.getGuildById(id)
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.KICK)) {
				attributes.put("permissions", 1);
			} else {
				// Why are they here?
				halt(401, "You are not authorized to view this page.");
			}

			GuildObject g = GuildObject.guildMap.get(id);
			// Channel attributes
			attributes.put("channels", channelList);
			attributes.put("pmWelcome", g.getConfig().isPmWelcomeMessage());
			attributes.put("welcomeMessage", g.getConfig().getWelcomeMessage());
			attributes.put("serverName", Bot.getInstance().shards.getGuildById(id).getName());
			if(Bot.getInstance().shards.getGuildById(id).getIconId() != null)
				attributes.put("serverIcon", Bot.getInstance().shards.getGuildById(id).getIconUrl());
			attributes.put("voiceChannels", voiceChannelList);
			attributes.put("guildid", id);
			SpecialChannels spc = g.getSpecialChannels();
			attributes.put("music", spc.getMusic());
			attributes.put("welcome", spc.getWelcome());
			attributes.put("log", spc.getLog());
			attributes.put("musicVoice", spc.getMusicVoice());
			
			// DJ Role
			attributes.put("djRole", g.getConfig().getDjRoleId());
			
			// Auto assign role
			attributes.put("autoAssignRole", g.getConfig().getAutoAssignRoleId());
			
			// All roles
			attributes.put("roles", roleList);

			// Feed attributes
			attributes.put("redditFeed", SparkRedditBean.getSubredditsForGuildId(id));
			attributes.put("twitterFeed", SparkTwitterBean.getTwitterForGuild(id));

			//Server configuration attributes
			attributes.put("joinableRoleLimitation", g.getConfig().isLimitToOneRole());
			attributes.put("deleteInvites", g.getConfig().isDisableInvites());
			attributes.put("slowMode", g.getConfig().getMessagesPerFifteen());
			attributes.put("commandPrefix", g.getConfig().getCommandPrefix());
			attributes.put("advancedLogging", g.getConfig().isAdvancedLogging());

			//Command configuration attributes
			attributes.put("enabledCommands", g.getCommandStatus().keySet().stream()
					.filter(c -> g.getCommandStatus(c)
							&& CommandHandler.getCommand(c) != null 
							&& CommandHandler.getCommand(c).getPermission().equals(Permission.NONE))
					.sorted()
					.collect(Collectors.toList()));
			attributes.put("disabledCommands", g.getCommandStatus().keySet().stream()
					.filter(c -> !g.getCommandStatus(c)
							&& CommandHandler.getCommand(c) != null 
							&& CommandHandler.getCommand(c).getPermission().equals(Permission.NONE))
					.sorted()
					.collect(Collectors.toList()));
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			return new ModelAndView(attributes, "dashboard.ftl");
		}, fme);
		post("/dash/channel", (req, res) -> {
			if(req.queryParams("guildId") == null || Bot.getInstance().shards.getGuildById(req.queryParams("guildId")) == null)
				halt(500, "Request missing parameters");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_SERVER)) {
				halt(401, "Unauthorized");
			}
			final GuildObject gld = GuildObject.guildMap.get(req.queryParams("guildId"));
			req.queryParams().stream().forEach(s -> {
				if(s.endsWith("-channel-val")
						&& (Bot.getInstance().shards.getTextChannelById(req.queryParams(s)) != null
						|| req.queryParams(s).equals("none") || req.queryParams(s).equals("pmWelcome"))) {
					switch(s.split("-")[0]) {
					case "welcome":
						if(req.queryParams(s).equals("pmWelcome")) {
							gld.getConfig().setPmWelcomeMessage(true);
						} else {
							gld.getSpecialChannels().setWelcome(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
							if(gld.getConfig().isPmWelcomeMessage())
								gld.getConfig().setPmWelcomeMessage(false);
						}
						break;
					case "log":
						gld.getSpecialChannels().setLog(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
						break;
					case "music":
						gld.getSpecialChannels().setMusic(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
						break;
					}
				}
				if(s.equals("music-voicechannel-val") 
						&& (Bot.getInstance().shards.getVoiceChannelById(req.queryParams(s)) != null
						|| req.queryParams(s).equals("none"))) {
					gld.getSpecialChannels().setMusicVoice(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
				}
				if(s.equals("welcome-message-val")) {
					gld.getConfig().setWelcomeMessage(req.queryParams(s));
				}
			});
			return "Successfully saved settings";
		});
		post("/dash/server", (req, res) -> {
			if(req.queryParams("guildId") == null 
					|| Bot.getInstance().shards.getGuildById(req.queryParams("guildId")) == null)
				halt(500, "Request missing parameters");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_SERVER)) {
				halt(401, "Not authorized");
			}
			final GuildObject gld = GuildObject.guildMap.get(req.queryParams("guildId"));
			req.queryParams().stream().forEach(s -> {
				switch(s) {
				case "limitRoles":
					gld.getConfig().setLimitToOneRole(req.queryParams(s).equals("true") ? true : false);
					break;
				case "deleteInvites":
					gld.getConfig().setDisableInvites(req.queryParams(s).equals("true") ? true : false);
					break;
				case "slowMode":
					if(Util.isInteger(req.queryParams(s))) {
						gld.getConfig().setMessagesPerFifteen(Integer.parseInt(req.queryParams(s)));
					}
					break;
				case "commandPrefix":
					if(!req.queryParams(s).contains(" ") && req.queryParams(s).length() <= 6) {
						gld.getConfig().setCommandPrefix(req.queryParams(s));
					}
					break;
				case "djRole":
					gld.getConfig().setDjRoleId(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
					break;
				case "autoAssignRole":
					gld.getConfig().setAutoAssignRoleId(req.queryParams(s).equals("none") ? "" : req.queryParams(s));
					break;
				}
			});
			return "Successfully saved settings";
		});
	}
}
