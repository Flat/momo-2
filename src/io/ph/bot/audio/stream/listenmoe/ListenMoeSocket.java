package io.ph.bot.audio.stream.listenmoe;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import io.ph.bot.Bot;
import io.ph.bot.audio.stream.StreamSource;
import io.ph.bot.model.GuildObject;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class ListenMoeSocket extends WebSocketClient {
	private static final Logger LOG = LoggerFactory.getLogger(ListenMoeSocket.class);
	private static final String LINK = "wss://listen.moe/api/v2/socket";

	private static ListenMoeSocket instance;


	private ListenMoeSocket(URI serverUri) {
		super(serverUri);
		try {
			setSocket(SSLSocketFactory.getDefault().createSocket(serverUri.getHost(), 443));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		LOG.info("Opened Listen.moe websocket");
	}

	@Override
	public void onMessage(String message) {
		if (GuildObject.streamingListenMoe == 0) {
			getInstance().close();
			return;
		}
		if (!message.isEmpty()) {
			JsonObject jo = Json.parse(message).asObject();
			if (jo.get("song_id") == null) // Simple check to verify valid msg
				return;
			ListenMoeData moe = ListenMoeData.getInstance();
			moe.setSongId(jo.getInt("song_id", 0));
			moe.setSongName(jo.getString("song_name", ""));
			moe.setArtist(jo.getString("artist_name", ""));
			moe.setListeners(jo.getInt("listeners", 0));
			moe.setAnimeName(jo.getString("anime_name", ""));
			moe.setRequester(jo.getString("requested_by", ""));

			// Here we can send out a message to music channels
			// when a new song comes on. Also leave empty channels
			EmbedBuilder em = new EmbedBuilder();
			em.setTitle("New Listen.moe track")
			.setColor(Color.CYAN)
			.addField("Title", moe.getSongName(), true)
			.addField("Artist", moe.getArtist(), true)
			.addField("Listeners", moe.getListeners() + "", true);
			GuildObject.guildMap.values().parallelStream()
			.filter(go -> go.getMusicManager() != null
				&& go.getMusicManager().getTrackManager().getCurrentSong() != null
				&& go.getMusicManager().getTrackManager().getCurrentSong()
				.getStreamSource().equals(StreamSource.LISTEN_MOE)
				&& Bot.getInstance().shards.getGuildById(go.getGuildId())
				.getAudioManager().isConnected())
			.forEach(go -> {
				AudioManager a = Bot.getInstance().shards.getGuildById(go.getGuildId())
						.getAudioManager();

				if (a.getConnectedChannel().getMembers().size() <= 1) {
					a.closeAudioConnection();
				} else if (go.getSpecialChannels().getMusic() != null) {
					TextChannel t = Bot.getInstance().shards
							.getTextChannelById(go.getSpecialChannels().getMusic());
					if (t != null) {
						t.sendMessage(em.build()).queue();
					}
				}

			});
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		LOG.error("Listen.moe socket closed: {}: {}", code, reason);
		LOG.info("Attempting reconnect to Listen.moe socket");
		Util.setTimeout(() -> retry(), 10 * 1000, true);
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
	}

	private static void retry() {
		instance = null;
		getInstance().connect();
	}

	/**
	 * Get the singleton instance of this websocket client
	 * @return ListenMoeSocket singleton
	 */
	public static ListenMoeSocket getInstance() {
		if (instance == null) {
			try {
				instance = new ListenMoeSocket(new URI(LINK));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
}