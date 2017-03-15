package io.ph.bot;

import java.io.File;

import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Icon;

/**
 * Various helper methods to change the state of the bot
 * @author Paul
 *
 */
public class State {
	public static void changeBotStatus(String status) {	
		Bot.getInstance().getBots().forEach(j -> j.getPresence().setGame(Game.of(status)));
	}
	public static void changeBotUsername(String newUser) {
		Bot.getInstance().getBots().forEach(j -> j.getSelfUser().getManager().setName(newUser).queue());
	}
	public static void changeBotAvatar(File image) {
		Bot.getInstance().getBots().forEach(j -> {
			try {
				j.getSelfUser().getManager().setAvatar(Icon.from(image)).queue();
			} catch (Exception e) {
				LoggerFactory.getLogger(State.class).error("Error changing avatar");
			}
		});

	}
	public static void changeBotPresence(OnlineStatus status) {
		Bot.getInstance().getBots().forEach(j -> j.getPresence().setStatus(status));
	}
}
