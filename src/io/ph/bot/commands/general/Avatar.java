package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

/**
 * Get a user's avatar
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "avatar",
		aliases = {"ava", "avi"},
		permission = Permission.NONE,
		description = "Link your or another person's avatar",
		example = "(no parameters)"
		)
public class Avatar extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		Member target;
		if(contents.isEmpty()) {
			target = msg.getGuild().getMember(msg.getAuthor());
		} else if ((target = Util.resolveMemberFromMessage(msg)) == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No user found for " + contents);
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		msg.getChannel().sendMessage(target.getUser().getAvatarUrl() + "?size=256").queue();
	}

}
