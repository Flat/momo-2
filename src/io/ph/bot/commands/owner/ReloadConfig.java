package io.ph.bot.commands.owner;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.entities.Message;

@CommandData (
		defaultSyntax = "reloadconfig",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Reload config",
		example = "(no params)"
		)
public class ReloadConfig extends Command {

	@Override
	public void executeCommand(Message msg) {
		Bot.getInstance().loadProperties();
		msg.getAuthor().openPrivateChannel().queue(ch -> {
			ch.sendMessage("Reloaded").queue();
		});
	}

}
