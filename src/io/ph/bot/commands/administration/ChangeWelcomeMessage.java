package io.ph.bot.commands.administration;

import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.Util;
import net.dv8tion.jda.core.entities.Message;

/**
 * Change welcome message the server sends when a new person is joined
 * $user$ and $server$ are replaced with the new user and the server respectively
 * Note: this will not send if there is no welcome channel set
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "changewelcome",
		aliases = {"welcomemessage", "changewelcomemessage"},
		permission = Permission.MANAGE_SERVER,
		description = "Change the server's welcome message. Use $user$ and $server$ to replace with the new user and the server name, respectively",
		example = "Welcome $user$ to $server$!"
		)
@ProceduralAnnotation (
		title = "Welcome message",
		steps = {"PM message to the user? If no, the message will be broadcast to your "
				+ "designated welcome channel (y/n)"}, 
		types = {StepType.YES_NO},
		breakOut = "finish"
		)
public class ChangeWelcomeMessage extends ProceduralCommand {
	public ChangeWelcomeMessage(Message msg) {
		super(msg);
		super.setTitle(getTitle());
	}
	
	/**
	 * Necessary constructor to register to commandhandler
	 */
	public ChangeWelcomeMessage() {
		super(null);
	}
	
	@Override
	public void executeCommand(Message msg) {
		ChangeWelcomeMessage instance = new ChangeWelcomeMessage(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void finish() {
		boolean pmWelcomeMessage = (boolean) super.getResponses().get(0);
		String welcomeMessage = Util.getCommandContents(super.getStarter());
		GuildObject.guildMap.get(super.getStarter().getGuild().getId()).getConfig().setPmWelcomeMessage(pmWelcomeMessage);
		GuildObject.guildMap.get(super.getStarter().getGuild().getId()).getConfig().setWelcomeMessage(welcomeMessage);
		if(welcomeMessage.equals(""))
			super.sendMessage("Reset welcome message");
		else
			super.sendMessage("**Changed welcome message to**\n" + welcomeMessage);
		super.exit();
	}
}
