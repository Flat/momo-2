package io.ph.bot.commands.music;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.entities.Message;


@CommandData (
		defaultSyntax = "next",
		aliases = {"queue"},
		permission = Permission.NONE,
		description = "Tells you the list of songs in queue",
		example = ""
		)
public class Next extends Command {

	@Override
	public void executeCommand(Message msg) {
		Music.next(msg);
	}
}
