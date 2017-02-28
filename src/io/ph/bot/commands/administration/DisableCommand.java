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
 * Disable a command
 * @author Paul
 */
@CommandData (
		defaultSyntax = "disable",
		aliases = {"disablecommand"},
		permission = Permission.MANAGE_ROLES,
		description = "Disable a command.\n"
				+ "Only normal user commands can be disabled. You can see the status by using the commandstatus command. "
				+ "Use \"disable all\" to disable all user commands",
		example = " macro"
		)
public class DisableCommand extends Command {

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
			g.disableAllCommands();
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("All commands have been disabled");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		try {
			if(g.disableCommand(content)) {
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("**" + content + "** has been disabled");
			} else {
				em.setTitle("Hmm...", null)
				.setColor(Color.CYAN)
				.setDescription("**" + content + "** is already disabled");
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
