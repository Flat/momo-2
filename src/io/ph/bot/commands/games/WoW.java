package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadCharacterException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.model.games.WoWCharacter;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Process a wow character and display an embed with useful stats
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "wow",
		aliases = {},
		permission = Permission.NONE,
		description = "Look up a character in the World of Warcraft armory.\n"
				+ "Format is wow character-name region realm-name\n"
				+ "*character-name* - Name of your character"
				+ "*region* - Region in 2 letter format (i.e. NA or EU)\n"
				+ "*realm-name* - Name of your realm",
				example = "character-name na darkspear"
		)
public class WoW extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		if(split.length < 3) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		if(!split[1].equalsIgnoreCase("na") && !split[1].equalsIgnoreCase("eu") 
				&& !split[1].equalsIgnoreCase("tw")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Valid regions: NA, EU, TW");
			return;
		}
		String tempS = Util.combineStringArray(Util.removeFirstArrayEntry(split));
		final String serverName = Util.combineStringArray(Util.removeFirstArrayEntry(tempS.split(" ")));

		msg.getChannel()
		.sendMessage(new EmbedBuilder().setColor(Color.CYAN)
				.setDescription("Searching...").build())
		.queue(message -> {
			try {
				WoWCharacter wow = new WoWCharacter(serverName, split[0], split[1].toLowerCase());
				StringBuilder sb = new StringBuilder();
				em.setTitle(wow.getUsername() + " of " + wow.getRealm(), null)
				.setColor(Util.memberFromMessage(msg).getColor());
				sb.append("**Level " + wow.getLevel() + "** " + wow.getGender() + " "  
				+ wow.getRace() + " " + wow.getGameClass() +"\n");
				sb.append("**Item Level**: " + wow.getItemLevel() + "\n");
				if(wow.getGuild() != null)
					sb.append("**Guild**: " + wow.getGuild() + " | **Members**: " + wow.getGuildMembers()+"\n");
				sb.append("**LFR Kills**:        " + wow.getLfrKills() + "\n");
				sb.append("**Normal Kills**: " + wow.getNormalKills() + "\n");
				sb.append("**Heroic Kills**:   " + wow.getHeroicKills() + "\n");
				sb.append("**Mythic Kills**:  " + wow.getMythicKills() + "\n");

				em.setDescription(sb.toString())
				.setFooter("Achievement Points: " + wow.getAchievementPoints(), null)
				.setImage(wow.getThumbnail());
				msg.getChannel().sendMessage(em.build()).queue();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				em.setTitle("error", null)
				.setColor(Color.RED)
				.setDescription("Error finding your character.\n"
						+ GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix()
						+ "wow character-name region realm-name");
				msg.getChannel().sendMessage(em.build()).queue();
			} catch (IOException e) {
				e.printStackTrace();
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Blizzard server timed out. Please try again later");
				msg.getChannel().sendMessage(em.build()).queue();
			} catch (BadCharacterException e) {
				em.setTitle("error", null)
				.setColor(Color.RED)
				.setDescription("Error finding your character.\n"
						+ GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix()
						+ "wow character-name region realm-name");
				msg.getChannel().sendMessage(em.build()).queue();
			} catch (NoAPIKeyException e) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("This bot isn't setup to lookup WoW characters");
				msg.getChannel().sendMessage(em.build()).queue();		
			} finally {
				try {
					message.delete().queue();;
				} catch (Exception e2) { }
			}
		});

	}
}
