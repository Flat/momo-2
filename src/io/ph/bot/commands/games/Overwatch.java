package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandCategory;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.restwrappers.overwatch.OverwatchAPI;
import io.ph.restwrappers.overwatch.model.Competitive;
import io.ph.restwrappers.overwatch.model.OverwatchPlayer;
import io.ph.restwrappers.overwatch.model.Quickplay;
import io.ph.restwrappers.overwatch.model.Stats;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Overwatch user lookup
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "overwatch",
		aliases = {"ow"},
		category = CommandCategory.GAMES,
		permission = Permission.NONE,
		description = "Lookup an overwatch user. You must use a region code (NA, EU, or KR)"
				+ " and your FULL Battle.net username. This includes the #xxxx identifier."
				+ " Your username **is case sensitive**",
				example = "na UserName#1234"
		)
public class Overwatch extends Command {

	@Override
	public void executeCommand(Message msg) {
		if(msg.getContent().split(" ").length != 3 || !msg.getContent().contains("#")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		String user = Util.getCommandContents(contents);
		String username = user.split("#")[0];
		String discrim = user.split("#")[1];
		if (!Util.isInteger(discrim)) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(OverwatchAPI.ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		OverwatchAPI overwatchApi = retrofit.create(OverwatchAPI.class);
		try {
			Call<OverwatchPlayer> callPlayer = overwatchApi.getPlayer(username, Integer.parseInt(discrim));
			Response<OverwatchPlayer> res = callPlayer.execute();
			if (!res.isSuccessful()) {
				JsonObject jo = Json.parse(res.errorBody().string()).asObject();
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(StringUtils.capitalize(jo.get("msg").asString()));
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			OverwatchPlayer player = res.body();
			Stats stats;
			try {
				switch(contents.substring(0, contents.indexOf(' ')).toLowerCase()) {
				case "na":
				case "us":
				default:
					stats = player.getUs().getStats();
					break;
				case "eu":
					stats = player.getEu().getStats();
					break;
				case "kr":
					stats = player.getKr().getStats();
					break;
				}
			} catch (NullPointerException e) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Character not found for " + user
						+ ". Make sure your spelling **and** capitalization is correct,"
						+ " as well as your region code (na, eu, kr)");
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			Competitive c = stats.getCompetitive();

			String competitive = String.format("**Win %%**: %.2f (%d/%d)\n"
					+ "**Rank**: %s (%d)\n"
					+ "**Average K/D**: %.1f/%.1f", c.getOverallStats().getWinRate(),
					c.getOverallStats().getWins(), c.getOverallStats().getLosses(),
					StringUtils.capitalize(c.getOverallStats().getTier()), c.getOverallStats().getComprank(),
					c.getAverageStats().getFinalBlowsAvg(), c.getAverageStats().getDeathsAvg());

			Quickplay q = stats.getQuickplay();
			String quickplay = String.format("**Win %%**: %.2f (%d/%d)\n"
					+ "**Average K/D**: %.1f/%.1f", q.getOverallStats().getWinRate(),
					q.getOverallStats().getWins(), q.getOverallStats().getLosses(),
					q.getAverageStats().getFinalBlowsAvg(), q.getAverageStats().getDeathsAvg());

			em.setTitle(username, null)
			.setThumbnail(stats.getQuickplay().getOverallStats().getAvatar())
			.setColor(Util.resolveColor(msg.getMember(), Color.GREEN))
			.addField("Competitive", competitive, true)
			.addField("Quickplay", quickplay, true);
			msg.getChannel().sendMessage(em.build()).queue();
		} catch (IOException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Something went funny with the Overwatch servers");
			e.printStackTrace();
		}
	}


}
