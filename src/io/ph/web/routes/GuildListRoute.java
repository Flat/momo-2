package io.ph.web.routes;

import static io.ph.web.WebServer.getConfiguration;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ph.bot.Bot;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import io.ph.web.WebServer;
import io.ph.web.beans.SparkGuildBean;
import net.dv8tion.jda.core.entities.User;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

public class GuildListRoute {
	public static final String ROUTE = "/guilds";
	public static void initialize() {

		FreeMarkerEngine fme = new FreeMarkerEngine();
		fme.setConfiguration(getConfiguration());
		before(ROUTE, (req, res) -> {
			if(WebServer.userToAuthGuilds.get(req.cookie("usession")) == null)
				halt(401, "You are not authorized to view this page. Please login through the homepage");
		});

		get(ROUTE, (req, res) -> {
			Map<String, Object> attributes = new HashMap<String, Object>();
			User user = Bot.getInstance().shards
					.getUserById(WebServer.userToAuthGuilds.get(req.cookie("usession")).getUserId());
			List<SparkGuildBean> guilds = new ArrayList<>();

			Bot.getInstance().getBots().stream()
			.forEach(j -> j.getGuilds().stream()
					.filter(g -> g.getMemberById(WebServer.userToAuthGuilds.get(req.cookie("usession")).getUserId()) != null
					&& Util.memberHasPermission(g.getMember(user), Permission.KICK))
					.forEach(g -> guilds.add(new SparkGuildBean(g.getId(), g.getName(), g.getIconUrl()))));
			guilds.stream()
			.sorted((g1, g2) -> g1.getName().compareTo(g2.getName()));
			
			attributes.put("guilds", guilds);
			attributes.put("username", WebServer.userToAuthGuilds.get(req.cookie("usession")).getUsername());
			if(user.getAvatarId() != null)
				attributes.put("userIcon", user.getAvatarUrl());
			attributes.put("botName", Bot.getInstance().getBots().get(0).getSelfUser().getName());
			return new ModelAndView(attributes, "guildlist.ftl");
		}, fme);
	}
}
