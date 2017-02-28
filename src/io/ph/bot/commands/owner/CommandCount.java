package io.ph.bot.commands.owner;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.GenericContainer;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

@CommandData (
		defaultSyntax = "commandcount",
		aliases = { "count" },
		permission = Permission.BOT_OWNER,
		description = "Get command counts",
		example = ""
		)
public class CommandCount extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Command counts", null)
		.setColor(Color.CYAN);
		
		GenericContainer<Integer> i = new GenericContainer<>();
		i.setVal(0);
		CommandHandler.getAllCommands().stream()
		.sorted((c1, c2) -> {
			if (c1.getCommandCount() != c2.getCommandCount()) {
				return c2.getCommandCount() - c1.getCommandCount();
			}
			return c1.getDefaultCommand().compareTo(c2.getDefaultCommand());
		})
		.forEach(c -> {
			if (i.getVal() == 24) {
				msg.getChannel().sendMessage(em.build()).queue();
				em.clearFields();
				i.setVal(0);
			}
			i.setVal(i.getVal() + 1);
			em.addField(c.getDefaultCommand(), c.getCommandCount() + "", true);
		});
		msg.getChannel().sendMessage(em.build()).queue();
		
	}

}
