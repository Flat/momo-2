package io.ph.bot.commands.moderation;

import com.mashape.unirest.http.exceptions.UnirestException;

import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.bot.model.StrawpollObject;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import net.dv8tion.jda.core.entities.Message;

/**
 * Create a strawpoll with the new procedural command system
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "strawpoll",
		aliases = {"createpoll", "poll"},
		permission = Permission.KICK,
		description = "Create a poll",
		example = "(no parameters)"
		)
@ProceduralAnnotation (
		title = "Strawpoll creation",
		steps = {"Name of poll?", "Can users multi-vote? (y/n)", "New option"}, 
		types = {StepType.STRING, StepType.YES_NO, StepType.REPEATER},
		breakOut = "finish"
		)
public class Strawpoll extends ProceduralCommand {

	public Strawpoll(Message msg) {
		super(msg);
		super.setTitle(getTitle());
	}

	/**
	 * Necessary constructor to register to commandhandler
	 */
	public Strawpoll() {
		super(null);
	}

	@Override
	public void executeCommand(Message msg) {
		Strawpoll instance = new Strawpoll(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void step(Message msg) {
		if(msg.getContent().equalsIgnoreCase(this.getBreakOut()) 
				&& getTypes()[super.getCurrentStep()].equals(StepType.REPEATER)) {
			finish();
			return;
		}

		super.step(msg);
	}
	@Override
	public void finish() {
		String title = (String) super.getResponses().get(0);
		if(title.length() > 400) {
			super.sendMessage("Error: Title length cannot be greater than 400 characters");
			super.exit();
		}
		boolean multi = (boolean) super.getResponses().get(1);
		String[] options = super.getResponses().subList(2, super.getResponses().size())
				.toArray(new String[super.getResponses().size()]);
		if(options.length < 2 || options.length > 30) {
			super.sendMessage("Error: You must have between 2 and 30 options (inclusive)");
			super.exit();
		}
		StrawpollObject poll = new StrawpollObject(title, multi, options);
		try {
			super.sendMessage("http://www.strawpoll.me/" + poll.createPoll());
		} catch (UnirestException e) {
			super.sendMessage("Sorry, something went wrong creating your Strawpoll - Might be over my limits!");
			e.printStackTrace();
		}
		super.exit();
	}
}