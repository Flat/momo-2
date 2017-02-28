package io.ph.bot.procedural;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.util.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Superclass for procedural commands
 * For usage, see {@link io.ph.bot.commands.moderation.Strawpoll}
 * @author Paul
 *
 */
public abstract class ProceduralCommand extends Command implements ProceduralInterface {
	
	// This message is the one that started it all
	private Message starter;
	// Title of all embeds
	private String title;
	private int currentStep;
	private List<Object> responses = new ArrayList<Object>();
	private List<Object> cache;
	
	public void addCache(Object o) {
		if(cache == null)
			cache = new ArrayList<Object>();
		cache.add(o);
	}
	public List<Object> getCache() {
		return this.cache;
	}
	/**
	 * Generate a procedural command and set the starting message
	 * @param msg Message that started it all
	 */
	public ProceduralCommand(Message msg) {
		currentStep = 0;
		this.starter = msg;
	}
	/*public void initiate(Message msg) {
		this.starter = msg;
		currentStep = 0;
	}*/
	/**
	 * Send a (templated) message to the original channel
	 * @param description Description to be included
	 */
	public void sendMessage(String description) {
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle(this.title, null)
		.setColor(Color.MAGENTA)
		.setDescription(description)
		.setFooter("Type \"exit\" to quit", null);
		this.starter.getChannel().sendMessage(em.build()).queue();
	}
	
	/**
	 * Step through the options
	 * @param msg {@link Message} that triggered this
	 */
	public void step(Message msg) {
		if(msg.getContent().equalsIgnoreCase("exit")) {
			exit();
			return;
		}
		if(msg.getContent().equalsIgnoreCase(this.getBreakOut()) 
				&& getTypes()[getCurrentStep()].equals(StepType.REPEATER)) {
			incrementStep();
			if(getCurrentStep() >= getSteps().length) {
				finish();
				return;
			}
		}
		switch(getTypes()[getCurrentStep()]) {
		case STRING:
			addResponse(msg.getContent());
			break;
		case INTEGER:
			if(Util.isInteger(msg.getContent())) {
				addResponse(Integer.parseInt(msg.getContent()));
			} else {
				sendMessage("Error: Not a valid numerical input\n" + getSteps()[getCurrentStep()]);
				return;
			}
			break;
		case DOUBLE:
			if(Util.isDouble(msg.getContent())) {
				addResponse(Double.parseDouble(msg.getContent()));
			} else {
				sendMessage("Error: Not a valid decimal input\n" + getSteps()[getCurrentStep()]);
				return;
			}
			break;
		case REPEATER:
			addResponse(msg.getContent());
			sendMessage(getSteps()[getCurrentStep()] + " (to finish, respond with \"" + getBreakOut() + "\")");
			return;
		case YES_NO:
			String s = msg.getContent();
			if(s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes")) {
				addResponse(true);
			} else if(s.equalsIgnoreCase("n") || s.equalsIgnoreCase("no")) {
				addResponse(false);
			} else {
				sendMessage("Invalid yes/no answer: Please use \"y\" or \"n\"\n" + getSteps()[getCurrentStep()]);
				return;
			}
			break;
		}
		if(getCurrentStep() + 1 >= getSteps().length) {
			finish();
		} else {
			incrementStep();
			sendMessage(getSteps()[getCurrentStep()]);
		}
	}
	
	/**
	 * Finish this command, handle the responses
	 */
	public abstract void finish();
	
	/**
	 * Exit stepping through and remove from listener
	 */
	public void exit() {
		ProceduralListener.getInstance().removeListener(this.starter);
	}
	public Message getStarter() {
		return this.starter;
	}
	public void addResponse(Object o) {
		this.responses.add(o);
	}
	public List<Object> getResponses() {
		return this.responses;
	}
	public int getCurrentStep() {
		return currentStep;
	}
	public void incrementStep() {
		currentStep++;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
