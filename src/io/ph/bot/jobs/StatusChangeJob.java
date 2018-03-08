package io.ph.bot.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.ph.bot.State;

public class StatusChangeJob implements Job {
	public static List<String> statuses = new ArrayList<>();
	private static int index = 0;
	public static JobDetail job = 
			JobBuilder.newJob(StatusChangeJob.class).withIdentity("statusChangeJob", "group1").build();
	
	private static boolean interrupted = false;
	
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if (interrupted) {
			return;
		}
		State.changeBotStatus(statuses.get(index));
		if (++index >= statuses.size()) {
			index = 0;
		}
	}
	
	/**
	 * Calling $update changes the status array to a countdown timer
	 * @param minutes Minutes to countdown
	 */
	public static void commenceUpdateCountdown(int minutes) {
		statuses.clear();
		String[] strings = new String[minutes + 1];
		for (int i = minutes; i > 0; i--) {
			strings[(minutes - i)] = "Restart in " + i + " minutes";
		}
		strings[strings.length - 1] = "Restart now!";
		index = 0;
		statuses.addAll(Arrays.asList(strings));
	}
	
	/**
	 * Set statuses
	 * @param s String array of statuses
	 */
	public static void setStatuses(String[] s) {
		statuses.clear();
		statuses.addAll(Arrays.asList(s));
		index = 0;
	}

	/**
	 * Add a status to the rotation
	 * @param s Status to add
	 */
	public static void addStatus(String s) {
		statuses.add(s);
	}
	
	/**
	 * Remove a status from the rotation
	 * @param s Status to remove
	 */
	public static void removeStatus(String s) {
		statuses.remove(s);
	}
	
	public static void interrupt() {
		interrupted = true;
	}
	
	public static void resume() {
		interrupted = false;
	}
	
	public static boolean isInterrupted() {
		return interrupted;
	}
}
