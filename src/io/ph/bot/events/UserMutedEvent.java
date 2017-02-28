package io.ph.bot.events;

import java.awt.Color;
import java.time.Instant;

import io.ph.bot.model.GuildObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Event for when a user is muted
 * @author Paul
 *
 */
public class UserMutedEvent extends MomoEvent {
	private final User muter;
	private final User target;
	private final Guild guild;
	
	public UserMutedEvent(Guild guild, User muter, User target) {
		super(guild);
		this.muter = muter;
		this.target = target;
		this.guild = guild;
	}
	
	@Override
	public void handle() {
		GuildObject g = GuildObject.guildMap.get(guild.getId());
		TextChannel ch;
		if (!g.getSpecialChannels().getLog().isEmpty()
				&& (ch = guild.getTextChannelById(g.getSpecialChannels().getLog())) != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.setAuthor(target.getName() + " has been muted by " + muter.getName(), null, target.getAvatarUrl())
			.setColor(Color.RED)
			.setTimestamp(Instant.now());
			ch.sendMessage(em.build()).queue();
		}
	}
	
	/**
	 * Person that muted the target
	 * @return User of muter
	 */
	public User getMuter() {
		return this.muter;
	}
	/**
	 * Muted user
	 * @return User of target
	 */
	public User getTarget() {
		return this.target;
	}
	
	/**
	 * Guild this was performed in
	 * @return Guild
	 */
	@Override
	public Guild getGuild() {
		return this.guild;
	}
}
