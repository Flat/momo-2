package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.GenericContainer;
import io.ph.bot.model.Permission;
import io.ph.rest.RESTCache;
import io.ph.restwrappers.saucenao.Result;
import io.ph.restwrappers.saucenao.SauceNaoAPI;
import io.ph.restwrappers.saucenao.SauceNaoResult;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Search SauceNao for either a given image URL or attachment
 * If no valid image is found, then go back up to 10 messages searching for one
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "source",
		aliases = {"sauce"},
		permission = Permission.NONE,
		description = "Check SauceNao for the source on an image\n"
				+ "Will first check your message for an image URL or attachment. "
				+ "If nothing is found, it will go back at most 10 messages searching for a valid iamge",
				example = "http://i.imgur.com/oRPyLuc.jpg"
		)
public class SourceSearch extends Command {
	private static final int SEARCH = 10;
	EmbedBuilder em;
	@Override
	public void executeCommand(Message msg) {
		URL url = resolveUrl(msg);
		em = new EmbedBuilder();
		if(url == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No image found in your message or the previous " + SEARCH + " messages");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Interceptor interceptor;
		Builder builder = new Retrofit.Builder()
				.baseUrl(SauceNaoAPI.ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create());
		if((interceptor = queryInterceptor()) != null) {
			builder.client((new OkHttpClient.Builder()).addInterceptor(interceptor).build());
		} else {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("This bot isn't setup to search SauceNao");
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		Retrofit rf = builder.build();

		SauceNaoAPI api = rf.create(SauceNaoAPI.class);
		Call<SauceNaoResult> sauceCall = api.getSauce(url);
		try {
			SauceNaoResult sauce = sauceCall.execute().body();
			if(sauce.getResults() == null) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(String.format("No results found on SauceNao for <%s>", url.toString()))
				.setThumbnail(url.toString());
				msg.getChannel().sendMessage(em.build()).queue();
				return;
			}
			resolveData(sauce);
		} catch (IOException e) {
			e.printStackTrace();
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

	private void resolveData(SauceNaoResult sauce) {
		Result image = sauce.getResults().get(0);
		if(image.getHeader().getThumbnail() != null)
			em.setThumbnail(image.getHeader().getThumbnail());
		em.setColor(Color.MAGENTA);
		em.addField("Similarity", String.format("%s%%", image.getHeader().getSimilarity()), true);
		switch(image.getHeader().getIndexId()) {
		case 5: // Pixiv
			em.setTitle(String.format("%s by %s", image.getData().getTitle(),
					image.getData().getMemberName()), null)
			.addField("Artist", String.format("http://www.pixiv.net/member.php?id=%d", 
					image.getData().getMemberId()), true)
			.addField("Original", String.format("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=%d",
					image.getData().getPixivId()), true);
			break;
		case 9: // Danbooru
		case 30: // I guess 30 is danbooru as well
			em.setTitle(String.format("Artist: %s", image.getData().getCreator()), null)
			.addField("Artist", image.getData().getCreator(), true)
			.addField("Original", String.format("https://danbooru.donmai.us/posts/%d",
					image.getData().getDanbooruId()), true);
			break;
		case 15: // Shutterstock lol
			em.setTitle("Source", null)
			.addField("Original", String.format("https://www.shutterstock.com/pic-%d/", 
					image.getData().getShutterstockId()), true);
			break;
		case 21: // Direct screenshot from an anime, anidb
			em.setTitle("Anime match: " + image.getData().getSource(), null)
			.addField("Episode", image.getData().getPart(), true)
			.addField("Anidb", String.format("https://anidb.net/perl-bin/animedb.pl?show=anime&aid=%d", 
					image.getData().getAnidbAid()), true);
			break;
		default:
			em.setTitle("Source unknown", null)
			.setColor(Color.RED).setDescription("Please report this to Kagumi with the URL of the image"
					+ ": Error for " + image.getHeader().getIndexName());
			break;
		}
	}
	/**
	 * Return interceptor that adds on these API keys - null if not
	 * @return Interceptor
	 * @throws NoAPIKeyException No API key, returns null
	 */
	private static Interceptor queryInterceptor() {
		return new Interceptor() {  
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request original = chain.request();
				HttpUrl originalHttpUrl = original.url();
				HttpUrl url;
				try {
					url = originalHttpUrl.newBuilder()
							.addQueryParameter("output_type", "2")
							.addQueryParameter("db", "999")
							.addQueryParameter("api_key", Bot.getInstance().getApiKeys().get("saucenao"))
							.addQueryParameter("numres", "1")
							.build();
				} catch (NoAPIKeyException e) {
					return null;
				}
				Request.Builder requestBuilder = original.newBuilder()
						.url(url);
				Request request = requestBuilder.build();
				return chain.proceed(request)
						.newBuilder()
						.header("Cache-Control", "public, max-age=" + RESTCache.CACHE_AGE)
						.build();
			}
		};
	}
	/**
	 * Resolve a URL from either current msg/attachments or past 5 msgs
	 * @param msg Message to check and index from
	 * @return URL if image, null if not
	 */
	private static URL resolveUrl(Message msg) {
		URL url;
		if(Util.getCommandContents(msg).isEmpty() && msg.getAttachments().isEmpty()) {
			//System.out.println("Checking for empty sauce");
			GenericContainer<URL> urlContainer = new GenericContainer<>();
			msg.getTextChannel().getHistoryAround(msg, 20)
			.queue(history -> {
				List<Message> list = history.getRetrievedHistory();
				for(int i = 0; i < SEARCH; i++) {
					try {
						URL localUrl;
						//System.out.println(list.get(i).getAttachments().isEmpty() + " | " + list.get(i).getContent());
						if(!list.get(i).getAttachments().isEmpty()) {
							if(checkMime((localUrl = new URL(list.get(i).getAttachments().get(0).getUrl()))) != null)
								urlContainer.setVal(localUrl);
						} else {
							if(checkMime((localUrl = new URL(list.get(i).getContent()))) != null)
								urlContainer.setVal(localUrl);
						}
					} catch (MalformedURLException e) {	}
				}
			});
			return urlContainer.getVal();
		} else {
			try {
				if(!msg.getAttachments().isEmpty()) {
					if(checkMime((url = new URL(msg.getAttachments().get(0).getUrl()))) != null)
						return url;
				} else {
					if(checkMime((url = new URL(Util.getCommandContents(msg)))) != null)
						return url;
				}
			} catch (MalformedURLException e) {	}
		}
		return null;
	}

	private static URL checkMime(URL url) {
		String mime = Util.getMimeFromUrl(url);
		if(mime != null && (mime.contains("jpeg") || mime.contains("png") || mime.contains("gif")))
			return url;
		return null;
	}
}
