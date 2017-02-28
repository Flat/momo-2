package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * List joinable roles, order by members, and set the color to the most popular role's
 * @author p
 *
 */
@CommandData (
		defaultSyntax = "rolecount",
		aliases = {"rolelist", "rolestats"},
		permission = Permission.NONE,
		description = "Get meta information about your joinable roles",
		example = "(no parameters)"
		)
public class RoleCount extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if(g.getJoinableRoles().isEmpty()) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like your server doesn't have any joinable roles!");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		Supplier<Stream<String>> stream = () -> g.getJoinableRoles()
				.stream().sorted((a, b) -> {
					return Integer.compare(msg.getGuild().getMembersWithRoles(msg.getGuild().getRoleById(b)).size(),
							(msg.getGuild().getMembersWithRoles(msg.getGuild().getRoleById(a)).size()));
				});
		stream.get().forEach(s -> {
			sb.append(String.format("**%s** | %d\n", msg.getGuild().getRoleById(s).getName(), 
					msg.getGuild().getMembersWithRoles(msg.getGuild().getRoleById(s)).size()));
		});

		em.setTitle("Role ranking", null)
		.setColor(msg.getGuild().getRoleById(stream.get().findFirst().get()).getColor())
		.setDescription(sb.toString());
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
