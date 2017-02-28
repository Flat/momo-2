package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Change server's log level
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "loglevel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Set log level of your log channel. Valid: normal, advanced\n"
				+ "Normal logs user join, leave, ban, kick, mute, and nickname updates.\n"
				+ "Advanced logs message edits and deletions",
		example = " normal"
		)
public class LogLevel extends Command {
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg).toLowerCase();
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if(contents.equals("advanced")) {
			em.setTitle("Log level set", null)
			.setColor(Color.GREEN)
			.setDescription("Log level set to advanced");
			g.getConfig().setAdvancedLogging(true);
		} else {
			em.setTitle("Log level set", null)
			.setColor(Color.GREEN)
			.setDescription("Log level set to normal");
			g.getConfig().setAdvancedLogging(false);
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
