package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Remove role from joinable role list for server
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "disablerole",
		aliases = {"removejoinablerole"},
		permission = Permission.MANAGE_ROLES,
		description = "Disable a joinable role.\n"
				+ "Designate a role that cannot be joined. Note: This will not remove users who are already in that role, "
				+ "and they cannot leave the role of their own volition",
		example = "role-to-remove"
		)
public class RemoveOptInRole extends Command {
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		em.setTitle("Error", null)
		.setColor(Color.RED)
		.setDescription("**" + role + " is not a joinable role");
		for(Role r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role)
					&& GuildObject.guildMap.get(msg.getGuild().getId()).removeJoinableRole(r.getId())) {
				em.setTitle("Success", null)
				.setColor(Color.GREEN)
				.setDescription("**" + role + "** is not joinable anymore");
				break;
			}
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
