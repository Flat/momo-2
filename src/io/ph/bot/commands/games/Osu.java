package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Permission;
import io.ph.restwrappers.osu.OsuAPI;
import io.ph.restwrappers.osu.OsuUser;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * osu! user lookup
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "osu",
		aliases = {"circles"},
		permission = Permission.NONE,
		description = "Lookup an osu! user",
		example = "username"
		)
public class Osu extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(OsuAPI.ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		OsuAPI osuApi = retrofit.create(OsuAPI.class);
		try {
			Call<List<OsuUser>> callUser = osuApi.getUser(contents, Bot.getInstance().getApiKeys().get("osu"));
			List<OsuUser> userList = callUser.execute().body();
			if(userList.isEmpty()) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(String.format("osu! user not found for **%s**", contents));
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			OsuUser user = userList.get(0);
			em.setAuthor("osu! results for " + user.getUsername(),
					"https://osu.ppy.sh/u/" + user.getUserId(), null)
			.setColor(Color.PINK)
			.addField("Ranking", NumberFormat.getInstance().format(Integer.parseInt(user.getPpRank())), true)
			.addField("PP", NumberFormat.getInstance().format((int) Double.parseDouble(user.getPpRaw())), true)
			.addField("Accuracy", (new DecimalFormat("#,###.00")).format(Double.parseDouble(user.getAccuracy())) + "%", true)
			.addField("Play Count", NumberFormat.getInstance().format(Integer.parseInt(user.getPlaycount())), true)
			.addField("SS Count", NumberFormat.getInstance().format(Integer.parseInt(user.getCountRankSs())), true)
			.addField("S Count", NumberFormat.getInstance().format(Integer.parseInt(user.getCountRankS())), true)
			.setThumbnail(String.format("https://a.ppy.sh/%s_1.jpg", user.getUserId()));
		} catch (NoAPIKeyException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Looks like this bot isn't setup to do osu! lookups");
			e.printStackTrace();
		} catch (IOException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Something went funny with the osu! servers");
			e.printStackTrace();
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}
	
}
