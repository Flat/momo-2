package io.ph.web.beans;

import org.apache.commons.lang3.text.WordUtils;

public class SparkGuildBean {
	private String id;
	private String name;
	private String icon;
	
	public SparkGuildBean(String id, String name, String icon) {
		this.id = id;
		this.name = name;
		this.icon = icon;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return WordUtils.capitalize(this.name);
	}
	
	public String getIcon() {
		if(this.icon == null || this.icon.contains("null")) {
			return "";
		}
		return this.icon;
	}
}
