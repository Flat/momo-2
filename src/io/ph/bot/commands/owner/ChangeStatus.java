package io.ph.bot.commands.owner;

import java.awt.Color;

import io.ph.bot.State;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

@CommandData (
		defaultSyntax = "changestatus",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Change bot's game status. This interrupts the set rotation.\n"
				+ "To resume rotation, use the command $changestatus reset",
		example = "New status"
		)
public class ChangeStatus extends Command {

	@Override
	public void executeCommand(Message msg) {
		if (!Util.getCommandContents(msg).equals("reset")) {
			StatusChangeJob.interrupt();
			State.changeBotStatus(Util.getCommandContents(msg));
		} else {
			StatusChangeJob.resume();
			EmbedBuilder em = new EmbedBuilder();
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription("Resuming status rotation");
			msg.getChannel().sendMessage(em.build()).queue();
		}
	}
}
