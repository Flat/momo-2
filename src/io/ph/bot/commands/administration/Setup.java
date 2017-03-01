package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
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
		permission =  io.ph.bot.model.Permission.MANAGE_SERVER,
		description = "Perform initial setup by creating the muted role and initializing the guild",
		example = "(no parameters)"
		)
public class Setup extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		if((!g.getConfig().getMutedRoleId().equals("")
				|| g.getConfig().getMutedRoleId() != null)
				&& msg.getGuild().getRoleById(g.getConfig().getMutedRoleId()) != null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like I'm already setup here...");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		/*IRole mutedRole = null;
		RoleBuilder rb = new RoleBuilder(msg.getGuild());
		rb.setMentionable(false);
		rb.setColor(new Color(217, 0, 90));
		rb.withName("muted");
		rb.withPermissions(Permissions.getDeniedPermissionsForNumber(3212288));*/
		msg.getGuild().getController().createRole().queue(role -> {
			role.getManagerUpdatable()
			.getNameField().setValue("muted")
			.getMentionableField().setValue(false)
			.getColorField().setValue(new Color(217, 0, 90))
			.getPermissionField().revokePermissions(Permission.MESSAGE_WRITE, 
					Permission.VOICE_SPEAK, Permission.MESSAGE_ADD_REACTION)
			.update().queue(success -> {
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
