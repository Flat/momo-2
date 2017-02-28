package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.IllegalArgumentException;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Enable a command
 * @author Paul
 */
@CommandData (
		defaultSyntax = "enable",
		aliases = {"enablecommand"},
		permission = Permission.MANAGE_ROLES,
		description = "Enable a command.\n"
				+ "Only normal user commands can be enabled. You can see the status by using the commandstatus command. "
				+ "Use \"enable all\" to enable all commands",
		example = "macro"
		)
public class EnableCommand extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(content.equals("all")) {
			g.enableAllCommands();
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("All commands have been disabled");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		try {
			if(g.enableCommand(content)) {
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("**" + content + "** has been enabled");
			} else {
				em.setTitle("Hmm...", null)
				.setColor(Color.CYAN)
				.setDescription("**" + content + "** is already enabled");
			}
		} catch(IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("**" + content + "** is not a valid command.\n"
					+ "If you need valid options, do " + g.getConfig().getCommandPrefix() + "commandstatus");
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
