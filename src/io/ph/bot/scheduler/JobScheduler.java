package io.ph.bot.scheduler;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.TwitchEventListener;
import io.ph.bot.jobs.ReminderJob;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.jobs.TimedPunishJob;

public class JobScheduler {

	public static Scheduler scheduler;

	/**
	 * Load settings & start
	 */
	public static void initializeScheduler() {
		try {
			scheduler = new StdSchedulerFactory("resources/config/quartz.properties").getScheduler();
			scheduler.start();
			startJobs();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Periodically check the Twitch.tv API for stream status changes
	 */
	private static void twitchStreamCheck() {
		JobDetail job = JobBuilder.newJob(TwitchEventListener.class)
				.withIdentity("twitchJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("twitchJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(180).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Periodically check /r/all for updates
	 */
	private static void redditFeed() {
		JobDetail job = JobBuilder.newJob(RedditEventListener.class)
				.withIdentity("redditFeedJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("redditFeedJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(8).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Periodically check for reminders
	 */
	private static void remindCheck() {
		JobDetail job = JobBuilder.newJob(ReminderJob.class).withIdentity("reminderJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("reminderJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Periodically check to unban/mute offenders
	 */
	private static void punishCheck() {
		JobDetail job = JobBuilder.newJob(TimedPunishJob.class).withIdentity("punishJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("punishJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Change status on rotation
	 */
	private static void statusChange() {
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("statusChangeJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();
		try {
			scheduler.scheduleJob(StatusChangeJob.job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Register scheduler
	 */
	private static void startJobs() {
		try {
			Bot.getInstance().getApiKeys().get("twitch");
			twitchStreamCheck();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a Twitch.tv API "
					+ "key setup in Bot.properties - Your bot will not have support for Twitch.tv announcements.");
		}
		try {
			Bot.getInstance().getApiKeys().get("redditkey");
			redditFeed();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a "
					+ "reddit client/secret setup in Bot.properties - Your bot will not have support for Reddit feeds");
		}
		if(StatusChangeJob.statuses != null && StatusChangeJob.statuses.length > 0
				&& !StatusChangeJob.statuses[0].isEmpty()) {
			statusChange();
		}
			
		remindCheck();
		punishCheck();
	}
}
