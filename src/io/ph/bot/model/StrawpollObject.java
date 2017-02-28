package io.ph.bot.model;

import java.io.IOException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.util.Util;

/**
 * Strawpoll object to create and view polls
 * @author Paul
 *
 */
public class StrawpollObject {

	private String title;
	@SerializedName("multi")
	private boolean multiVote;
	private String[] options;
	private transient int[] votes;

	// Make sure this is http, no SSL/TLS
	private static final String BASE_API = "http://www.strawpoll.me/api/v2/polls";

	public StrawpollObject(String title, boolean multiVote, String[] options, int[] votes) {
		this.title = title;
		this.multiVote = multiVote;
		this.options = options;
		this.votes = votes;
	}

	public StrawpollObject(String title, boolean multiVote, String... options) {
		this.title = title;
		this.multiVote = multiVote;
		this.options = options;
	}

	/**
	 * Create the poll as represented by this object
	 * @return ID of the created poll
	 * @throws IOException Something bad happened when accessing the resource
	 */
	public int createPoll() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.post(BASE_API)
				.header("Content-Type", "application/json")
				.body((new Gson()).toJson(this))
				.asJson();
		return jsonResponse.getBody().getObject().getInt("id");
	}
	
	/**
	 * Get strawpoll from its ID
	 * @param id ID to lookup
	 * @return Strawpoll Object
	 * @throws IOException Strawpoll.me is down or denied our API access
	 */
	public static StrawpollObject fromId(int id) throws IOException {
		JsonValue jv = Util.jsonFromUrl(BASE_API + "/" + id);
		JsonObject jo = jv.asObject();
		String title = jo.getString("title", "title");
		boolean multiVote = jo.getBoolean("multi", false);

		JsonArray jOptions = jo.get("options").asArray();
		String[] options = new String[jOptions.size()];
		JsonArray jVotes = jo.get("votes").asArray();
		int[] votes = new int[jVotes.size()];
		for(int i = 0; i < options.length; i++) {
			options[i] = jOptions.get(i).asString();
			votes[i] = jVotes.get(i).asInt();
		}
		return new StrawpollObject(title, multiVote, options, votes);
	}

	public String getTitle() {
		return title;
	}

	public boolean isMultiVote() {
		return multiVote;
	}

	public String[] getOptions() {
		return options;
	}

	public int[] getVotes() {
		return votes;
	}
}
