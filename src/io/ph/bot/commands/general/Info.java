package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Information & intro
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "info",
		aliases = {"information"},
		permission = Permission.NONE,
		description = "Information on the bot",
		example = "(no parameters)"
		)
public class Info extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Hi, I'm " + msg.getGuild().getMember(msg.getJDA().getSelfUser()).getEffectiveName(), null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA))
		.addField("Repository", "<https://momobot.io/github>", true)
		.addField("Help server", "<https://momobot.io/join>", true)
		.addField("Invite link", Bot.getInstance().getConfig().getBotInviteLink(), true)
		.addField("Command list", "<https://momobot.io/commands.html>", true)
		.setDescription("I can do a lot of things! Too many to list here, though. Feel free to take a look "
				+ "through the links below, though, to get a quick rundown of my features")
		.setThumbnail(msg.getJDA().getSelfUser().getAvatarUrl())
		.setFooter(String.format("Version %s | Made with <3 by %s", 
				Bot.BOT_VERSION,
				"Kagumi"), null);
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
