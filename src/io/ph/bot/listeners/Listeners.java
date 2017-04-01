package io.ph.bot.listeners;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * General purpose listeners for events
 * @author Paul
 *
 */
public class Listeners extends ListenerAdapter {
	private static Logger log = LoggerFactory.getLogger(Listeners.class);

	@Override
	public void onReady(ReadyEvent e) {
		e.getJDA().getGuilds().stream()
		.forEach(g -> {
			checkFiles(g);
			GuildObject.guildMap.put(g.getId(), new GuildObject(g));
			startupChecks(g);
		});
		if (e.getJDA().getShardInfo() != null) {
			log.info("Bot is now logged on shard {}: {} guilds", e.getJDA().getShardInfo().getShardId(),
					e.getJDA().getGuilds().size());
		} else {
			log.info("Bot is now logged: {} guilds", e.getJDA().getGuilds().size());
		}
	}
	
	/**
	 * This provides startup checks for guilds and config.
	 * For example, a guild deletes a joinable role while the bot was offline
	 * @param guild Guild to check
	 */
	private static void startupChecks(Guild guild) {
		GuildObject g = GuildObject.guildMap.get(guild.getId());
		
		// Joinable roles
		for (String id : g.getJoinableRoles()) {
			if (guild.getRoleById(id) == null) {
				g.removeJoinableRole(id);
			}
		}
		
	}

	@Override 
	public void onGuildJoin(GuildJoinEvent e) {
		if (!e.getGuild().getSelfMember().hasPermission(net.dv8tion.jda.core.Permission.MESSAGE_WRITE)) {
			e.getGuild().leave().queue();
			return;
		}
		checkFiles(e.getGuild());
		GuildObject g = new GuildObject(e.getGuild());
		GuildObject.guildMap.put(e.getGuild().getId(), g);
		log.info("Guild joined: {} on shard: {}", e.getGuild().getName(), e.getJDA().getShardInfo().getShardId());
		if (g.getConfig().isFirstTime()) {
			AtomicInteger guildCount = new AtomicInteger();
			Bot.getInstance().getBots().stream()
			.forEach(j -> guildCount.addAndGet(j.getGuilds().size()));
			e.getGuild().getPublicChannel().sendMessage("Hi, I'm Momo! You are my "
					+ Util.ordinal(guildCount.get()) + " server.\n"
					+ "If you want a list of commands, use `$help`. If you want some tutorials on my features, "
					+ "do `$howto` - I suggest doing `$howto setup` immediately.\n"
					+ "I also feature a web dashboard for my configuration! "
					+ "Access it here: <https://momobot.io/dash>").queue(success -> {
						g.getConfig().setFirstTime(false);
					}, failure -> {
						g.getConfig().setFirstTime(false);
					});
			if (!e.getGuild().getSelfMember().hasPermission(net.dv8tion.jda.core.Permission.MESSAGE_EMBED_LINKS)) {
				e.getGuild().getPublicChannel().sendMessage("I require permissions to Embed Links for the"
						+ " vast majority of my functionality. Please enable it!").queue();
			}
		}
	}

	@Override
	public void onGuildAvailable(GuildAvailableEvent e) {
		checkFiles(e.getGuild());
		log.info("Guild available: {}", e.getGuild().getName());
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent e) {
		if (!Bot.isReady)
			return;
		try {
			FileUtils.deleteDirectory(new File("resources/guilds/" + e.getGuild().getId() + "/"));
			GuildObject.guildMap.remove(e.getGuild().getId());
			log.info("Guild has left: {}", e.getGuild().getName());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		if (!Bot.isReady)
			return;
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		if (!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().setAuthor(e.getMember().getUser().getName() + " has joined the server", 
					null, e.getMember().getUser().getAvatarUrl())
					.setColor(Color.GREEN)
					.setTimestamp(Instant.now());
			MessageUtils.sendMessage(g.getSpecialChannels().getLog(), em.build());
		}
		if ((!g.getSpecialChannels().getWelcome().equals("") || g.getConfig().isPmWelcomeMessage())
				&& !g.getConfig().getWelcomeMessage().isEmpty()) {
			String msg = g.getConfig().getWelcomeMessage();
			msg = msg.replaceAll("\\$user\\$", e.getMember().getAsMention());
			msg = msg.replaceAll("\\$server\\$", e.getGuild().getName());
			if (!g.getConfig().isPmWelcomeMessage())
				MessageUtils.sendMessage(g.getSpecialChannels().getWelcome(), msg);
			else
				MessageUtils.sendPrivateMessage(e.getMember().getUser().getId(), msg);
		}
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
		if (!Bot.isReady)
			return;
		if (e.getMember().getUser().getId().equals(e.getJDA().getSelfUser().getId()))
			return;
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		if (!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().setAuthor(e.getMember().getUser().getName() + " has left the server",
					null, e.getMember().getUser().getAvatarUrl())
					.setColor(Color.RED)
					.setTimestamp(Instant.now());
			MessageUtils.sendMessage(g.getSpecialChannels().getLog(), em.build());
		}
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent e) {
		if (!Bot.isReady)
			return;
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		EmbedBuilder em = new EmbedBuilder();
		em.setColor(Color.CYAN)
		.setTimestamp(Instant.now());

		if (!g.getSpecialChannels().getLog().equals("")) {
			if (e.getPrevNick() != null && e.getNewNick() != null) {
				em.setDescription("**" + e.getPrevNick() + "** to **" + e.getNewNick() + "**");
				em.setAuthor(e.getMember().getUser().getName() + " changed their nickname",
						null, e.getMember().getUser().getAvatarUrl());
			} else if (e.getPrevNick() != null && e.getNewNick() == null) {
				em.setDescription("**" + e.getPrevNick() + "** to **" + e.getMember().getUser().getName() + "**");
				em.setAuthor(e.getMember().getUser().getName() + " removed their nickname",
						null, e.getMember().getUser().getAvatarUrl());
			} else {
				em.setDescription("**" + e.getMember().getUser().getName() + "** to **" + e.getNewNick() + "**");
				em.setAuthor(e.getMember().getUser().getName() + " added a nickname", null, 
						e.getMember().getUser().getAvatarUrl());
			}
			MessageUtils.sendMessage(g.getSpecialChannels().getLog(), em.build());
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (!Bot.isReady)
			return;
		//log.info("{}: {}", e.getAuthor().getName(), e.getMessage().getContent());
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());

		// Delete invites
		if (g.getConfig().isDisableInvites()
				&& !Util.memberHasPermission(e.getMember(), Permission.KICK)) {
			if (e.getMessage().getContent().toLowerCase().contains("discord.gg/")) {
				e.getMessage().delete().queue(success -> {
					e.getChannel().sendMessage("No advertising, " + e.getAuthor().getAsMention()).queue();
				});
			}
		}
		// Bot check
		if (e.getAuthor().isBot())
			return;
		// Requesting prefix
		if (!e.getMessage().mentionsEveryone()
				&& e.getMessage().isMentioned(e.getJDA().getSelfUser())) {
			if (e.getMessage().getContent().contains("prefix")) {
				e.getAuthor().openPrivateChannel().queue(ch -> {
					ch.sendMessage(GuildObject
							.guildMap.get(e.getGuild().getId()).getConfig().getCommandPrefix()).queue();
				});
				return;
			}
		}
		// Jump to command
		if (e.getMessage().getContent().startsWith(g.getConfig().getCommandPrefix())) {
			if (!e.getChannel().canTalk()) {
				return;
			}
			CommandHandler.processCommand(e.getMessage());
			return;
		}
		// Slow mode
		if (g.getConfig().getMessagesPerFifteen() > 0
				&& !Util.memberHasPermission(e.getMember(), Permission.KICK)) {
			Integer counter;
			if ((counter = g.getUserTimerMap().get(e.getAuthor().getId())) == null) {
				counter = 0;
			}
			if (++counter > g.getConfig().getMessagesPerFifteen()) {
				e.getMessage().delete().queue(success -> {
					EmbedBuilder em = new EmbedBuilder();
					em.setColor(Color.RED)
					.setTitle("Error", null)
					.setDescription("Whoa, slow down there! You're sending too many messages");
					e.getAuthor().openPrivateChannel().queue(ch -> {
						ch.sendMessage(em.build()).queue();
					});
				});
			} else {
				g.getUserTimerMap().put(e.getAuthor().getId(), counter);
			}
		}
		ProceduralListener.getInstance().update(e.getMessage());
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
		if (!Bot.isReady)
			return;
		if (e.getAuthor().equals(e.getJDA().getSelfUser())) {
			return;
		}

		EmbedBuilder em = new EmbedBuilder();
		Command c;
		if((c = CommandHandler.getCommand(e.getMessage().getContent().toLowerCase())) == null) {
			em.setTitle("Invalid command", null)
			.setColor(Color.RED)
			.setDescription(e.getMessage().getContent() + " is not a valid command");
			e.getChannel().sendMessage(em.build()).queue();
			return;
		}
		em.setTitle(e.getMessage().getContent(), null)
		.setColor(Color.CYAN)
		.addField("Primary Command", c.getDefaultCommand(), true);
		String[] aliases = c.getAliases();
		if(aliases.length > 0) {
			em.addField("Aliases", 
					Arrays.toString(aliases).substring(1, Arrays.toString(aliases).length() - 1) + "\n", true);
		}
		em.addField("Permissions", c.getPermission().toString(), true)
		.addField("Description", c.getDescription(), false)
		.addField("Example", c.getDefaultCommand() + " " 
				+ c.getExample().replaceAll("\n", "\n" + c.getDefaultCommand() + " "), false);
		e.getChannel().sendMessage(em.build()).queue();
		return;
	}

	@Override
	public void onTextChannelCreate(TextChannelCreateEvent e) {
		if (!Bot.isReady)
			return;
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		Role r;
		if (!g.getConfig().getMutedRoleId().isEmpty() 
				&& (r = e.getGuild().getRoleById(g.getConfig().getMutedRoleId())) != null) {
			e.getChannel().createPermissionOverride(r).queue(or -> {
				or.getManager().deny(net.dv8tion.jda.core.Permission.MESSAGE_WRITE, 
						net.dv8tion.jda.core.Permission.MESSAGE_ADD_REACTION).queue();
			});
		}
	}

	@Override
	public void onVoiceChannelCreate(VoiceChannelCreateEvent e) {
		if (!Bot.isReady)
			return;
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		Role r;
		if (!g.getConfig().getMutedRoleId().isEmpty() 
				&& (r = e.getGuild().getRoleById(g.getConfig().getMutedRoleId())) != null) {
			e.getChannel().createPermissionOverride(r).queue(or -> {
				or.getManager().deny(net.dv8tion.jda.core.Permission.VOICE_SPEAK).queue();
			});
		}
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent e) {
		GuildObject g = GuildObject.guildMap.get(e.getGuild().getId());
		if (g.isJoinableRole(e.getRole().getId())) {
			g.removeJoinableRole(e.getRole().getId());
		}
	}

	private static void checkFiles(Guild g) {
		File f;
		if (!(f = new File("resources/guilds/" + g.getId() +"/")).exists()) {
			try {
				FileUtils.forceMkdir(f);
				FileUtils.copyFile(new File("resources/guilds/template.properties"), 
						new File("resources/guilds/" + g.getId() + "/GuildProperties.properties"));
				FileUtils.copyFile(new File("resources/guilds/template.db"), 
						new File("resources/guilds/" + g.getId() + "/Data.db"));
				FileUtils.copyFile(new File("resources/guilds/template.json"), 
						new File("resources/guilds/" + g.getId() + "/IdlePlaylist.json"));
				log.info("Guild file initialized: {}", g.getId());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
