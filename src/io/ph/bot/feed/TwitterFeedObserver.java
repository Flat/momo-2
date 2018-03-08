package io.ph.bot.feed;

import java.awt.Color;
import java.io.Serializable;
import java.time.Instant;

import io.ph.bot.Bot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import twitter4j.MediaEntity;
import twitter4j.Status;

public class TwitterFeedObserver implements Serializable {
	private static final long serialVersionUID = -4749221302251416947L;

	private String discoChannel;
	private String twitterHandle;
	private boolean showPictures;
	private boolean showRetweets;
	private boolean showReplies;

	public TwitterFeedObserver(String discoChannel, String twitterHandle, boolean showPictures,
			boolean showRetweets, boolean showReplies) {
		this.discoChannel = discoChannel;
		this.twitterHandle = twitterHandle;
		this.showPictures = showPictures;
		this.showRetweets = showRetweets;
		this.showReplies = showReplies;
	}

	public boolean trigger(Status status) {
		if(getDiscoChannel() == null || !getDiscoChannel().canTalk()) {
			return false;
		}
		if ((status.isRetweet() && !showRetweets)
				|| (status.getInReplyToScreenName() != null && !showReplies)) {
			return true;
		}
		EmbedBuilder em = new EmbedBuilder();
		String text = status.getText();
		em.setTitle("New tweet from \\@" + status.getUser().getScreenName(),
				"https://twitter.com/" + status.getUser().getScreenName() 
				+ "/status/" + status.getId())
		.setColor(Color.MAGENTA)
		.setThumbnail(status.getUser().getProfileImageURL())
		.setDescription(text);

		String url = null;
		for(MediaEntity e : status.getMediaEntities()) {
			if(this.showPictures && url == null && (e.getType().equals("photo")))
				url = e.getMediaURL();
			text = text.replaceAll(e.getURL(), "");
		}
		if(url != null)
			em.setImage(url);
		if(status.getMediaEntities().length > 0 && url == null || status.getMediaEntities().length > 1) {
			em.setFooter("Tweet contains more media", null);
		} else {
			em.setFooter("Local time", null);
		}
		em.setTimestamp(Instant.now());
		getDiscoChannel().sendMessage(em.build()).queue();
		return true;
	}

	public int subscribe(long twitterId) {
		return TwitterEventListener.addTwitterFeed(twitterId, this);
	}

	public String getDiscoChannelId() {
		return this.discoChannel;
	}

	public TextChannel getDiscoChannel() {
		return Bot.getInstance().shards.getTextChannelById(discoChannel);
	}

	public String getTwitterHandle() {
		return twitterHandle;
	}
	
	public void test() {
		this.showReplies = true;
		this.showRetweets = true;
	}
}
