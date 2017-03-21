package io.ph.bot.commands.administration;

import java.awt.Color;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Setup the DJ role
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "dj",
		aliases = {"djrole"},
		permission =  io.ph.bot.model.Permission.MANAGE_ROLES,
		description = "Create or specify a role to control music. If one is set, normal users "
				+ "need to have the role to control queues. Leave this command empty if you want "
				+ "to remove the restriction from your server",
		example = "role-name *(leave empty to remove restriction)*"
		)
public class DJ extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		String roleName = Util.getCommandContents(msg);
		
		if(roleName.isEmpty()) {
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("Removed the DJ role restriction from your server");
			g.getConfig().setDjRoleId("");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		List<Role> roleList;
		if (!(roleList = msg.getGuild().getRolesByName(roleName, true)).isEmpty()) {
			g.getConfig().setDjRoleId(roleList.get(0).getId());
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("Set the DJ role restriction for your "
					+ "server to pre-existing role " + roleName);
			msg.getChannel().sendMessage(em.build()).queue();
		} else {
			msg.getGuild().getController().createRole().queue(role -> {
				role.getManagerUpdatable()
				.getNameField().setValue(roleName).update().queue(success -> {
					g.getConfig().setDjRoleId(role.getId());
					em.setTitle("Success", null)
					.setColor(Color.GREEN)
					.setDescription("Set the DJ role restriction for "
							+ "your server to the new role " + roleName);
					msg.getChannel().sendMessage(em.build()).queue();
				}, failure -> {
					em.setTitle("Error", null)
					.setColor(Color.RED)
					.setDescription(failure.getMessage());
					msg.getChannel().sendMessage(em.build()).queue();
				});
			});
		}
	}
}
