package io.ph.bot.commands.administration;

import java.awt.Color;
import java.time.Instant;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Change server's command prefix
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "changeprefix",
		aliases = {"changecommandprefix", "commandprefix"},
		permission = Permission.MANAGE_SERVER,
		description = "Change the server's command prefix",
		example = "#"
		)
public class ChangeCommandPrefix extends Command {
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(contents.contains(" ")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Cannot have spaces in your command prefix");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		GuildObject.guildMap.get(msg.getGuild()
				.getId()).getConfig().setCommandPrefix(contents);
		em.setTitle("Success", null)
		.setColor(Color.GREEN)
		.setDescription("Changed command prefix to " + contents)
		.setTimestamp(Instant.now());
		msg.getChannel().sendMessage(em.build()).queue();
	}


}
