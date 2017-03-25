package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Mini tutorial for users
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "howto",
		aliases = {"tutorial"},
		permission = Permission.NONE,
		description = "PM a guide to the user, specified by the given input. "
				+ "Doing just the command will give you a list of help topics",
				example = "setup"
		)
public class HowTo extends Command {
	EmbedBuilder em;
	@Override
	public void executeCommand(Message msg) {
		em = new EmbedBuilder();
		em.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA));
		String s = Util.getCommandContents(msg);
		String prefix = GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix();
		switch(s.toLowerCase()) {
		case "setup":
			setupMessage(prefix);
			break;
		case "music":
			setupMusic(prefix);
			break;
		case "live feeds":
		case "livefeeds":
		case "live":
			setupFeeds(prefix);
			break;
		case "moderation":
			setupModeration(prefix);
			break;
		case "roles":
		case "role management":
			setupRoleManagement(prefix);
			break;
		default:
			defaultMessage(prefix, msg.getTextChannel());
			return;
		}
		em.setFooter(String.format("Current version: %s", Bot.BOT_VERSION), null);
		msg.getAuthor().openPrivateChannel().queue(ch -> {
			ch.sendMessage(em.build()).queue();
		});
		EmbedBuilder em2 = new EmbedBuilder();
		em2.setTitle("Success", null)
		.setColor(Color.GREEN)
		.setDescription("Check your PMs!");
		msg.getChannel().sendMessage(em2.build()).queue();
	}

	private void setupRoleManagement(String prefix) {
		em.setTitle("Role management", null)
		.setDescription("Users with manage role+ permissions can setup and disable *joinable roles*. "
				+ "Joinable roles allow users to join a role to show their flair, whether it's allegiance "
				+ "to a character or to a color")
		.addField("Creating a joinable role", String.format("You can create a joinable role with "
				+ "`%sjoinablerole name-of-role`. If the role doesn't exist, I will create it for you. "
				+ "If it does exist, I'll use that role. \n"
				+ "`%<sdisablerole name-of-role` will disable this as a joinable role. "
				+ "Note that this will not automatically remove users from this role!\n"
				+ "If you want a listing of roles, you can use `%<srolestats`", prefix), false);
	}

	private void setupModeration(String prefix) {
		em.setTitle("Moderation features", null)
		.setDescription("I provide various moderation functions to both streamline mutes, kicks, and bans, as well as "
				+ "limiting usage on my commands")
		.addField("Timed mutes and bans", String.format("As well as offering indefinite mutes and bans, "
				+ "you can use the `%smute` and %<sban` commands to temporarily punish a user.\n"
				+ "To do so, use the `temp` parameter with a time in this format: #w#d#h#s. For example, "
				+ "to mute someone for 1 day and 2 hours, do `%<smute temp 1d2h @target`. "
				+ "Same syntax with bans: `%<sban temp 1d2h @target`", prefix), false)
		.addField("Enabling & disabling commands", String.format("You can enable or disable commands for users "
				+ "with the `%senablecommand` and `%<sdisablecommand`. You can then check "
				+ "the status of your commands with `%<scommandstatus`\n"
				+ "Note: disabled commands can still be used by users with kick+ permissions", prefix), false);
	}

	private void setupFeeds(String prefix) {
		em.setTitle("Live feeds", null)
		.setDescription("This tutorial briefly goes over Twitch.tv, Twitter, and Reddit feeds. You need at least the *kick* permission")
		.addField("Twitch.tv", String.format("You can register Twitch.tv channels for automatic notifications "
				+ "that trigger when they go online and offline. To do so, use the `%stwitchchannel` "
				+ "in the channel you want to register. Then, use `%<stwitch username` to register. "
				+ "To undo, do `%<sunregistertwitch username`", prefix), false)
		.addField("Subreddits", String.format("Registering subreddits is straightforward. \n"
				+ "Use the `%sreddit subreddit` command to register a subreddit for notifications. "
				+ "You can then connfigure various features, such as showing all/no nsfw/no images and text previews.\n"
				+ "You can remove a subscription with `%<sremovereddit subreddit`. \n"
				+ "To list all of your subscriptions, use `%<slistreddit`", prefix), false)
		.addField("Twitter", String.format("Twitter follows the same format as subreddits.\n"
				+ "Use `%stwitter twitter-name` to register a Twitter account.\n"
				+ "To remove it, use `%<sremovetwitter twitter-name`\n"
				+ "To list all of your subscriptions, use `%<stwitterlist`", prefix), false);
	}

	private void setupMusic(String prefix) {
		em.setTitle("Music usage", null)
		.setDescription(String.format("This is a quick tutorial on how to use my music features. "
				+ "If you did not do %ssetupmusic, I will join whatever channel you're in. "
				+ "If you did do %<ssetupmusic, I will join only that channel", prefix))
		.addField("Supported sources", "I currently support the following sources: "
				+ "YouTube videos & playlists, direct links, attachments sent through discord, "
				+ ".webm files (such as the music on Themes.moe), Soundcloud, Bandcamp, and more!", false)
		.addField("Playing music", String.format("Playing music is easy. "
				+ "Use the `%smusic` command with your URL directly afterwards. "
				+ "This will automatically add it to the queue, and, after a short processing period, will play in "
				+ "the designated music voice channel", 
				prefix), false)
		.addField("Integration", String.format("The `%smusic` command can be integrated with two built-in search functions\n"
				+ "After you use the `%<stheme` and `%<syoutube` commands, you get a numbered list. You can then "
				+ "use `%<smusic #` on the result to directly play music",
				prefix), false)
		.addField("Options", String.format("I have various options you can use with the `%smusic` command.\n"
				+ "`%<smusic skip` adds a vote to skip the song\n"
				+ "`%<smusic now` shows the current song and timestamp\n"
				+ "`%<smusic next` shows the current queue\n"
				+ "`%<smusic volume #` allows moderators to change the volume\n"
				+ "`%<smusic shuffle` allows moderators to shuffle the queue\n"
				+ "`%<smusic stop` allows moderators to kill the queue",
				prefix), false);
	}
	private void setupMessage(String prefix) {
		em.setTitle("Basic setup", null)
		.setDescription("These are the first three commands you should be doing.")
		.addField("Mute setup", String.format("Muting requires me to create a special role. "
				+ "This role will have special permission overrides for every channel, preventing them from sending messages. "
				+ "To do this, make sure you have the Manage Server role and do `%ssetup`", 
				prefix), false)
		.addField("Basic configuration", String.format("I feature a web-dashboard you can use to configure me. "
				+ "Access it at <https://momobot.io/dash> - it's self explanatory and very simple!",
				prefix), false)
		.addField("Music", String.format("Last, but not least, is music. You have two options for how the music "
				+ "system should work. \n**1)** Have the bot join whatever channel you're in to play music: No action required\n"
				+ "**2)** Specify a single channel the bot should go to: Do `%ssetupmusic`\n"
				+ "Then, if you want music announcements for when a new song is playing, do `%<smusicchannel` in a "
				+ "designated channel.", 
				prefix), false)
		.addField("Support", String.format("If you need help with the bot, feel free to join my support server at %s",
				"https://momobot.io/join"), false);
	}
	private void defaultMessage(String prefix, TextChannel channel) {
		em.setTitle("How To options", null)
		.setColor(Color.MAGENTA)
		.setDescription(String.format("Do `%showto` with a topic afterwards, i.e. `%<showto setup`.\n"
				+ "If you just want a list of commands, do `%<scommands`\n"
				+ "More info, invite links, and the dashboard can be found at my website: <https://momobot.io>", prefix))
		.addField("Options", "setup, moderation, role management, live feeds, music", true);
		channel.sendMessage(em.build()).queue();
	}
}
