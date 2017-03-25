package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadCharacterException;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.model.games.FFXIVCharacter;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Lookup a FFXIV character and display useful statistics given by its Lodestone
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "ffxiv",
		aliases = {"ff14"},
		permission = Permission.NONE,
		description = "Lookup a character in the Final Fantasy 14 Lodestone search.\n"
				+ "Format is ffxiv server first-name last-name\n"
				+ "*server* - Name of the server you are on\n"
				+ "*first-name* - First name of your character\n"
				+ "*last-name* - Last name of your character",
				example = "sargatanas first-name last-name"
		)
public class FFXIV extends Command {

	@Override
	public void executeCommand(Message msg) {
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		if(split.length != 3) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		msg.getChannel().sendMessage(new EmbedBuilder().setColor(Color.CYAN)
				.setDescription("Searching...").build())
		.queue(message -> {
			try {
				FFXIVCharacter xiv = new FFXIVCharacter(split[0], split[1], split[2]);
				em.setAuthor(xiv.getFirstName() + " " 
						+ xiv.getLastName() + " of " + xiv.getServer(), 
						xiv.getLodestoneLink(), null)
				.setColor(Util.memberFromMessage(msg).getColor())
				.setThumbnail(xiv.getJobImageLink());
				StringBuilder sb = new StringBuilder();
				sb.append("**" + xiv.getGender() + " " + xiv.getRace() + "** | **" + xiv.getFaction() + "**\n")
				.append("**Nameday**: " + xiv.getNameday() + " | **Guardian**: " + xiv.getGuardian() + "\n")
				.append("**Grand Company**: " + xiv.getGrandCompany() + "\n")
				.append("**Free Company**: " + xiv.getFreeCompany());
				em.setDescription(sb.toString())
				.setImage(xiv.getImageLink());

				msg.getChannel().sendMessage(em.build()).queue();
			} catch (IOException e) {
				e.printStackTrace();
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Square Enix server timed out. Please try again later");
				msg.getChannel().sendMessage(em.build()).queue();
			} catch (BadCharacterException e) {
				em.setTitle("error", null)
				.setColor(Color.RED)
				.setDescription("Error finding your character.\n"
						+ GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix()
						+ "ffxiv server-name first-name last-name");
				msg.getChannel().sendMessage(em.build()).queue();
			} finally {
				try {
					message.delete().queue();
				} catch (Exception e2) { }
			}
		});

	}
}
