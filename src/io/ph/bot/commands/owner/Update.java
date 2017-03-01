package io.ph.bot.commands.owner;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;

@CommandData (
		defaultSyntax = "update",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Start an update timer in the status to say \"Restart in n\" where n is minutes."
				+ " Doesn't actually kill the bot at 0",
		example = "5"
		)
public class Update extends Command {

	@Override
	public void executeCommand(Message msg) {
		try {
			StatusChangeJob.commenceUpdateCountdown(Integer.parseInt(Util.getCommandContents(msg)));
			msg.getChannel().sendMessage("Set a timer for " + Util.getCommandContents(msg)).queue();
		} catch(NumberFormatException e) {
			msg.getChannel().sendMessage(Util.getCommandContents(msg) + " is not a valid integer");
		}
	}

}
