package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.lang.WordUtils;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.rest.RESTCache;
import io.ph.restwrappers.anime.KitsuAPI;
import io.ph.restwrappers.anime.kitsu.KitsuAnime;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Search for an anime by name on Kitsu.io
 * Does not give multi results back like $mal does... But is overall, more effective
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "anime",
		aliases = {"kitsu"},
		permission = Permission.NONE,
		description = "Search for an anime from Kitsu.io\n",
		example = "shin sekai yori"
		)
public class KitsuAnimeSearch extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(KitsuAPI.ENDPOINT)
				.client(RESTCache.client)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		KitsuAPI api = retrofit.create(KitsuAPI.class);
		Call<KitsuAnime> call = api.getAnime(contents);
		try {
			KitsuAnime anime;
			if((anime = call.execute().body()) == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(String.format("No anime results found for **%s**", contents));
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}

			em.setTitle(anime.getData().get(0).getAttributes().getCanonicalTitle(),
					"https://kitsu.io/anime/" + anime.getData().get(0).getAttributes().getSlug())
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
			if(anime.getData().get(0).getAttributes().getCoverImage() != null)
				em.setImage(anime.getData().get(0).getAttributes().getCoverImage().getOriginal());
			em.addField("Type", WordUtils.capitalize(anime.getData().get(0).getType()), true);
			if(Util.isDouble(anime.getData().get(0).getAttributes().getAverageRating() + ""))
				em.addField("Rating", 
						(new DecimalFormat(".##").format(anime.getData().get(0).getAttributes().getAverageRating())) + "/5", true);
			em.addField("Episodes", anime.getData().get(0).getAttributes().getEpisodeCount() == null ? "not yet aired" : 
				anime.getData().get(0).getAttributes().getEpisodeCount() + "", true);
			StringBuilder aired = new StringBuilder();
			if(anime.getData().get(0).getAttributes().getStartDate() != null)
				aired.append(anime.getData().get(0).getAttributes().getStartDate());
			if(anime.getData().get(0).getAttributes().getEndDate() != null)
				aired.append(" -\n" + anime.getData().get(0).getAttributes().getEndDate());
			if(aired.length() > 0)
				em.addField("Airing Dates", aired.toString(), true);
			if(anime.getData().get(0).getAttributes().getSynopsis().length() > 800)
				anime.getData().get(0).getAttributes().setSynopsis(anime.getData().get(0).getAttributes().getSynopsis().substring(0, 700) + "...");
			em.addField("Synopsis", anime.getData().get(0).getAttributes().getSynopsis(), false);
			em.setFooter("information from kitsu.io", null);
			msg.getChannel().sendMessage(em.build()).queue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
