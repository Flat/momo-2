package io.ph.bot.commands.music;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.entities.Message;


@CommandData (
		defaultSyntax = "nowplaying",
		aliases = {"np", "now"},
		permission = Permission.NONE,
		description = "Tells you what song is currently playing",
		example = ""
		)
public class NowPlaying extends Command {

	@Override
	public void executeCommand(Message msg) {
		Music.now(msg);
	}
}
