package io.ph.bot.commands.fun;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;

/**
 * Make the bot say something
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "say",
		aliases = {"echo"},
		category = CommandCategory.FUN,
		permission = Permission.NONE,
		description = "Have the bot say something",
		example = "Hi, it's me!"
		)
public class Say extends Command {

	@Override
	public void executeCommand(Message msg) {
		msg.getChannel().sendMessage(Util.getCommandContents(msg).replaceAll("[<|@|>]", "")).queue(success -> {
			msg.delete().queue();
		});
	}
}
