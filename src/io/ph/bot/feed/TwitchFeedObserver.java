package io.ph.bot.feed;

import java.awt.Color;
import java.io.Serializable;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Observer class for twitch feed
 * @author Paul
 */
public class TwitchFeedObserver implements Serializable {
	private static final long serialVersionUID = -8485810270273369608L;
	private String discoChannel;
	private String username; // This is used for $twitchlist

	/**
	 * Initialize and register a TwitterFeedObserver
	 * @param discoChannel The channel ID to register to
	 * @param twitchUser Twitch username
	 */
	public TwitchFeedObserver(String discoChannel, String username, String userId) {
		this.discoChannel = discoChannel;
		this.username = username;
		this.register(userId);
	}

	/**
	 * Register a twitch userID
	 * @param userId Twitch User ID to register
	 */
	public void register(String userId) {
		TwitchEventListener.addTwitchFeed(userId, this);
		TwitchEventListener.saveFeed();
	}

	/**
	 * Trigger this twitch observer
	 * @param userId User ID to trigger
	 * @param json The details given by Twitch API
	 * @return True if could process, false if not
	 */
	public boolean trigger(String userId, JSONObject json) {
		if(getDiscoChannel() == null || !getDiscoChannel().canTalk()) {
			return false;
		}
		process(userId, json);
		return true;
	}

	/**
	 * Process the twitch status change
	 * @param userId Twitch User ID that is changing status
	 * @param json The details given by Twitch API
	 */
	private void process(String userId, JSONObject json) {
		EmbedBuilder em = new EmbedBuilder();
		if (json.isNull("stream")) {
			// User went offline
			try {
				HttpResponse<JsonNode> userJson = Unirest.get(TwitchEventListener.ENDPOINT + "users/" + userId)
						.header("Accept", "application/vnd.twitchtv.v5+json")
						.header("Client-ID", Bot.getInstance().getApiKeys().get("twitch"))
						.asJson();
				String username = userJson.getBody().getObject().getString("display_name");
				em.setTitle(username + " has gone offline on Twitch.tv", "https://twitch.tv/" + username)
				.setColor(Color.MAGENTA);
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (NoAPIKeyException e) {
				e.printStackTrace();
			}
		} else {
			// User came back online
			String username = json.getJSONObject("stream")
					.getJSONObject("channel").getString("display_name");
			String gameName = json.getJSONObject("stream").getString("game");
			String status = json.getJSONObject("stream")
					.getJSONObject("channel").getString("status");
			String imageUrl = json.getJSONObject("stream").getJSONObject("preview").getString("large");

			em.setTitle(String.format("%s is now playing %s on Twitch.tv", username, gameName), imageUrl)
			.setColor(Color.MAGENTA)
			.setDescription(String.format("%s\nhttps://twitch.tv/%s", status, username))
			.setImage(imageUrl);
		}
		this.getDiscoChannel().sendMessage(em.build()).queue();
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Get the TextChannel this feed is going to
	 * @return TextChannel
	 */
	public TextChannel getDiscoChannel() {
		return Bot.getInstance().shards.getTextChannelById(discoChannel);
	}

	/**
	 * Get the channel ID of the TextChannel this feed is going to
	 * @return String of channel ID
	 */
	public String getDiscoChannelId() {
		return this.discoChannel;
	}

}

