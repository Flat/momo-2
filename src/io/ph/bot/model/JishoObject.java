package io.ph.bot.model;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.ph.util.Util;

public class JishoObject {

	private String kana;
	private String kanji;
	private boolean common;
	private String englishDefinitions;
	private String tags; //"usually written in kana" etc


	public JishoObject() {

	}

	public static ArrayList<JishoObject> searchVocabulary(String query) {
		ArrayList<JishoObject> toReturn = null;
		try {
			String url = "http://jisho.org/search/"+URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8.toString());

			Document doc = Jsoup.parse(Util.stringFromUrl(url));
			if(doc.select("div#no-matches").hasText())
				return null;

			Elements vocab = doc.select("div.concept_light");
			int breakCount = 0;
			int toBreak = 3;

			toReturn = new ArrayList<JishoObject>();

			for(Element e : vocab) {
				if(breakCount++ == toBreak) {
					break;
				}
				JishoObject j = new JishoObject();

				j.setKana(e.select("div.concept_light-representation").select("span.furigana").text());
				j.setKanji(e.select("span.text").text());

				String s;
				if(!(s = e.select("span.concept_light-tag.label").not(".light-common").text()).equals(""))
					j.setTags(s);
				Element meaningsWrapper = e.select("div.concept_light-meanings.medium-9.columns").first().child(0);

				StringBuilder sb = new StringBuilder();
				for(Element e2 : meaningsWrapper.select("div.meaning-wrapper")) {
					if(e2.parent().parent().parent().parent().hasClass("concepts")) {
						j = null;
						break;
					}
					if(!e2.previousElementSibling().text().contains("Wikipedia")) {
						if(e2.previousElementSibling().hasClass("meaning-tags"))
							sb.append("**"+e2.previousElementSibling().text()+"**\n");
						Element def = e2.select("div.meaning-definition.zero-padding").first();
						Elements defE = def.children().not(".supplemental_info");
						for(Element iter : defE) {
							sb.append(iter.text()+" ");
						}

						if(e2.select("span.supplemental_info").hasText())
							sb.append(" *"+e2.select("span.supplemental_info").text()+"*");
						sb.append("\n");
					}
				}
				if(j != null) {
					j.setEnglishDefinitions(sb.toString());
					toReturn.add(j);
				}
			}
			
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		return toReturn;
	}


	public String getKana() {
		return kana;
	}
	public void setKana(String kana) {
		this.kana = kana;
	}
	public String getKanji() {
		return kanji;
	}
	public void setKanji(String kanji) {
		this.kanji = kanji;
	}
	public boolean isCommon() {
		return common;
	}
	public void setCommon(boolean common) {
		this.common = common;
	}
	public String getEnglishDefinitions() {
		return englishDefinitions;
	}
	public void setEnglishDefinitions(String englishDefinitions) {
		this.englishDefinitions = englishDefinitions;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
}
