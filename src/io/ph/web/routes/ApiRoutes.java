package io.ph.web.routes;

import static io.ph.web.WebServer.getBotStats;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.RedditFeedObserver;
import io.ph.bot.feed.TwitchEventListener;
import io.ph.bot.feed.TwitchFeedObserver;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.feed.TwitterFeedObserver;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import io.ph.web.WebServer;
import io.ph.web.beans.SparkRedditBean;
import io.ph.web.beans.SparkTwitchBean;
import io.ph.web.beans.SparkTwitterBean;
import net.dean.jraw.http.NetworkException;
import net.dv8tion.jda.core.entities.Channel;
import twitter4j.TwitterException;
import twitter4j.User;

public class ApiRoutes {
	public static void initialize() {
		before("/api/*", (req, res) -> {
			if(WebServer.userToAuthGuilds.get(req.cookie("usession")) == null)
				halt(401, "Unauthorized access");
			if(!req.splat()[0].startsWith("commands/")) {
				if(req.queryParams("channelId") == null
						|| Bot.getInstance().shards.getTextChannelById(req.queryParams("channelId")) == null)
					halt(500, "Channel not found");
			}
			// Every request must include the guildId
			if(req.queryParams("guildId") == null || Bot.getInstance().shards.getGuildById(req.queryParams("guildId")) == null) {
				halt(500, "Guild not found");
			}
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.KICK)) {
				halt(401, "You are not authorized to view this page.");
			}
			// Kick permissions minimum here
		});
		get("/api/twitter", (req, res) -> {
			String channelId = req.queryParams("channelId");

			List<SparkTwitterBean> feed = SparkTwitterBean.getTwitterForChannel(channelId);
			JsonArray ja = new JsonArray();
			feed.stream().forEach(t -> ja.add(t.getTwitterHandle()));
			return ja.toString();
		});
		post("/api/twitter/remove", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String twitter = req.queryParams("twitter");
			if(twitter == null)
				halt(500, "Request missing parameters");
			Channel ch = Bot.getInstance().shards.getTextChannelById(channelId);
			User u = TwitterEventListener.twitterClient.lookupUsers(new String[]{twitter}).get(0);
			if(TwitterEventListener.removeTwitterFeed(u.getId(), ch.getGuild())) {
				res.status(200);
			} else {
				halt(500, "Feed not found. Make sure you selected one to remove...");
			}
			return "Unsubscribed from @" + twitter;
		});
		post("/api/twitter/add", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String twitter = req.queryParams("twitter");
			String showImages = req.queryParams("showImages");
			String showRetweets = req.queryParams("showRetweets");
			String showReplies = req.queryParams("showReplies");
			if(showImages == null || showRetweets == null || showReplies == null || twitter == null)
				halt(500, "Request missing parameters");

			try {
				User u = TwitterEventListener.twitterClient.lookupUsers(new String[]{twitter}).get(0);
				boolean showImg = false;
				boolean showRet = false;
				boolean showRep = false;
				if (showImages.equals("yes")) {
					showImg = true;
				}
				if (showRetweets.equals("yes")) {
					showRet = true;
				}
				if (showReplies.equals("yes")) {
					showRep = true;
				}

				(new TwitterFeedObserver(channelId, twitter, showImg, showRet, showRep)).subscribe(u.getId());
			} catch(TwitterException e) {
				if(e.getErrorCode() == 17) {
					halt(500, String.format("Twitter account **%s** does not exist", twitter));
				} else {
					halt(500, "Unspecified error");
				}
			}
			return "Successfully subscribed to @" + twitter;
		});
		get("/api/reddit", (req, res) -> {
			String channelId = req.queryParams("channelId");

			List<SparkRedditBean> feed = SparkRedditBean.getSubredditsForChannelId(channelId);
			JsonArray ja = new JsonArray();
			feed.stream().forEach(r -> ja.add(r.getSubreddit()));
			return ja.toString();
		});
		post("/api/reddit/remove", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String subreddit = req.queryParams("subreddit");
			if(subreddit == null)
				halt(500, "Request missing parameters");
			Channel ch = Bot.getInstance().shards.getTextChannelById(channelId);
			if(RedditEventListener.removeRedditFeed(subreddit, ch.getGuild())) {
				res.status(200);
			} else {
				halt(500, "Feed not found. Make sure you selected one to remove...");
			}
			return "Unsubscribed from /r/" + subreddit;
		});
		post("/api/reddit/add", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String subreddit = req.queryParams("subreddit");
			String showImages = req.queryParams("showImages");
			String showText = req.queryParams("showText");

			if(subreddit == null || showImages == null || showText == null)
				halt(500, "Request missing parameters");

			RedditFeedObserver ob;
			if((ob = RedditEventListener.getObserver(subreddit,
					Bot.getInstance().shards.getTextChannelById(channelId).getGuild())) != null) {
				halt(500, "Feed already exists for this server in #" + ob.getDiscoChannel().getName());
			} else {
				try {
					RedditEventListener.redditClient.getSubreddit(subreddit);
				} catch(NetworkException | IllegalArgumentException e) {
					halt(500, "Subreddit /r/" + subreddit + " does not exist");
				}
				boolean showImage = false;
				boolean showNsfw = false;
				boolean showTextPreview = false;
				switch(showImages) {
				case "all":
					showImage = true;
					showNsfw = true;
					break;
				case "no nsfw":
					showImage = true;
					break;
				case "none":
					break;
				default:
					halt(500, "Invalid parameter for \"show images\"");
					break;
				}
				switch(showText) {
				case "yes":
					showTextPreview = true;
					break;
				case "no":
					break;
				default:
					halt(500, "Invalid parameter for \"show text\"");
					break;
				}
				new RedditFeedObserver(channelId, subreddit, showImage, showNsfw, showTextPreview);
			}
			return "Successfully subscribed to /r/" + subreddit;
		});
		get("/api/twitch", (req, res) -> {
			String channelId = req.queryParams("channelId");

			List<SparkTwitchBean> feed = SparkTwitchBean.getTwitchForChannel(channelId);
			JsonArray ja = new JsonArray();
			feed.stream().forEach(t -> ja.add(t.getTwitchHandle()));
			return ja.toString();
		});
		post("/api/twitch/remove", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String twitch = req.queryParams("twitch");
			if(twitch == null)
				halt(500, "Request missing parameters");
			Channel ch = Bot.getInstance().shards.getTextChannelById(channelId);
			if(TwitchEventListener.removeTwitchFeed(TwitchEventListener.resolveUserIdFromUsername(twitch), ch.getGuild())) {
				res.status(200);
			} else {
				halt(500, "Twitch user not found. Make sure you selected one to remove...");
			}
			return "Unfollowing Twitch.tv user " + twitch;
		});
		post("/api/twitch/add", (req, res) -> {
			String channelId = req.queryParams("channelId");
			String twitch = req.queryParams("twitch");
			if(twitch == null)
				halt(500, "Request missing parameters");

			try {
				String userId = TwitchEventListener.resolveUserIdFromUsername(twitch);
				// Really need to verify that the twitch user exists
				new TwitchFeedObserver(channelId, twitch.toLowerCase(), userId);
			} catch(IllegalArgumentException e) {
				halt(500, String.format("Twitch account **%s** does not exist", twitch));
			}
			return "Successfully following Twitch.tv user " + twitch;
		});
		post("/api/commands/enable", (req, res) -> {
			String guildId = req.queryParams("guildId");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_ROLES)) {
				halt(401, "Not authorized");
			}
			String command = req.queryParams("command").toLowerCase();
			GuildObject g = GuildObject.guildMap.get(guildId);
			JsonArray ja = Json.parse(command).asArray();

			StringBuilder sb = new StringBuilder();
			try {
				for(JsonValue jv : ja) {
					String c = jv.asString();
					g.enableCommand(c);
					sb.append(c + ", ");
				}
			} catch(IllegalArgumentException | NullPointerException e) {
				halt(500, "Invalid command(s)");
			}
			return "Enabled " + sb.substring(0, sb.length() - 2);
		});
		post("/api/commands/disable", (req, res) -> {
			String guildId = req.queryParams("guildId");
			if(!Util.memberHasPermission(Bot.getInstance().shards.getGuildById(req.queryParams("guildId"))
					.getMember(Bot.getInstance().shards.getUserById(WebServer.userToAuthGuilds
							.get(req.cookie("usession")).getUserId())), Permission.MANAGE_ROLES)) {
				halt(401, "Not authorized");
			}
			String command = req.queryParams("command").toLowerCase();
			GuildObject g = GuildObject.guildMap.get(guildId);
			JsonArray ja = Json.parse(command).asArray();

			StringBuilder sb = new StringBuilder();
			try {
				for(JsonValue jv : ja) {
					String c = jv.asString();
					g.disableCommand(c);
					sb.append(c + ", ");
				}
			} catch(IllegalArgumentException e) {
				halt(500, "Invalid command(s)");
			}
			return "Disabled " + sb.substring(0, sb.length() - 2);
		});

		get("/public/counts", (req, res) -> {
			if (req.queryParams("type") != null && req.queryParams("type").equals("msg")) {
				return (new JsonArray()).add(getBotStats().getMessageCount())
						.add(getBotStats().getCommandCount())
						.toString();
			} else if (req.queryParams("type") != null && req.queryParams("type").equals("status")) {
				return (new JsonArray()).add(getBotStats().getUsers())
						.add(getBotStats().getMemoryUsage())
						.add(getBotStats().getGuilds())
						.add(getBotStats().getUptimeHours())
						.add(getBotStats().getUptimeMinutes())
						.toString();
			} else {
				halt(400);
			}
			return null;
		});
	}
}
