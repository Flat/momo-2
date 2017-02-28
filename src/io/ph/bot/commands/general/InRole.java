package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.stream.Collectors;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Get a list of users in a role
 * @author Paul
 */
@CommandData (
		defaultSyntax = "inrole",
		aliases = {"role"},
		permission = Permission.NONE,
		description = "See what users are in a role",
		example = "role-name"
		)
public class InRole extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String roleName = Util.getCommandContents(msg).toLowerCase();
		Role role = null;
		for (Role r : msg.getGuild().getRoles()) {
			if (r.getName().toLowerCase().equals(roleName)) {
				role = r;
				break;
			}
		}
		if(role == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No users found for role " + roleName);
		} else {
			em.setTitle("Members with the role " + role.getName(), null)
			.setColor(Color.CYAN)
			.setDescription(msg.getGuild().getMembersWithRoles(role)
					.stream()
					.map(Member::getEffectiveName)
					.collect(Collectors.joining(", ")));
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
