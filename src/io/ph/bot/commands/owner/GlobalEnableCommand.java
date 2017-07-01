package io.ph.bot.commands.owner;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.exception.IllegalArgumentException;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Globally enable a command
 * @author Paul
 */
@CommandData (
		defaultSyntax = "globalenable",
		aliases = {},
		category = CommandCategory.BOT_OWNER,
		permission = Permission.BOT_OWNER,
		description = "",
		example = "macro"
		)
public class GlobalEnableCommand extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(content.equals("all")) {
			for (GuildObject g : GuildObject.guildMap.values()) {
				g.enableAllCommands();
			}
			em.setTitle("Success", null)
			.setColor(Color.GREEN)
			.setDescription("All commands have been enabled on all servers");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		try {
			for (GuildObject g : GuildObject.guildMap.values()) {
				g.enableCommand(content);
			}
			PropertiesConfiguration config = new PropertiesConfiguration("resources/guilds/template.properties");
			config.setAutoSave(false);
			
			List<String> enabled = config.getList("EnabledCommands").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
			List<String> disabled = config.getList("DisabledCommands").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
			String cmd = CommandHandler.aliasToDefaultMap.get(content);
			if (disabled.contains(cmd)) {
				disabled.remove(cmd);
				enabled.add(cmd);
				config.setProperty("EnabledCommands", enabled);
				config.setProperty("DisabledCommands", disabled);
				config.save();
			}
			em.setTitle("Success")
			.setColor(Util.resolveColor(msg.getMember(), Color.GREEN))
			.setDescription(String.format("**%s** has been enabled on all servers", content));
		} catch(IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("**" + content + "** is not a valid command.\n"
					+ "If you need valid options, do `commandstatus`");
		} catch (ConfigurationException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Error loading resources/guilds/template.properties");
			e.printStackTrace();
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
