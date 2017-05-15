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
			this.imageLink = doc.select("div.character__detail__image").select("img").first().attr("src");
			String raceGender = doc.select("div.character-block__box").first()
					.select("p.character-block__name").first()
					.html();
			this.race = raceGender.substring(0, raceGender.indexOf('<'));
			this.faction = raceGender.substring(raceGender.indexOf('>') + 1, raceGender.indexOf('/') - 1);
			this.gender = raceGender.contains("♀") ? "Female" : "Male";
			
			Element charProfile = doc.select("div.character-block__box").get(1);
			this.nameday = charProfile.select("p.character-block__birth").first().text();
			this.guardian = charProfile.select("p.character-block__name").first().text();
			
			Element cityStateEle = doc.select("div.character-block__box").get(2);
			this.cityState = cityStateEle.select("p.character-block__name").first().text();
			
			int profileElements = doc.select("div.character__profile__data__detail").first()
					.select("div.character-block").size();
			if(profileElements == 5) { // Has both grand & free
				this.grandCompany = doc.select("div.character-block__box").get(3)
						.select("p.character-block__name").first().text();
				this.freeCompany = doc.select("div.character-block__box").get(4)
						.select("div.character__freecompany__name").first()
						.select("a").first().text();
			} else if(profileElements == 4) { // Has only grand OR free
				if (doc.select("div.character__freecompany__crest").isEmpty()) {
					this.grandCompany = doc.select("div.character-block__box").get(3)
							.select("p.character-block__name").first().text();
					this.freeCompany = "none";
				} else {
					this.freeCompany = doc.select("div.character-block__box").get(4)
							.select("div.character__freecompany__name").first()
							.select("a").first().text();
					this.grandCompany = "none";
				}
			} else {
				this.freeCompany = "none";
				this.grandCompany = "none";
			}

			this.jobImageLink = doc.select("div.character__class_icon").first().select("img").first().attr("src");
		} catch (IOException e) {
			throw new IOException();
		}
	}

	/**
	 * Get and retrieve the character URL for an FFXIV character
	 * @param firstName First name fo character
	 * @param lastName Last name of character
	 * @param server Server name
	 * @return URL of character if found, null if not found or an exception occurs
	 */
	private static String getCharacterUrl(String firstName, String lastName, String server) {
		String world = StringUtils.capitalize(server.toLowerCase());
		String url = String.format("http://na.finalfantasyxiv.com/lodestone/character/?q=%s+%s&worldname=%s&classjob=&race_tribe=&order=", 
				firstName, lastName, world);
		final String first = StringUtils.capitalize(firstName.toLowerCase());
		final String last = StringUtils.capitalize(lastName.toLowerCase());
		try {
			Document doc = Jsoup.parse(new URL(url), 10000);
			Elements eles = doc.getElementsByClass("entry__link");
			if(eles.size() == 0) {
				return null;
			}
			
			Element character = eles.stream()
					.filter(e -> e.select("div.entry__box--world").first().select("p.entry__name").first()
							.text().equalsIgnoreCase(first + " " + last))
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
