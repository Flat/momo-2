package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.Random;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Magic eight ball for a response
 * @author Paul
 */
@CommandData (
		defaultSyntax = "eightball",
		aliases = {"magicball"},
		permission = Permission.NONE,
		description = "Ask the magic eight ball a question",
		example = "Is this thing rigged?"
		)
public class EightBall extends Command {
	String[] responses = new String[]
			{
				"It is certain",
				"It is decidedly so",
				"Without a doubt",
				"Yes, definitely",
				"You may rely on it",
				"As I see it, yes",
				"Most likely",
				"Outlook good",
				"Yes",
				"Signs point to yes",
				
				"Reply hazy, try again",
				"Ask again later",
				"Better not tell you now",
				"Cannot predict now",
				"Concentrate and ask again",
				
				"Don't count on it",
				"My reply is no",
				"My sources say no",
				"Outlook not so good",
				"Very doubtful"
			};

	@Override
	public void executeCommand(Message msg) {
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		int r = new Random().nextInt(responses.length);
		em.setTitle(content, null)
		.setDescription(responses[r]);
		Color c;
		if(r < 10)
			c = Color.GREEN;
		else if(r >= 10 && r <= 14)
			c = Color.CYAN;
		else
			c = Color.RED;
		em.setColor(c);
		msg.getChannel().sendMessage(em.build()).queue();
		
	}
}

