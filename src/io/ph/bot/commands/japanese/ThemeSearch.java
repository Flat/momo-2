package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.LoggerFactory;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.exception.NoSearchResultException;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.model.Theme;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Search for a theme from the Themes.moe api
 * @author Paul
 */
@CommandData (
		defaultSyntax = "theme",
		aliases = {"animetheme"},
		permission = Permission.NONE,
		description = "Search for an anime theme song off Themes.moe",
		example = "shinsekai yori"
		)
public class ThemeSearch extends Command implements Runnable {

	private Message msg;

	public ThemeSearch() { }

	private ThemeSearch(Message msg) {
		this.msg = msg;
	}
	@Override
	public void executeCommand(Message msg) {
		Runnable t = new ThemeSearch(msg);
		new Thread(t).start();
	}

	private void process(Message msg) {
		String search = Util.getCommandContents(msg);
		if(search.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		Message tempMessage = null;
		EmbedBuilder em = new EmbedBuilder();
		GuildObject g = GuildObject.guildMap.get(msg.getGuild().getId());
		if(Util.isInteger(search)) {
			Map<Integer, ArrayList<Theme>> historical 
			= g.getHistoricalSearches().getHistoricalThemeSearchResults();
			int given = Integer.parseInt(search);
			if((given) > historical.size() || given < 1) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Giving a number provides information "
						+ "on a previous search. This # is too large");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}

			StringBuilder sb = new StringBuilder();
			int i = 0;
			String title = null;
			for(Theme t : historical.get(given)) {
				sb.append("**" + (++i) + ") " + t.getType() + "** <" + t.getLink() + ">  \"" + t.getSongTitle() +  "\"\n");
				title = t.getAnimeTitle();
				g.getHistoricalSearches().addHistoricalMusic(i, new String[] {t.getSongTitle(), t.getLink()});
			}
			em.setTitle(title, null)
			.setColor(Color.CYAN)
			.setDescription(sb.toString());
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		try {
			tempMessage = msg.getChannel()
					.sendMessage(new EmbedBuilder()
							.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA))
							.setDescription("Searching...")
							.build()).complete();
			Map<String, ArrayList<Theme>> map = Theme.getThemeResults(search);
			if(map.size() == 1) {
				String key = null;
				for(Map.Entry<String, ArrayList<Theme>> entry : map.entrySet()) {
					key = entry.getKey();
					break;
				}
				em.setTitle(key, null)
				.setColor(Color.CYAN);
				StringBuilder sb = new StringBuilder();
				int i = 0;
				g.getHistoricalSearches().getHistoricalMusic().clear();
				for(Theme t : map.get(key)) {
					sb.append("**" + (++i) + ") " + t.getType() + "** <" + t.getLink() + ">  \"" + t.getSongTitle() +  "\"\n");
					g
					.getHistoricalSearches().addHistoricalMusic(i, new String[] {t.getSongTitle(), t.getLink()});
				}
				em.setFooter("Use "+ g.getConfig().getCommandPrefix() + "music # to play", null);
				em.setDescription(sb.toString());
			} else {
				em.setTitle("Multiple results found", null)
				.setColor(Color.WHITE);
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(Map.Entry<String, ArrayList<Theme>> entry : map.entrySet()) {
					g.getHistoricalSearches().getHistoricalThemeSearchResults().put(++i, entry.getValue());
					sb.append("**" + i + ")** " + entry.getKey() + "\n");
				}
				StringBuilder footer = new StringBuilder();
				footer.append("use " + g.getConfig().getCommandPrefix() + "theme # to search");

				em.setDescription(sb.toString());
				em.setFooter(footer.toString(), null);
			}

		} catch (NoSearchResultException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No search results for " + search)
			.setFooter("Note: Not all anime are indexed on Themes.moe", null);
			e.printStackTrace();
		} catch (IOException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Themes.moe may be having issues - please try again later");
			e.printStackTrace();
		} catch (NoAPIKeyException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like this bot doesn't have access to Themes.moe");
			LoggerFactory.getLogger(ThemeSearch.class).error("You do not have an API key for Themes.moe setup in Bot.properties");
		} finally {
			msg.getChannel().sendMessage(em.build()).queue();
			try {
				tempMessage.delete().queue();
			} catch(Exception e) {}
		}
	}
	@Override
	public void run() {
		process(this.msg);
	}

}
