package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Allow a user to join a role that is designated as joinable by administrators
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "joinrole",
		aliases = {"addto"},
		permission = Permission.NONE,
		description = "Assign yourself a designated joinable role",
		example = "role-name"
		)
public class JoinRole extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.getCommandContents(msg);
		if(role.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		for(Role r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role) 
					&& GuildObject.guildMap.get(msg.getGuild().getId()).isJoinableRole(r.getId())) {
				if (msg.getGuild().getMember(msg.getAuthor()).getRoles().contains(r)) {
					em.setTitle("Hmm...", null)
					.setColor(Color.CYAN)
					.setDescription("You're already in this role!");
					msg.getChannel().sendMessage(em.build()).queue();
					return;
				}
				if(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().isLimitToOneRole()) {
					Set<String> userRoles = msg.getGuild().getMember(msg.getAuthor()).getRoles()
							.stream()
							.map(Role::getId)
							.collect(Collectors.toSet());
					if(userRoles.stream()
							.filter(s -> GuildObject.guildMap.get(msg.getGuild().getId())
									.getJoinableRoles().contains(s))
							.count() > 0) {
						em.setTitle("Error", null)
						.setColor(Color.RED)
						.setDescription("You cannot join more than one role!");
						msg.getChannel().sendMessage(em.build()).queue();
						return;
					}
				}
				msg.getGuild().getController()
				.addRolesToMember(msg.getGuild().getMember(msg.getAuthor()), r).queue(
						success -> {
							em.setTitle("Success", null)
							.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
							.setDescription("You are now in the role **" + role + "**");
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
		.setDescription("That role doesn't exist or isn't joinable");
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
