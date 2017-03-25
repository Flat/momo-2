package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Leave role designated as joinable
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "leaverole",
		aliases = {},
		permission = Permission.NONE,
		description = "Leave a role that is designated as joinable",
		example = "role-name"
		)
public class LeaveRole extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		if(role.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		for(Role r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role)) {
				if(!GuildObject.guildMap.get(msg.getGuild().getId()).isJoinableRole(r.getId())) {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription("The role **" + role + "** is not a valid");
					return;
				}
				if (!msg.getGuild().getMember(msg.getAuthor()).getRoles().contains(r)) {
					em.setTitle("Hmm...", null)
					.setColor(Color.CYAN)
					.setDescription("Looks like you aren't in this role");
					msg.getChannel().sendMessage(em.build()).queue();
					return;
				}

				msg.getGuild().getController()
				.removeRolesFromMember(msg.getGuild().getMember(msg.getAuthor()), r).queue(
						success -> {
							em.setTitle("Success", null)
							.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
							.setDescription("You are now removed from the role **" + role + "**");
							msg.getChannel().sendMessage(em.build()).queue();
						},
						failure -> {
							em.setTitle("Error", null)
							.setColor(Color.RED)
							.setDescription(failure.getMessage());
							msg.getChannel().sendMessage(em.build()).queue();
						});
				return;
			}

		}
		em.setTitle("Error", null)
		.setColor(Color.RED)
		.setDescription("That role doesn't exist or isn't leaveable");
		msg.getChannel().sendMessage(em.build()).queue();

	}

}
