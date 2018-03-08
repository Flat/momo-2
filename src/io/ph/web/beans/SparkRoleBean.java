package io.ph.web.beans;

import java.util.List;
import java.util.stream.Collectors;

import io.ph.bot.Bot;
import net.dv8tion.jda.core.entities.Role;

public class SparkRoleBean {
	private String id;
	private String name;
	private int position;

	private SparkRoleBean(Role r) {
		this.id = r.getId();
		this.name = r.getName();
		this.position = r.getPosition();
	}
	public static List<SparkRoleBean> getRoles(String guildId) {
		return Bot.getInstance().shards.getGuildById(guildId).getRoles()
				.stream()
				.map(r -> new SparkRoleBean(r))
				.collect(Collectors.toList());
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
}
