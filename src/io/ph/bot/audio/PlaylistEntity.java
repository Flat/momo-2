package io.ph.bot.audio;

public class PlaylistEntity {
	private String title;
	private String url;
	
	public PlaylistEntity(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}
}
