package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.time.Instant;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

/**
 * Kick a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "kick",
		aliases = {"k"},
		permission = Permission.KICK,
		description = "Kick a user",
		example = "target"
		)
public class Kick extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder().setTimestamp(Instant.now());
		if (Util.getCommandContents(msg).isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No target specified");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Member target = Util.resolveMemberFromMessage(msg);
		if (target == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No user found for **" + target + "**");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		msg.getGuild().getController().kick(target.getUser().getId()).queue(success -> {
			em.setTitle("Success", null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.setDescription(target.getEffectiveName() + " has been kicked");
			
		});

	}

}
