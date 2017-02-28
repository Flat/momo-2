package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.rest.RESTCache;
import io.ph.restwrappers.pokemon.PokemonAPI;
import io.ph.restwrappers.pokemon.model.Pokemon;
import io.ph.restwrappers.pokemon.model.Type;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Search Pokemon by name or ID
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "pokemon",
		aliases = {"poke"},
		permission = Permission.NONE,
		description = "Lookup a Pokemon by name or ID. Gen 7 is not supported yet",
		example = "meloetta-aria"
		)
public class PokemonSearch extends Command {
	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg).toLowerCase();
		if(contents.split(" ")[0].equals("mega")) {
			contents = Util.getCommandContents(contents);
			contents += "-mega";
		}
		if(contents.isEmpty()) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}

		Retrofit rf = new Retrofit.Builder()
				.baseUrl(PokemonAPI.ENDPOINT)
				.client(RESTCache.client)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		PokemonAPI api = rf.create(PokemonAPI.class);
		Call<Pokemon> pokemonCall = api.getPokemon(contents);
		try {
			Pokemon pokemon;
			if((pokemon = pokemonCall.execute().body()) == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(String.format("Pokemon not found for **%s**", contents));
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			em.setTitle(StringUtils.capitalize(pokemon.getName()), null)
			.setThumbnail(pokemon.getSprites().getFrontDefault())
			.addField("National Dex", resolveId(pokemon.getSpecies().getUrl()), true);
			if(pokemon.getGameIndices().size() > 0)
				em.addField("First seen", resolveGeneration((Lists.reverse(pokemon.getGameIndices())
						.get(0).getVersion().getName().replaceAll("-", " "))), true);
			em.addField("Abilities", Lists.reverse(pokemon.getAbilities()).stream()
					.map(a -> a.toString() + (a.getIsHidden() ? " (H)" : "")).collect(Collectors.joining(", ")), true)
			.addField("Typing", Joiner.on(" & ").join(Lists.reverse(pokemon.getTypes())), true)
			.setFooter("Stat total: " + pokemon.getStats().stream()
					.mapToInt(type -> type.getBaseStat())
					.sum() + " | "
					+ Lists.reverse(pokemon.getStats()).stream()
					.map(s -> s.getBaseStat().toString())
					.collect(Collectors.joining("/")), null)
			.setColor(getColor(Lists.reverse(pokemon.getTypes()).get(0)));
		} catch (IOException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Something went funny connecting");
			e.printStackTrace();
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

	/**
	 * Because certain forms have odd IDs, this'll resolve the pokemon ID based on their Species redirect
	 * @param url Species URL
	 * @return Pokemon ID
	 */
	private static String resolveId(String url) {
		url = url.substring(0, url.length() - 1);
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private static String resolveGeneration(String given) {
		switch(given) {
		case "red":
		case "yellow":
		case "firered":
		case "leafgreen":
			return "R/B";

		case "gold":
		case "silver":
		case "crystal":
		case "heartgold":
		case "soulsilver":
			return "G/S/C";

		case "ruby":
		case "sapphire":
		case "emerald":
		case "omega ruby":
		case "alpha sapphire":
			return "R/S/E";

		case "diamond":
		case "pearl":
		case "platinum":
			return "D/P/Pt";

		case "black":
		case "white":
		case "black 2":
		case "white 2":
			return "B/W";

		case "x":
		case "y":
			return "X/Y";

		default:
			return StringUtils.capitalize(given);
		}
	}
	private static Color getColor(Type type) {
		switch(type.getType().getName()) {
		case "normal":
			return Color.decode("#A8A77A");
		case "fighting":
			return Color.decode("#C22E28");
		case "flying":
			return Color.decode("#A98FF3");
		case "poison":
			return Color.decode("#A33EA1");
		case "ground":
			return Color.decode("#E2BF65");
		case "rock":
			return Color.decode("#B6A136");
		case "bug":
			return Color.decode("#A6B91A");
		case "ghost":
			return Color.decode("#735797");
		case "steel":
			return Color.decode("#B7B7CE");
		case "fire":
			return Color.decode("#EE8130");
		case "water":
			return Color.decode("#6390F0");
		case "grass":
			return Color.decode("#7AC74C");
		case "electric":
			return Color.decode("#F7D02C");
		case "psychic":
			return Color.decode("#F95587");
		case "ice":
			return Color.decode("#96D9D6");
		case "dragon":
			return Color.decode("#6F35FC");
		case "dark":
			return Color.decode("#705746");
		case "fairy":
			return Color.decode("#D685AD");
		default: 
			return Color.BLACK;
		}
	}
}
