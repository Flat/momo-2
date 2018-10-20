package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;

/**
 * Setup the muted role
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "setup",
		aliases = {"initialize"},
		category = CommandCategory.ADMINISTRATION,
		permission =  io.ph.bot.model.Permission.MANAGE_SERVER,
		description = "Perform initial setup by creating the muted role and initializing the guild",
		example = "(no parameters)"
		)
public class Setup extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		if(g.getConfig().getMutedRoleId() != null
				&& !g.getConfig().getMutedRoleId().isEmpty()) {
			if (msg.getGuild().getRoleById(g.getConfig().getMutedRoleId()) != null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Looks like I'm already setup here...");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
		}
		msg.getGuild().getController().createRole().queue(role -> {
			role.getManager()
			.setName("muted")
			.setMentionable(false)
			.setColor(new Color(217, 0, 90))
			.revokePermissions(Permission.MESSAGE_WRITE,
					Permission.VOICE_SPEAK, Permission.MESSAGE_ADD_REACTION)
			.queue(success -> {
				g.getConfig().setMutedRoleId(role.getId());
				for (Channel channel : msg.getGuild().getTextChannels()) {
					channel.createPermissionOverride(role).queue(or -> {
						or.getManager().deny(Permission.MESSAGE_WRITE, 
								Permission.VOICE_SPEAK, Permission.MESSAGE_ADD_REACTION).queue();
					}, failure -> {
						em.setTitle("Error", null)
						.setColor(Color.RED)
						.setDescription(failure.getMessage());
						msg.getChannel().sendMessage(em.build()).queue();
					});
				}
			});
		}, failure -> {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(failure.getMessage());
			msg.getChannel().sendMessage(em.build()).queue();
		});
		em.setTitle("Success", null)
		.setColor(Color.GREEN)
		.setDescription("Setup your muted role and saved configuration");
		msg.getChannel().sendMessage(em.build()).queue();


	}

}
