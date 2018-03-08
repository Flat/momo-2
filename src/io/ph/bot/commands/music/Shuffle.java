package io.ph.bot.commands.music;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;


@CommandData (
		defaultSyntax = "shuffle",
		aliases = {},
		category = CommandCategory.MUSIC,
		permission = Permission.KICK,
		description = "Shuffle the current playlist",
		example = ""
		)
public class Shuffle extends Command {

	@Override
	public void executeCommand(Message msg) {
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		boolean djSet = !g.getConfig().getDjRoleId().isEmpty();
		if ((djSet && !msg.getGuild().getMember(msg.getAuthor())
				.getRoles().contains(msg.getGuild().getRoleById(g.getConfig().getDjRoleId())))
				&& !Util.memberHasPermission(msg.getGuild().getMember(msg.getAuthor()), Permission.KICK)) {
			return;
		}
		Music.shuffle(msg, djSet);
	}
}
