package io.ph.web;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.secure;
import static spark.Spark.redirect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import freemarker.template.Configuration;
import freemarker.template.Version;
import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.web.beans.BotStatsBean;
import io.ph.web.beans.SparkSessionBean;
import io.ph.web.routes.ApiRoutes;
import io.ph.web.routes.DashboardRoute;
import io.ph.web.routes.GuildListRoute;
import io.ph.web.routes.PageRoute;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import spark.Response;
import spark.Spark;


public class WebServer {
	private static final String OAUTH_URL = "https://discordapp.com/oauth2/authorize?client_id=%s&scope=identify%%20guilds&response_type=code";
	
	private final static BotStatsBean botStats = new BotStatsBean();
	public static Map<String, SparkSessionBean> userToAuthGuilds = ExpiringMap.builder()
			.expiration(45, TimeUnit.MINUTES)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();

	public static void launchServer(String... args) {
		try {
			Bot.getInstance().getApiKeys().get("dashboardid");
			Bot.getInstance().getApiKeys().get("dashboardsecret");
		} catch(NoAPIKeyException e) {
			LoggerFactory.getLogger(WebServer.class).warn("No dashboard keys set. Not running webserver!");
			return;
		}
		try {
			Bot.getInstance().getApiKeys().get("sslpw");
			secure("keystore.jks", Bot.getInstance().getApiKeys().get("sslpw"), null, null);
			port(Bot.getInstance().getConfig().getDefaultSSLPort());
		} catch(NoAPIKeyException e) {
			port(Bot.getInstance().getConfig().getDefaultInsecurePort());
			LoggerFactory.getLogger(WebServer.class).warn("No Java Keystore password set for SSL. Running on port 8080");
		}
		Spark.staticFileLocation("/public");
		Spark.exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
		});
		DashboardRoute.initialize();
		GuildListRoute.initialize();
		PageRoute.initialize();
		ApiRoutes.initialize();
		get("/callback", (req, res) -> {
			String authGrant = req.queryParams("code");
			oauth(authGrant, res);
			res.redirect(GuildListRoute.ROUTE);
			return null;
		});
		// These 3 lines are examples of redirections. Feel free to change or whatever you want to do
		redirect.get("/changes", "/changelog.html");
		redirect.get("/join", "https://discord.gg/uM3pyW8");
		redirect.get("/github", "https://github.com/paul-io/momo-2");
		
	}

	/**
	 * Perform an Oauth2 callback to the Discord servers with the token given by the user's approval
	 * @param token Token from user
	 * @param res Passed on response
	 * @throws ClientProtocolException Error in HTTP protocol
	 * @throws IOException Encoding exception or error in protocol
	 * @throws NoAPIKeyException No API keys set
	 */
	static void oauth(String token, Response res) throws ClientProtocolException, IOException, NoAPIKeyException {
		
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpPost post = new HttpPost("https://discordapp.com/api/oauth2/token");
		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("client_id", Bot.getInstance().getApiKeys().get("dashboardid")));
		nvp.add(new BasicNameValuePair("client_secret", Bot.getInstance().getApiKeys().get("dashboardsecret")));
		nvp.add(new BasicNameValuePair("grant_type", "authorization_code"));
		nvp.add(new BasicNameValuePair("code", token));

		post.setEntity(new UrlEncodedFormEntity(nvp));

		String accessToken;
		CloseableHttpResponse response = httpclient.execute(post);
		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			JsonObject authJson;
			try(BufferedReader buffer = new BufferedReader(new InputStreamReader(entity.getContent()))) {
				authJson = Json.parse(buffer.lines().collect(Collectors.joining("\n"))).asObject();
			}
			accessToken = authJson.getString("access_token", "");
			EntityUtils.consume(entity);
			getGuilds(res, accessToken);
		} finally {
			response.close();
		}
	}

	/**
	 * Get the user's guilds using the given access token
	 * @param res Passed on response
	 * @param accessToken Access token
	 * @return 
	 * @throws ClientProtocolException HTTP protocol error
	 * @throws IOException Exception in consumption or protocol error
	 */
	static String getGuilds(Response res, String accessToken) throws ClientProtocolException, IOException {
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet get = new HttpGet("https://discordapp.com/api/users/@me");
			get.addHeader("Authorization", "Bearer " + accessToken);
			response = httpclient.execute(get);
			HttpEntity entity = response.getEntity();
			String s;
			try(BufferedReader buffer = new BufferedReader(new InputStreamReader(entity.getContent()))) {
				s = buffer.lines().collect(Collectors.joining());
			}
			String userId = Json.parse(s).asObject().getString("id", null);
			String username = Json.parse(s).asObject().getString("username", null);
			String avatar;
			if(!Json.parse(s).asObject().get("avatar").isNull())
				avatar = Json.parse(s).asObject().get("avatar").asString();
			else
				avatar = "";
			if(userId == null || username == null) {
				halt(401, "Bad token. Try again");
			}
			response.close();

			get = new HttpGet("https://discordapp.com/api/users/@me/guilds");
			get.addHeader("Authorization", "Bearer " + accessToken);
			response = httpclient.execute(get);
			entity = response.getEntity();
			try(BufferedReader buffer = new BufferedReader(new InputStreamReader(entity.getContent()))) {
				s = buffer.lines().collect(Collectors.joining());
			}
			JsonArray ja = Json.parse(s).asArray();
			List<String> userGuilds = new ArrayList<String>();
			for(JsonValue jv : ja) {
				JsonObject jo = jv.asObject();
				if(Bot.getInstance().shards.getGuildById(jo.getString("id", "")) != null) {
					userGuilds.add(jo.get("id").asString());
				}
			}
			String sessionId = UUID.randomUUID().toString();
			userToAuthGuilds.put(sessionId, new SparkSessionBean(userId, username, avatar, userGuilds));
			res.cookie("usession", sessionId);
			return null;
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			response.close();
		}
	}
	static Configuration config = null;
	public static Configuration getConfiguration() {
		if(config == null) {
			config = new Configuration(new Version(2, 3, 0));
			config.setTemplateUpdateDelayMilliseconds(7000);
			config.setDefaultEncoding("UTF-8");
			try {
				config.setDirectoryForTemplateLoading(new File("src/main/resources/spark/template/freemarker"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}
	
	public static BotStatsBean getBotStats() {
		return botStats;
	}
	
	public static String getOauthUrl() {
		try {
			return String.format(OAUTH_URL, Bot.getInstance().getApiKeys().get("dashboardid"));
		} catch (NoAPIKeyException e) {
			e.printStackTrace();
		}
		return "";
	}
}