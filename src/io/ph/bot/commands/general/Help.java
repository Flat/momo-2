package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Get help with commands
 * @author Paul
 */
@CommandData (
		defaultSyntax = "help",
		aliases = {"commands"},
		permission = Permission.NONE,
		description = "Either list all commands or get help for one",
		example = "(optional command name)"
		)
public class Help extends Command {

	@Override
	public void executeCommand(Message msg) {
		String command = Util.getCommandContents(msg).toLowerCase();
		EmbedBuilder em = new EmbedBuilder();
		if(command.length() > 0) {
			//Help about a specific command
			Command c;
			if((c = CommandHandler.getCommand(command)) == null) {
				em.setTitle("Invalid command", null)
				.setColor(Color.RED)
				.setDescription(command + " is not a valid command");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			em.setTitle(command, null)
			.setColor(Color.CYAN)
			.addField("Primary Command", c.getDefaultCommand(), true);
			String[] aliases = c.getAliases();
			if(aliases.length > 0) {
				em.addField("Aliases", 
						Arrays.toString(aliases).substring(1, Arrays.toString(aliases).length() - 1) + "\n", true);
			}
			em.addField("Permissions", c.getPermission().toString(), true).addField("Description", c.getDescription(), false);
			// Pretty up multiple line examples
			String prefix = GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix();
			StringBuilder escapedPrefix = new StringBuilder();
			for (char cha : prefix.toCharArray()) {
				escapedPrefix.append("\\" + cha);
			}
			em.addField("Example", prefix + c.getDefaultCommand()
				+ " " + c.getExample().replaceAll("\n", "\n" + escapedPrefix.toString() + c.getDefaultCommand() + " "), false);
			msg.getChannel().sendMessage(em.build()).queue();
		} else {			
			List<Command> coll = (List<Command>) CommandHandler.getAllCommands();
			Collections.sort(coll, (f, s) -> {
				if(f.getPermission().compareTo(s.getPermission()) != 0)
					return f.getPermission().compareTo(s.getPermission());
				return f.getDefaultCommand().compareTo(s.getDefaultCommand());
			});
			StringBuilder sb = new StringBuilder();
			String prevPermissions = "";
			for(Command c : coll) {
				if(!prevPermissions.equals(c.getPermission().toString())) {
					if(prevPermissions.length() > 0)
						em.addField(prevPermissions, sb.toString(), false);
					sb.setLength(0);
					prevPermissions = c.getPermission().toString();
				}
				sb.append(c.getDefaultCommand() + "\n");
			}
			final String prev = prevPermissions;
			msg.getAuthor().openPrivateChannel().queue(success -> {
				em.setTitle("Command list", null)
				.setColor(Color.CYAN)
				.addField(prev, sb.toString(), false)
				.setFooter("PM me a command name to get more information", null);
				msg.getAuthor().getPrivateChannel().sendMessage(em.build()).queue(success1 -> {
					em.clearFields();
					em.setTitle("Success", null)
					.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
					.setDescription("Check your PMs!");
					msg.getChannel().sendMessage(em.build()).queue();
				});
			});
			
		}

	}

}
