package io.ph.bot.procedural;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Message;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class ProceduralListener {
	private static ProceduralListener instance;

	/**
	 * Observers are identified by the User ID & Channel ID that started
	 * Form: userID,channelID
	 */
	private Map<String, ProceduralCommand> observers = ExpiringMap.builder()
			.expiration(5, TimeUnit.MINUTES)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();

	public void addListener(Message msg, ProceduralCommand c) {
		observers.put(String.join(",", msg.getAuthor().getId(), msg.getChannel().getId()), c);
	}
	public void removeListener(Message msg) {
		observers.remove(String.join(",", msg.getAuthor().getId(), msg.getChannel().getId()));
	}

	public void update(Message msg) {
		if(observers.get(String.join(",", msg.getAuthor().getId(), msg.getChannel().getId())) != null)
			observers.get(String.join(",", msg.getAuthor().getId(), msg.getChannel().getId())).step(msg);
	}

	public static ProceduralListener getInstance() {
		if(instance == null)
			instance = new ProceduralListener();
		return instance;
	}
}
