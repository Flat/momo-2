package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

/**
 * Get basic information on a user
 * @author p
 *
 */
@CommandData (
		defaultSyntax = "userinfo",
		aliases = {"user"},
		permission = Permission.NONE,
		description = "Information on a user",
		example = "@target"
		)
public class UserInfo extends Command {

	@Override
	public void executeCommand(Message msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		Member target;
		if(contents.isEmpty())
			target = msg.getGuild().getMember(msg.getAuthor());
		else if((target = Util.resolveMemberFromMessage(msg)) == null) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("No user found for " + contents);
			msg.getChannel().sendMessage(em.build()).queue();
			return;
		}
		
		AtomicInteger mutualServers = new AtomicInteger();
		Bot.getInstance().getBots().stream()
		.forEach(j -> {
			mutualServers.addAndGet((int) j.getGuilds().stream()
			.filter(g -> g.getMemberById(target.getUser().getId()) != null)
			.count());
		});
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		em.setTitle("User info for " + target.getEffectiveName(), null)
		.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.MAGENTA))
		.addField("User", target.getUser().getName() + "#" + target.getUser().getDiscriminator(), true)
		.addField("Creation date", target.getUser().getCreationTime().format(formatter), true)
		.addField("Mutual servers", mutualServers + "", true)
		.addField("Server join date", target.getJoinDate().format(formatter), true)
		.addField("Roles", "`" + target.getRoles().stream()
				.map(Role::getName)
				.collect(Collectors.joining(", ")) + "`", true)
		.setThumbnail(target.getUser().getAvatarUrl());
		msg.getChannel().sendMessage(em.build()).queue();
	}


}
