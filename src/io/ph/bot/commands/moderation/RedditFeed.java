package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.RedditFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dean.jraw.http.NetworkException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Set reddit feed from given channel
 * @author Paul
 */
@CommandData (
		defaultSyntax = "reddit",
		aliases = {"subreddit", "redditfeed"},
		permission = Permission.KICK,
		description = "Set this channel as a reddit feed for a given subreddit.\n"
				+ "Use the unreddit command to remove and listreddit command to show all feeds\n"
				+ "The bot does not show images from posts that have the word \"spoiler\" in the title. "
				+ "However, be aware that if you show NSFW images, images that are marked NSFW for the purpose of spoilers will show!",
				example = "awwnime"
		)
@ProceduralAnnotation (
		title = "Reddit feed",
		steps = {"Show images in updates? Answer with all/no nsfw/none", "Show text preview for self posts?"}, 
		types = {StepType.STRING, StepType.YES_NO},
		breakOut = "finish"
		)
public class RedditFeed extends ProceduralCommand {

	public RedditFeed(Message msg) {
		super(msg);
	}

	/**
	 * Necessary constructor to register to commandhandler
	 */
	public RedditFeed() {
		super(null);
	}

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(RedditEventListener.getObserver(contents, msg.getGuild()) != null) {
			if(RedditEventListener.getObserver(contents, msg.getGuild()).getDiscoChannel().equals(msg.getTextChannel())) {
				em.setTitle("Error",  null)
				.setColor(Color.RED)
				.setDescription("The subreddit /r/**" + contents + "** is already feeding to this channel");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			} else {
				// Remove previous channel as they are changing it
				RedditEventListener.removeRedditFeed(contents, msg.getGuild());
			}
		}
		try {
			RedditEventListener.redditClient.getSubreddit(contents);
		} catch(NetworkException | IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("The subreddit /r/**" + contents + "** does not exist");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		RedditFeed instance = new RedditFeed(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void finish() {
		EmbedBuilder em = new EmbedBuilder();
		Message msg = super.getStarter();
		String contents = Util.getCommandContents(msg);

		em.setTitle("Success", null);
		em.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
		em.setDescription(String.format("Registered /r/**%s** for notifications", contents));
		String response = (String) super.getResponses().get(0);
		boolean showImg;
		boolean showNsfw;
		switch(response.toLowerCase()) {
		case "all":
		case "yes":
			showImg = true;
			showNsfw = true;
			break;
		case "no nsfw":
			showImg = true;
			showNsfw = false;
			break;
		case "none":
		case "no":
			showImg = false;
			showNsfw = false;
			break;
			// Since the following combination can never happen, good way to check if invalid response
		default:
			showImg = false;
			showNsfw = true;
			break;
		}
		if(!showImg && showNsfw) {
			super.sendMessage("Error: Invalid response. Please re-register this subreddit feed");
			super.exit();
			return;
		}
		new RedditFeedObserver(msg.getChannel().getId(), contents, showImg, showNsfw, (boolean) super.getResponses().get(1));
		msg.getChannel().sendMessage(em.build()).queue();
		super.exit();
	}
}
