package io.ph.bot.commands.general;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.entities.Message;

/**
 * Send the invite link to this channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "invite",
		aliases = {},
		permission = Permission.NONE,
		description = "Send a link to invite me to a server",
		example = "(no parameters)"
		)
public class Invite extends Command {

	@Override
	public void executeCommand(Message msg) {
		if(Bot.getInstance().getConfig().getBotInviteLink() == null)
			return;
		msg.getChannel().sendMessage(Bot.getInstance().getConfig().getBotInviteLink()).queue();
	}
}
