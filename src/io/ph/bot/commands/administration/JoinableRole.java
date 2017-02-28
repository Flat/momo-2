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
 * Create a role that a user can then join in. If it already exists, use that instead
 * Role can be created already. If not, this creates a blank role and adds it to the list
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "enablerole",
		aliases = {"joinablerole"},
		permission = Permission.MANAGE_ROLES,
		description = "Designate a joinable role\n"
				+ "Create or designate a pre-existing role as joinable. Users can then join it with the joinrole command",
				example = "role-to-join"
		)
public class JoinableRole extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String roleName = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		for(Role r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(roleName)) {
				if(GuildObject.guildMap.get(msg.getGuild().getId()).addJoinableRole(r.getId())) {
					em.setTitle("Success", null)
					.setColor(Color.GREEN)
					.setDescription("**" + roleName + "** is now joinable");
				} else {
					em.setTitle("Hmm...", null)
					.setColor(Color.CYAN)
					.setDescription("**" + roleName + "** is already joinable");
				}
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
		}
		msg.getGuild().getController().createRole().queue(role -> {
			role.getManager().setName(roleName).queue();
			GuildObject.guildMap.get(msg.getGuild().getId()).addJoinableRole(role.getId());
			em.setTitle("Created new role", null)
			.setColor(Color.GREEN)
			.setDescription("**" + roleName + "** is now joinable");
			msg.getChannel().sendMessage(em.build()).queue();
		});
	}

}
