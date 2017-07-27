package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.MacroObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

/**
 * Basic, harmless stats
 * @author Paul
 */
@CommandData (
		defaultSyntax = "stats",
		aliases = {},
		category = CommandCategory.UTILITY,
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
		if (msg.getJDA().getShardInfo() != null) {
			em.addField("Shard ID", msg.getJDA().getShardInfo().getShardString(), true);
		}
		Object[] topMacro = null;
		if((topMacro = MacroObject.topMacro(msg.getGuild().getId())) != null) {
			Member m = msg.getGuild().getMemberById((String) topMacro[2]);
			String name = m == null ? (String) topMacro[3] : m.getEffectiveName();
			em.addField("Top macro", "**" + topMacro[1] + "** by **"
					+ name + "**: " + topMacro[0] + " hits", true);
		}
		em.setColor(Util.resolveColor(msg.getMember(), Color.CYAN))
		.setFooter("Bot version: " + Bot.BOT_VERSION, null);
		msg.getChannel().sendMessage(em.build()).queue();

	}

}
