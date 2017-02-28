package io.ph.bot.model.games;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.exception.BadCharacterException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.util.Util;

public class WoWCharacter {
	String baseUrl = "https://%s.api.battle.net/wow/character/";

	private int lfrRaid = 0;
	private int normalRaid = 0;
	private int heroicRaid = 0;
	private int mythicRaid = 0;

	private int lfrKills = 0;
	private int normalKills = 0;
	private int heroicKills = 0;
	private int mythicKills = 0;

	private String username;
	private String realm;
	private String gender;
	private String gameClass;
	private String race;
	private int level;
	private int achievementPoints;
	private int itemLevel;
	
	private String guild;
	private int guildMembers;

	private String thumbnail;

	private String error;

	// Necessary to map their ints to classes
	private static HashMap<String, Object> cache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public WoWCharacter(String server, String name, String region) throws IOException, BadCharacterException, NoAPIKeyException {
		String apiKey;
		region = region.replace("na", "us");
		apiKey = Bot.getInstance().getApiKeys().get("battlenet");
		JsonObject jo = Util.jsonFromUrl((String.format(baseUrl, region)
				+ server + "/" + name + "?fields=statistics,progression,guild,items&apikey=" + apiKey)
				.replaceAll(" ", "%20")).asObject();
		if(!(jo.get("status") == null)) {
			if(jo.get("status").asString().equals("nok")) {
				throw new BadCharacterException();
			}
		}

		//Raid progression
		for(JsonValue jv : jo.get("progression").asObject().get("raids").asArray()) {
			JsonObject j = jv.asObject();
			lfrRaid += j.get("lfr").asInt();
			normalRaid += j.get("normal").asInt();
			heroicRaid += j.get("heroic").asInt();
			mythicRaid += j.get("mythic").asInt();
			for(JsonValue jv2 : j.get("bosses").asArray()) {
				JsonObject j2 = jv2.asObject();
				lfrKills += j2.getInt("lfrKills", 0);
				normalKills += j2.getInt("normalKills", 0);
				heroicKills += j2.getInt("heroicKills", 0);
				mythicKills += j2.getInt("mythicKills", 0);
			}
		}

		this.username = jo.get("name").asString();
		this.realm = jo.get("realm").asString();
		this.gender = (jo.get("gender").asInt() == 0 ? "Male" : "Female");
		this.gameClass = ((HashMap<Integer, String>) cache.get("wowclasses")).get(jo.get("class").asInt());
		this.race = ((HashMap<Integer, String>) cache.get("wowraces")).get(jo.get("race").asInt());
		this.level = jo.get("level").asInt();
		this.achievementPoints = jo.get("achievementPoints").asInt();
		this.itemLevel = jo.get("items").asObject().get("averageItemLevel").asInt();
		
		if(jo.get("guild") != null) {
			this.guild = jo.get("guild").asObject().getString("name", null);
			this.guildMembers = jo.get("guild").asObject().get("members").asInt();
		} else {
			this.guild = null;
		}

		this.thumbnail = jo.get("thumbnail").asString();
		this.thumbnail = thumbnail.replace("avatar", "profilemain");
		this.thumbnail = String.format("http://render-%s.worldofwarcraft.com/character/%s", region, thumbnail);
	}

	public int getLfrRaid() {
		return lfrRaid;
	}

	public int getNormalRaid() {
		return normalRaid;
	}

	public int getHeroicRaid() {
		return heroicRaid;
	}

	public int getMythicRaid() {
		return mythicRaid;
	}

	public int getLfrKills() {
		return lfrKills;
	}

	public int getNormalKills() {
		return normalKills;
	}

	public int getHeroicKills() {
		return heroicKills;
	}

	public int getMythicKills() {
		return mythicKills;
	}

	public String getUsername() {
		return username;
	}

	public String getGender() {
		return gender;
	}

	public String getGameClass() {
		return gameClass;
	}

	public String getRace() {
		return race;
	}

	public int getLevel() {
		return level;
	}

	public int getItemLevel() {
		return itemLevel;
	}

	public int getAchievementPoints() {
		return achievementPoints;
	}

	public String getGuild() {
		return guild;
	}

	public int getGuildMembers() {
		return guildMembers;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public String getError() {
		return error;
	}

	public String getRealm() {
		return realm;
	}

	static {
		JsonObject jo2;
		try {
			jo2 = Util.jsonFromUrl("https://us.api.battle.net/wow/data/character/races?locale=en_US&apikey=" 
					+ Bot.getInstance().getApiKeys().get("battlenet")).asObject();

			HashMap<Integer, String> races = new HashMap<Integer, String>();
			for(JsonValue j : jo2.get("races").asArray()) {
				races.put(j.asObject().get("id").asInt(), j.asObject().get("name").asString());
			}
			cache.put("wowraces", races);

			jo2 = Util.jsonFromUrl("https://us.api.battle.net/wow/data/character/classes?locale=en_US&apikey="  
					+ Bot.getInstance().getApiKeys().get("battlenet")).asObject();
			HashMap<Integer, String> classes = new HashMap<Integer, String>();
			for(JsonValue j : jo2.get("classes").asArray()) {
				classes.put(j.asObject().get("id").asInt(), j.asObject().get("name").asString());
			}
			cache.put("wowclasses", classes);
		} catch (IOException | NoAPIKeyException e) {
			LoggerFactory.getLogger(WoWCharacter.class)
			.warn("You do not have a valid Battle.net API key.\nAll Battle.net services are disabled");
		}
	}
}
