package io.ph.bot.model.games;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.ph.bot.exception.BadCharacterException;

/**
 * Process an ffxiv character and display an embed with useful stats
 * @author Paul
 *
 */
public class FFXIVCharacter {
	private String server;
	private String firstName;
	private String lastName;

	private String lodestoneLink;
	private String imageLink;
	private String jobImageLink;

	private String race;
	private String faction;
	private String gender;

	private String nameday;
	private String guardian;
	private String cityState;

	private String grandCompany;
	private String freeCompany;

	public FFXIVCharacter(String server, String firstName, String lastName) throws IOException, BadCharacterException {
		this.server = StringUtils.capitalize(server.toLowerCase());
		this.firstName = StringUtils.capitalize(firstName.toLowerCase());
		this.lastName = StringUtils.capitalize(lastName.toLowerCase());

		try {
			lodestoneLink = getCharacterUrl(firstName, lastName, server);
			if(lodestoneLink == null) {
				throw new BadCharacterException();
			}
			Document doc = Jsoup.parse(new URL(lodestoneLink), 10000);
			this.imageLink = doc.select(".bg_chara_264").select("img").first().attr("src");
			String[] raceGender = doc.select("div.chara_profile_title").first().text().split(" / ");
			this.race = raceGender[0];
			this.faction = raceGender[1];
			this.gender = raceGender[2].equalsIgnoreCase("♀") ? "Female" : "Male";
			
			Elements charProfile = doc.select("dl.chara_profile_box_info");
			this.nameday = charProfile.get(0).select(".txt_name").first().text();
			this.guardian = charProfile.get(0).select(".txt_name").get(1).text();
			this.cityState = charProfile.get(1).select(".txt_name").first().text();
			if(charProfile.size() == 4) {
				this.grandCompany = doc.select("dl.chara_profile_box_info").get(2).select(".txt_name").first().text();
				this.freeCompany = doc.select("dl.chara_profile_box_info").get(3).select(".txt_name").first().text();
			} else if(charProfile.size() == 3) {
				String s = charProfile.get(2).select(".txt").first().text();
				if(s.contains("Free")) {
					this.freeCompany = charProfile.get(2).select(".txt_name").first().text();
					this.grandCompany = "none";
				} else {
					this.grandCompany = charProfile.get(2).select(".txt_name").first().text();
					this.freeCompany = "none";
				}
			} else {
				this.freeCompany = "none";
				this.grandCompany = "none";
			}

			this.jobImageLink = doc.select("div#class_info").first().select("img").first().attr("src");
		} catch (IOException e) {
			throw new IOException();
		}
	}

	private static String getCharacterUrl(String firstName, String lastName, String server) {
		String world = StringUtils.capitalize(server.toLowerCase());
		String url = String.format("http://na.finalfantasyxiv.com/lodestone/character/?q=%s+%s&worldname=%s&classjob=&race_tribe=&order=", 
				firstName, lastName, world);
		final String first = StringUtils.capitalize(firstName.toLowerCase());
		final String last = StringUtils.capitalize(lastName.toLowerCase());
		try {
			Document doc = Jsoup.parse(new URL(url), 10000);
			Elements eles = doc.getElementsByClass("player_name_area");
			if(eles.size() == 0) {
				return null;
			}
			
			Element character = eles.stream()
					.filter(e -> e.select("a").first().text().equals(first + " " + last))
					.findFirst()
					.get().select("a").first();

			String charUrl = "http://na.finalfantasyxiv.com" + character.attr("href");
			return charUrl;
		} catch(NoSuchElementException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getServer() {
		return server;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getLodestoneLink() {
		return lodestoneLink;
	}

	public String getImageLink() {
		return imageLink;
	}

	public String getJobImageLink() {
		return jobImageLink;
	}

	public String getRace() {
		return race;
	}

	public String getFaction() {
		return faction;
	}

	public String getGender() {
		return gender;
	}

	public String getNameday() {
		return nameday;
	}

	public String getGuardian() {
		return guardian;
	}

	public String getCityState() {
		return cityState;
	}

	public String getGrandCompany() {
		return grandCompany;
	}

	public String getFreeCompany() {
		return freeCompany;
	}
}
