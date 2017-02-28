package io.ph.bot.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.exception.NoSearchResultException;
import io.ph.util.Util;
import net.jodah.expiringmap.ExpiringMap;

public class Theme {
	private int malId;
	private String type;
	private String link;
	private String songTitle;
	private String animeTitle;
	
	private static String baseThemeUrl = "https://themes.moe/includes/api/search.php?";

	private Theme(int malId, String type, String link, String songTitle, String animeTitle) {
		this.malId = malId;
		this.type = type;
		this.link = link;
		this.songTitle = songTitle;
		this.animeTitle = animeTitle;
	}
	public static Map<String, ArrayList<Theme>> getThemeResults(String search) throws NoSearchResultException, IOException, NoAPIKeyException {
		Map<String, ArrayList<Theme>> toReturn = ExpiringMap.builder()
				  .expiration(15, TimeUnit.MINUTES)
				  .build();

		String searchUrl = baseThemeUrl + "key=" + Bot.getInstance().getApiKeys().get("themes") + "&search=" + search;
		JsonValue jv = Util.jsonFromUrl(searchUrl);
		JsonArray ja = jv.asArray();
		if(ja.size() == 0) {
			throw new NoSearchResultException();
		}
		
		for(JsonValue jv2 : ja) {
			JsonObject jo = jv2.asObject();
			ArrayList<Theme> temp = new ArrayList<Theme>();
			for(JsonValue jv3 : jo.get("data").asArray()) {
				JsonObject jo2 = jv3.asObject();
				temp.add(new Theme(jo.getInt("malId", 1),
						jo2.getString("type", "OP"),
						jo2.getString("link", "https://my.mixtape.moe/cggknn.webm"),
						jo2.getString("songTitle", "Tank!"),
						jo.getString("animeName", "Cowboy Bebop")));
			}
			toReturn.put(jo.getString("animeName", "Cowboy Bebop"), temp);
		}
		return toReturn;
	}
	public int getMalId() {
		return malId;
	}
	public String getType() {
		return type;
	}
	public String getLink() {
		return link;
	}
	public String getSongTitle() {
		return songTitle;
	}
	public String getAnimeTitle() {
		return animeTitle;
	}

}
