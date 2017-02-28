package io.ph.bot.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.ph.bot.State;

public class StatusChangeJob implements Job {
	public static String[] statuses;
	private static int index = 0;

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		State.changeBotStatus(statuses[index]);
		if(++index >= statuses.length)
			index = 0;
	}
	
	/**
	 * Calling $update changes the status array to a countdown timer
	 * @param minutes Minutes to countdown
	 */
	public static void commenceUpdateCountdown(int minutes) {
		statuses = new String[minutes + 1];
		for(int i = minutes; i > 0; i--) {
			statuses[(minutes - i)] = "Restart in " + i + " minutes";
		}
		statuses[statuses.length - 1] = "Restart now!";
		index = 0;
	}
}
