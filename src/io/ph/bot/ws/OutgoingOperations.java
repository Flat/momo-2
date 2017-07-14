package io.ph.bot.ws;

import org.java_websocket.WebSocket;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.model.GuildObject;

/**
 * Outgoing operations that originate from the client.
 * 
 * For more information, see Opcode Reference.md
 * @author Paul
 *
 */
public class OutgoingOperations {


	/**
	 * Send total guild info based on received info from op1
	 * @param sock Socket to send to
	 * @param jo Incoming JsonObject payload
	 * 
	 * TODO: Differing for live w/ supporters
	 */
	protected static void sendOp1(WebSocket sock, JsonObject jo) {
		JsonObject ret = new JsonObject();
		ret.add("op", 1);
		JsonArray requested = jo.get("guildIds").asArray();
		JsonArray retGuilds = new JsonArray();
		for (JsonValue v : requested) {
			long l = v.asLong();
			if (Bot.getInstance().shards.getGuildById(l) == null) {
				continue;
			}
			JsonObject toAdd = new JsonObject();
			GuildObject go = GuildObject.guildMap.get(l + "");
			toAdd.add("guildId", l);
			toAdd.add("prefix", go.getConfig().getCommandPrefix());

			String dj = go.getConfig().getDjRoleId();
			long djL = 0;
			if (!dj.isEmpty()) {
				djL = Long.parseLong(dj);
			}
			toAdd.add("djRoleId", djL);

			String voice = go.getSpecialChannels().getMusicVoice();
			long voiceL = 0;
			if (!voice.isEmpty()) {
				voiceL = Long.parseLong(voice);
			}
			toAdd.add("voiceChannelId", voiceL);

			String music = go.getSpecialChannels().getMusic();
			long musicL = 0;
			if (!music.isEmpty()) {
				musicL = Long.parseLong(music);
			}
			toAdd.add("musicChannelId", musicL);

			retGuilds.add(toAdd);

		}

		ret.add("guilds", retGuilds);
		sock.send(ret.toString());
	}

	/**
	 * Send operation 2, guild info to a socket
	 * @param sock Socket to send through
	 * @param guildId Guild ID information
	 */
	public static void sendOp2(WebSocket sock, long guildId) {
		if (Bot.getInstance().shards.getGuildById(guildId) == null) {
			return;
		}
		JsonObject ret = new JsonObject();
		ret.add("op", 2);
		ret.add("guildId", guildId);
		GuildObject go = GuildObject.guildMap.get(guildId + "");

		ret.add("prefix", go.getConfig().getCommandPrefix());
		String dj = go.getConfig().getDjRoleId();
		long djL = 0;
		if (!dj.isEmpty()) {
			djL = Long.parseLong(dj);
		}
		ret.add("djRoleId", djL);

		String voice = go.getSpecialChannels().getMusicVoice();
		long voiceL = 0;
		if (!voice.isEmpty()) {
			voiceL = Long.parseLong(voice);
		}
		ret.add("voiceChannelId", voiceL);

		String music = go.getSpecialChannels().getMusic();
		long musicL = 0;
		if (!music.isEmpty()) {
			musicL = Long.parseLong(music);
		}
		ret.add("musicChannelId", musicL);

		if (sock == null) {
			for (WebSocket soc : WebsocketServer.companionBots) {
				soc.send(ret.toString());
			}
		} else {
			sock.send(ret.toString());
		}
	}
	
	/**
	 * Send op2, guild information, to all sockets
	 * @param guildId Guild ID
	 */
	public static void sendOp2(long guildId) {
		sendOp2(null, guildId);
	}

	/**
	 * Send opcode 3, guild changed command status, to all sockets
	 * @param guildId Guild ID
	 * @param enable Enabled or not
	 */
	public static void sendOp3(long guildId, boolean enable) {
		JsonObject ret = new JsonObject();
		ret.add("op", 3);
		ret.add("guildId", guildId);
		ret.add("enable", enable);
		for (WebSocket sock : WebsocketServer.companionBots) {
			sock.send(ret.toString());
		}
	}

}
