package io.ph.bot.commands.administration;

import java.awt.Color;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
/**
 * Set a role to be auto assigned to new users when they join
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "autorole",
		aliases = {"setautorole"},
		category = CommandCategory.ADMINISTRATION,
		permission = Permission.MANAGE_SERVER,
		description = "Set a role to be auto assigned when a new user joins the guild. Role has to exist",
		example = "role-name"
		)
public class SetAutoRole extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		String roleName = Util.getCommandContents(msg);
		String currentRoleId = g.getConfig().getAutoAssignRoleId();
		EmbedBuilder em = new EmbedBuilder();
		if (roleName.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		
		List<Role> roles = msg.getGuild().getRolesByName(roleName, true);
		if (roles.isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(String.format("No role named %s found", roleName));
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Role r = roles.get(0);
		em.setTitle("Success", null);
		if (currentRoleId.equals(r.getId())) {
			em.setColor(Util.resolveColor(msg.getMember(), Color.CYAN))
			.setDescription(String.format("Removed **%s** as your auto assigned role", r.getName()));
			g.getConfig().setAutoAssignRoleId("");
		} else {
			em.setColor(Util.resolveColor(msg.getMember(), Color.CYAN))
			.setDescription(String.format("Set **%s** as your auto assigned role", r.getName()));
			g.getConfig().setAutoAssignRoleId(r.getId());
		}
		
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
