package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.feed.TwitterFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import twitter4j.TwitterException;
import twitter4j.User;

@CommandData (
		defaultSyntax = "twitter",
		aliases = {"twitterfeed"},
		permission = Permission.KICK,
		description = "Subscribe to a Twitter account and get tweets delivered to this channel",
		example = "FF_XIV_EN"
		)
@ProceduralAnnotation (
		title = "Twitter feed",
		steps = {"Show images in updates?", "Show retweets?", "Show replies?"}, 
		types = {StepType.YES_NO, StepType.YES_NO, StepType.YES_NO},
		breakOut = "finish"
		)
public class TwitterFeed extends ProceduralCommand {

	public TwitterFeed() {
		super(null);
	}
	public TwitterFeed(Message msg) {
		super(msg);
	}

	private EmbedBuilder em = new EmbedBuilder();

	@Override
	public void executeCommand(Message msg) {		
		if(TwitterEventListener.twitterClient == null) {
			return;
		}

		User u;
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		try {
			u = TwitterEventListener.twitterClient.lookupUsers(new String[]{contents}).get(0);
			if(TwitterEventListener.getObserver(u.getId(), msg.getGuild()) != null) {
				if(TwitterEventListener.getObserver(u.getId(), msg.getGuild()).getDiscoChannel().equals(msg.getChannel())) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("The Twitter account **" + u.getScreenName() 
					+ "** is already feeding to this channel");
					msg.getChannel().sendMessage(em.build()).queue();
					return;
				} else {
					TwitterEventListener.removeTwitterFeed(u.getId(), msg.getGuild());
				}
			}
		} catch(TwitterException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED);
			if(e.getErrorCode() == 17) {
				em.setDescription(String.format("Twitter account **%s** does not exist", contents));
			} else {
				em.setDescription("Unspecified error");
			}
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		TwitterFeed instance = new TwitterFeed(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
		instance.addCache(u);
	}
	@Override
	public void finish() {
		Message msg = super.getStarter();
		User u = (User) super.getCache().get(0);
		int timeLeft = (new TwitterFeedObserver(msg.getChannel().getId(),
				u.getScreenName(), (boolean) super.getResponses().get(0), (boolean) super.getResponses().get(1),
				(boolean) super.getResponses().get(2))).subscribe(u.getId());
		em.setTitle("Success", null)
		.setColor(Color.CYAN);

		String when;
		if(timeLeft == -1)
			when = "within " + TwitterEventListener.DELAY;
		else
			when = "in " + timeLeft;
		em.setDescription("Subscribed to feeds from **" + u.getScreenName() + "**\nChanges will take effect " + when + " seconds");
		msg.getChannel().sendMessage(em.build()).queue();
		super.exit();
	}

}
