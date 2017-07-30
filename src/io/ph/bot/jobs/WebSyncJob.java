package io.ph.bot.jobs;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.ph.bot.Bot;
import io.ph.web.WebServer;

/**
 * Web sync
 * Sync data to the dashboard
 * @author Paul
 *
 */
public class WebSyncJob implements Job {
	public static int messageCount = 0;
	public static int commandCount = 0;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
		AtomicInteger userCount = new AtomicInteger();
		AtomicInteger guildCount = new AtomicInteger();
		
		Bot.getInstance().getBots().stream()
		.forEach(j -> { 
			userCount.addAndGet(j.getUsers().size());
			guildCount.addAndGet(j.getGuilds().size());
		});
		
		
		WebServer.getBotStats().setMessageCount(messageCount)
		.setCommandCount(commandCount)
		.setUsers(userCount.get())
		.setMemoryUsage((int) (((double) total - free) / total * 100))
		.setGuilds(guildCount.get())
		.setUptimeHours((int) TimeUnit.MILLISECONDS.toHours(uptime))
		.setUptimeMinutes((int) (TimeUnit.MILLISECONDS.toMinutes(uptime)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime))));

		messageCount = 0;
		commandCount = 0;
	}

}
