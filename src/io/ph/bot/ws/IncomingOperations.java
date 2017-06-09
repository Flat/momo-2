package io.ph.bot.ws;

import org.java_websocket.WebSocket;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import io.ph.bot.Bot;
import io.ph.bot.commands.owner.Diagnostics;
import io.ph.bot.exception.NoAPIKeyException;

public class IncomingOperations {

	/**
	 * Parse an incoming message from a shard
	 * @param sock Originating WebSocket
	 * @param msg Message sent by socket
	 */
	public static void parseMessage(WebSocket sock, String msg) {
		JsonObject jo = Json.parse(msg).asObject();
		int op = jo.get("op").asInt();

		String key = jo.getString("key", "");
		try {
			if (!key.equals(Bot.getInstance().getApiKeys().get("musiccompanion"))) {
				return;
			}
		} catch (NoAPIKeyException e1) {
			e1.printStackTrace();
			return;
		}

		switch (op) {
		// Initial handshake, guilds, and supporters received
		case 1:
			WebsocketServer.companionBots.add(sock);
			OutgoingOperations.sendOp1(sock, jo);
			break;
		case 2:
			OutgoingOperations.sendOp2(sock, jo.getLong("guildId", 0));
			break;
		case 5:
			Diagnostics.currentMusic.put(jo.getInt("shardId", 0), jo.getInt("playingMusic", 0));
			break;
		default:
			return;
		}
	}	
}
