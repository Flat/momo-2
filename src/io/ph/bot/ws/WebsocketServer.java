package io.ph.bot.ws;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketServer extends WebSocketServer {
	public static List<WebSocket> companionBots = new ArrayList<>();
	
	private static Logger log = LoggerFactory.getLogger(WebSocketServer.class);
	private static WebsocketServer instance;
	
	private static final int PORT = 14231;
	private WebsocketServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
	}

	private WebsocketServer(InetSocketAddress address) {
		super(address);
	}
	
	@Override
	public void onClose(WebSocket sock, int code, String reason, boolean remote) {
		log.warn("Client connection closed: code: {}\n\tRemote: {} Reason: {}", code, remote, reason);
		companionBots.remove(sock);
	}

	@Override
	public void onError(WebSocket sock, Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket sock, String msg) {
		log.info("Message received: {}", msg);
		IncomingOperations.parseMessage(sock, msg);
	}

	@Override
	public void onOpen(WebSocket sock, ClientHandshake handshake) {
		log.info("Socket opened: {}", sock.getRemoteSocketAddress().toString());
	}

	@Override
	public void onStart() {
		log.info("Websocket started");
	}
	
	public static WebsocketServer getInstance() {
		if (instance == null) {
			try {
				instance = new WebsocketServer(PORT);
			} catch (UnknownHostException e) {
				return null;
			}
		}
		return instance;
	}
}
