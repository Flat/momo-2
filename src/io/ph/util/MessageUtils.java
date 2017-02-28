package io.ph.util;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class MessageUtils {
	/**
	 * Send a message embed to a channel
	 * @param channelId Channel ID
	 * @param e MessageEmbed
	 */
	public static void sendMessage(String channelId, MessageEmbed e) {
		Bot.getInstance().getBot().getTextChannelById(channelId).sendMessage(e).queue();
	}
	
	/**
	 * Send a message to a channel
	 * @param channelId Channel ID
	 * @param msg String of message
	 */
	public static void sendMessage(String channelId, String msg) {
		Bot.getInstance().getBot().getTextChannelById(channelId).sendMessage(msg).queue();		
	}
	
	/**
	 * Send a PM to a user
	 * @param userId User ID
	 * @param msg String of message
	 */
	public static void sendPrivateMessage(String userId, String msg) {
		Bot.getInstance().getBot().getUserById(userId).openPrivateChannel().queue(ch -> ch.sendMessage(msg).queue());
	}
	
	/**
	 * Send an error message to a channel when a command is used incorrectly
	 * @param msg Original message. This determines which channel to send to
	 * @param commandName Name of the command
	 * @param args Arguments for the command
	 * @param argExplanations Explanations for arguments or command. Each is delimited with a newline
	 * @deprecated
	 */
	@Deprecated
	public static void badCommandUsage(Message msg, String commandName, String args, String... argExplanations) {
		String prefix = GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle(String.format("%s usage", prefix + commandName), null)
		.setColor(Color.RED)
		.appendDescription(String.format("%s%s %s\n", prefix, commandName, args));
		
	}
	
	public static void sendIncorrectCommandUsage(Message msg, Command cmd) {
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Incorrect usage", null)
		.setColor(Color.RED)
		.setDescription(String.format("Incorrect command usage. For more info, use %shelp %s",
				GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix(), 
				cmd.getDefaultCommand()));
		msg.getChannel().sendMessage(em.build()).queue();
	}
}
