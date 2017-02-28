package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.util.ArrayList;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.JishoObject;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
/**
 * Search Jisho for a term, english or japanese
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "jisho",
		aliases = {"jisyo", "eewa", "waee", "nihongo"},
		permission = Permission.NONE,
		description = "Lookup Jisho.org for a Japanese term",
		example = "house"
		)
public class Jisho extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String word = Util.getCommandContents(msg);
		ArrayList<JishoObject> jA = JishoObject.searchVocabulary(word);
		if(jA == null || jA.size() == 0) {
			em.setTitle("No results found", null)
			.setColor(Color.RED)
			.setDescription("No results found for: " + word);
		} else {
			em.setTitle("Jisho results for " + word, "http://jisho.org/search/" + word)
			.setColor(Color.CYAN);
			JishoObject j = jA.get(0);
			//No guarantee this service will be up in the future, if someone is using this in like 2026
			em.setThumbnail("http://iriguchi.moe/includes/kanji.php?kanji=" + j.getKanji());
			em.addField("Reading", j.getKana(), true)
			.addField("Kanji", j.getKanji(), true)
			.addField("Definition(s)", j.getEnglishDefinitions(), false);
			
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

}
