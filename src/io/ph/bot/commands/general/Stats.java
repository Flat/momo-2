package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.MacroObject;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Message;

/**
 * Basic, harmless stats
 * @author Paul
 */
@CommandData (
		defaultSyntax = "stats",
		aliases = {},
		permission = Permission.NONE,
		description = "Display stats for the server",
		example = "(no parameters)"
		)
public class Stats extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		int onlineUsers = (int) msg.getGuild().getMembers().stream()
				.filter(m -> !m.getOnlineStatus().equals(OnlineStatus.OFFLINE))
				.count();
		
		em.setTitle(msg.getGuild().getName(), null)
		.addField("Users", onlineUsers + "/" + msg.getGuild().getMembers().size(), true)
		.addField("Text Channels", msg.getGuild().getTextChannels().size() + "", true)
		.addField("Voice Channels", msg.getGuild().getVoiceChannels().size() + "", true)
		.addField("Owner", msg.getGuild().getOwner().getEffectiveName(), true)
		.addField("Creation Date", msg.getGuild().getCreationTime().format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.toString(), true)
		.addField("Server ID", msg.getGuild().getId(), true);
		Object[] topMacro = null;
		if((topMacro = MacroObject.topMacro(msg.getGuild().getId())) != null)
				em.addField("Top macro", "**" + topMacro[1] + "** by **"
						+ msg.getGuild().getMemberById((String) topMacro[2]).getEffectiveName() 
						+ "**: " + topMacro[0] + " hits", false);
		em.setColor(Color.CYAN)
		.setFooter("Bot version: " + Bot.BOT_VERSION, null);
		msg.getChannel().sendMessage(em.build()).queue();

	}

}
