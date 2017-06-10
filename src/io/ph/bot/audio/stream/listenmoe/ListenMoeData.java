package io.ph.bot.audio.stream.listenmoe;

public class ListenMoeData {
	public static ListenMoeData instance;
	
	private String requester;
	private String animeName;
	private int listeners;
	private String artist;
	private String songName;
	private int songId;
	
	public String getRequester() {
		return requester;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public String getAnimeName() {
		return animeName;
	}

	public void setAnimeName(String animeName) {
		this.animeName = animeName;
	}

	public int getListeners() {
		return listeners;
	}

	public void setListeners(int listeners) {
		this.listeners = listeners;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public int getSongId() {
		return songId;
	}

	public void setSongId(int songId) {
		this.songId = songId;
	}
	
	public static ListenMoeData getInstance() {
		if (instance == null) {
			instance = new ListenMoeData();
		}
		return instance;
	}
}
