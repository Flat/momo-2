package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.CommandData;
import io.ph.bot.model.GuildObject;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Various one-time configuration settings
 * @author p
 *
 */
@CommandData (
		defaultSyntax = "configure",
		aliases = {"config"},
		permission = Permission.MANAGE_SERVER,
		description = "Configure various settings for your server",
		example = "(no parameters)"
		)
@ProceduralAnnotation (
		title = "Bot configuration",
		steps = {"Limit $joinrole to a single role?",
				"Automatically delete invite links sent by non-moderator users?",
				"How many messages can a user send per 15 seconds? Use 0 to disable slow mode"}, 
		types = {StepType.YES_NO, StepType.YES_NO, StepType.INTEGER},
		breakOut = "finish"
		)
public class Configure extends ProceduralCommand {

	public Configure(Message msg) {
		super(msg);
		super.setTitle(getTitle());
	}
	
	public Configure() {
		super(null);
	}

	@Override
	public void executeCommand(Message msg) {
		Configure instance = new Configure(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void finish() {
		GuildObject g = GuildObject.guildMap.get(super.getStarter().getGuild().getId());
		g.getConfig().setLimitToOneRole((boolean) super.getResponses().get(0));
		g.getConfig().setDisableInvites((boolean) super.getResponses().get(1));
		g.getConfig().setMessagesPerFifteen((int) super.getResponses().get(2));
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Success", null)
		.setColor(Color.GREEN)
		.setDescription("Configured my settings for your server!");
		super.getStarter().getChannel().sendMessage(em.build()).queue();
		super.exit();
	}

}
