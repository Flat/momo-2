package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
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
		category = CommandCategory.UTILITY,
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
		.addField("Repository", "<https://github.com/Flat/Momo-2>", true)
		.addField("Invite link", Bot.getInstance().getConfig().getBotInviteLink(), true)
		.setThumbnail(msg.getJDA().getSelfUser().getAvatarUrl())
		.setFooter(String.format("Version %s", 
				Bot.BOT_VERSION
				), null);
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
