package io.ph.bot.commands.administration;

import java.awt.Color;
import java.util.Map.Entry;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Check on enabled/disabled commands
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "commandstatus",
		aliases = {"status"},
		permission = Permission.MANAGE_ROLES,
		description = "Check all toggleable commands, listing which are enabled and which are disabled",
		example = "(no parameters)"
		)
public class CommandStatus extends Command {

	@Override
	public void executeCommand(Message msg) {
		String contents = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		if(contents.length() > 0) {
			if(GuildObject.guildMap.get(msg.getGuild().getId()).validCommandToEdit(msg.getContent())) {
				em.setTitle(contents + " is not a valid command", null)
				.setColor(Color.RED);
			} else {
				GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
				em.setTitle("Status of " + g.getConfig().getCommandPrefix() + contents, null)
				.setColor(g.getCommandStatus(contents) == true ? Color.GREEN : Color.RED)
				.setDescription(g.getCommandStatus(contents) == true ? "Enabled" : "Disabled");
			}
		} else {
			em.setTitle("Status of all user commands", null)
			.setColor(Color.CYAN);
			StringBuilder sb = new StringBuilder();
			StringBuilder sbDis = new StringBuilder();
			sb.append("**Enabled**: ");
			sbDis.append("**Disabled**: ");
			for(Entry<String, Boolean> entry : GuildObject.guildMap.get(msg.getGuild().getId()).getCommandStatus().entrySet()) {
				if(entry.getValue() && CommandHandler.getCommand(entry.getKey()) != null)
					sb.append(entry.getKey() + ", ");
				else if(!entry.getValue() && CommandHandler.getCommand(entry.getKey()) != null)
					sbDis.append(entry.getKey() + ", ");
			}
			sb.setLength(sb.length() - 2);
			sbDis.setLength(sbDis.length() - 2);
			sb.append("\n").append(sbDis.toString());
			em.setDescription(sb.toString());
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
