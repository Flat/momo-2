package io.ph.bot.commands.general;

import java.awt.Color;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.MacroObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

/**
 * Create, delete, search, call information, and call macros
 * A macro is a way of mapping a word or phrase to an output
 * This can be used for actions such as reaction images, or saving something for later use
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "macro",
		aliases = {"m"},
		permission = Permission.NONE,
		description = "Create, delete, edit, search, or get information on a macro\n"
				+ "A macro is a quick way to bind text or links to a shortcut",
				example = "create \"test macro\" contents *This creates a macro named `test macro`*\n"
						+ "delete test macro *This deletes the macro*\n"
						+ "edit \"test macro\" new contents *This edits the macro's contents*\n"
						+ "info test macro *This gives information on the macro*\n"
						+ "test macro *This calls the macro*"
		)
public class Macro extends Command {
	private EmbedBuilder em;
	private Message msg;
	private String contents;

	@Override
	public void executeCommand(Message msg) {
		this.msg = msg;
		em = new EmbedBuilder();
		contents = Util.getCommandContents(msg);
		if(contents.equals("")) {
			MessageUtils.sendIncorrectCommandUsage(msg, this);
			return;
		}

		String param = Util.getParam(msg);
		if(param.equalsIgnoreCase("create")) {
			createMacro();
		} else if(param.equalsIgnoreCase("delete")) {
			deleteMacro();
		} else if(param.equalsIgnoreCase("edit")) {
			editMacro();
		} else if(param.equalsIgnoreCase("search")) {
			searchForMacro();
		} else if(param.equalsIgnoreCase("info")) {
			macroInfo();
		} else {
			try {
				MacroObject m = MacroObject.forName(contents, msg.getGuild().getId(), true);
				msg.getChannel().sendMessage(m.getMacroContent()).queue();
				return;
			} catch (IllegalArgumentException e) {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription(e.getMessage());
			}
		}
		msg.getChannel().sendMessage(em.build()).queue();
	}

	private void createMacro() {
		contents = Util.getCommandContents(contents);
		if(contents.equals("") || contents.split(" ").length < 2) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.addField(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix() 
					+ "macro create name contents",
					"You have designated to create a macro, but your"
							+ " command does not meet all requirements\n"
							+ "*name* - Name of the macro. If it is multi-worded, "
							+ "you can surround it in \"quotation marks\"\n"
							+ "*contents* - Contents of the macro", true);
			return;
		}
		String[] resolved = resolveMacroNameAndContents(contents);
		MacroObject m = new MacroObject(msg.getAuthor().getName(), resolved[0], resolved[1],
				0, msg.getAuthor().getId(), msg.getGuild().getId());
		try {
			if(m.create()) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Macro **" + resolved[0] + "** created");
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("Macro **" + resolved[0] + "** already exists");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteMacro() {
		contents = Util.getCommandContents(contents);
		if(contents.equals("")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.addField(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix() 
					+ "macro delete name",
					"You have designated to delete a macro, "
							+ "but your command does not meet all requirements"
							+ "*name* - Name of the macro. No quotation "
							+ "marks for multi-worded macros", true);
			return;
		}
		try {
			MacroObject m = MacroObject.forName(contents, msg.getGuild().getId());
			if(m.delete(msg.getAuthor().getId())) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Macro **" + contents + "** deleted");
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("You cannot delete macro **" + contents + "**")
				.setFooter("Users can only delete their own macros", null);
			}
		} catch (IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(e.getMessage());
		}
	}

	private void editMacro() {
		contents = Util.getCommandContents(contents);
		if(contents.equals("")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.addField(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix() 
					+ "macro edit name content",
					"You have designated to edit a macro, but your "
							+ "command does not meet all requirements"
							+ "*name* - Name of the macro. Requires "
							+ "\"quotation marks\" for multi-worde macros"
							+ "*content* - Content of the macro", true);
			return;

		}
		String[] resolved = resolveMacroNameAndContents(contents);
		try {
			MacroObject m = MacroObject.forName(resolved[0], msg.getGuild().getId());
			if(m.edit(msg.getAuthor().getId(), resolved[1])) {
				em.setTitle("Success", null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
				.setDescription("Macro **" + resolved[0] + "** edited");
			} else {
				em.setTitle("Error", null)
				.setColor(Color.RED)
				.setDescription("You cannot edit macro **" + contents + "**")
				.setFooter("Users can only edit their own macros", null);
			}
		} catch (IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription("Macro **" + resolved[0] + "** does not exist");
		}
	}

	private void searchForMacro() {
		contents = Util.getCommandContents(contents);
		if(contents.equals("")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.addField(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix() 
					+ "macro search [macro-name|user]",
					"You have designated to search for a macro, but "
							+ "your command does not meet all requirements"
							+ "*name* - Name of the macro to search for. "
							+ "No quotation marks needed for multi-word macros"
							+ "*user* - An @ mention of a user to search for", true);
			return;
		}
		String[] result;
		StringBuilder sb = new StringBuilder();
		// Search mentions a user
		if(msg.getMentionedUsers().size() == 1) {
			if((result = MacroObject.searchByUser(msg.getMentionedUsers().get(0).getId(),
					msg.getGuild().getId())) != null) {
				em.setTitle("Search results for user " 
						+ msg.getGuild().getMember(msg
								.getMentionedUsers().get(0)).getEffectiveName(), null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
				int i = 0;
				for(String s : result) {
					if(i++ == 75) {
						em.setFooter("Search limited to 75 results", null);
						break;
					}
					sb.append(s + ", ");
				}
				sb.setLength(sb.length() - 2);
				em.setDescription(sb.toString());
			} else {
				em.setTitle("No macros found", null)
				.setColor(Color.RED)
				.setDescription("No results for user **" 
						+ msg.getGuild().getMember(msg
								.getMentionedUsers().get(0)).getEffectiveName() + "**");
			}
		} else {
			if((result = MacroObject.searchForName(contents, msg.getGuild().getId())) != null) {
				em.setTitle("Search results for " + contents, null)
				.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN));
				int i = 0;
				for(String s : result) {
					if(i++ == 75) {
						em.setFooter("Search limited to 75 results", null);
						break;
					}
					sb.append(s + ", ");
				}
				sb.setLength(sb.length() - 2);
				em.setDescription(sb.toString());
			} else {
				em.setTitle("No macros found", null)
				.setColor(Color.RED)
				.setDescription("No results for **" + contents + "**");
			}
		}
	}

	private void macroInfo() {
		contents = Util.getCommandContents(contents);
		if(contents.equals("")) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.addField(GuildObject.guildMap.get(msg.getGuild().getId()).getConfig().getCommandPrefix() 
					+ "macro info name",
					"You have designated to search for a macro, "
							+ "but your command does not meet all requirements"
							+ "*name* - Name of the macro to display info for. "
							+ "No quotation marks needed for multi-word macros", true);
			return;
		}
		try {
			MacroObject m = MacroObject.forName(contents, msg.getGuild().getId());
			Member mem = msg.getGuild().getMemberById(m.getUserId());
			em.setTitle("Information on " + contents, null)
			.setColor(Util.resolveColor(Util.memberFromMessage(msg), Color.GREEN))
			.addField("Creator", mem == null ? m.getFallbackUsername() : mem.getEffectiveName(), true)
			.addField("Hits", m.getHits()+"", true)
			.addField("Date created", m.getDate().toString(), true);
		} catch (IllegalArgumentException e) {
			em.setTitle("Error", null)
			.setColor(Color.RED)
			.setDescription(e.getMessage());
		}
	}
	/**
	 * Resolve macro name and contents from a create statement
	 * This works to involve quotations around a spaced macro name
	 * @param s The parameters of a create statement - The contents past the $macro create bit
	 * @return Two index array: [0] is the macro name, [1] is the contents
	 * Prerequisite: s.split() must have length of >= 2
	 */
	private static String[] resolveMacroNameAndContents(String s) {
		String[] toReturn = new String[2];
		if(s.contains("\"") && StringUtils.countMatches(s, "\"") > 1) {
			int secondIndexOfQuotes = s.indexOf("\"", s.indexOf("\"") + 1);
			toReturn[0] = s.substring(s.indexOf("\"") + 1, secondIndexOfQuotes);
			toReturn[1] = s.substring(secondIndexOfQuotes + 2);
		} else {
			toReturn[0] = s.split(" ")[0];
			toReturn[1] = Util.getCommandContents(s);
		}
		return toReturn;
	}
}
