package io.ph.bot.commands.fun;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * osu! user lookup
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "kaga",
		aliases = {"sleep"},
		category = CommandCategory.FUN,
		permission = Permission.NONE,
		description = "It's time to sleep",
		example = ""
		)
public class Kaga extends Command {
	private static final String ALBUM_ID = "WtrQ0";
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		try {
			em.setImage(Util.getRandomImageLinkForImgurId(ALBUM_ID))
			.setColor(Util.resolveColor(msg.getMember(), null));
		} catch (NoAPIKeyException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No API key has been setup for Imgur");
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
