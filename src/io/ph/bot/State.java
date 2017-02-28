package io.ph.bot;

import java.io.File;
import java.io.IOException;

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
		Bot.getInstance().getBot().getPresence().setGame(Game.of(status));
	}
	public static void changeBotUsername(String newUser) {
		Bot.getInstance().getBot().getSelfUser().getManager().setName(newUser).queue();
	}
	public static void changeBotAvatar(File image) {
		try {
			Bot.getInstance().getBot().getSelfUser().getManager().setAvatar(Icon.from(image)).queue();
		} catch (IOException e) {
			LoggerFactory.getLogger(State.class).error("Error changing avatar");
		}
	}
	public static void changeBotPresence(OnlineStatus status) {
		Bot.getInstance().getBot().getPresence().setStatus(status);
	}
}
