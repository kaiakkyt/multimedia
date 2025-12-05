package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to simplify task scheduling without writing BukkitRunnable boilerplate.
 * Examples:
 *   SchedulerHelper.runLater(plugin, () -> {...}, 20); // Run after 1 second
 *   SchedulerHelper.runTimer(plugin, () -> {...}, 0, 20); // Run every second
 *   SchedulerHelper.runAsync(plugin, () -> {...}); // Run on async thread
 */
public final class SchedulerHelper {

	private SchedulerHelper() {}

	private static final BukkitScheduler scheduler = Bukkit.getScheduler();
	private static final List<BukkitTask> trackedTasks = new ArrayList<>();
	private static Plugin pluginInstance;

	public static void setPlugin(Plugin plugin) {
		pluginInstance = plugin;
	}

	public static Plugin getPlugin() {
		return pluginInstance;
	}

	public static long secondsToTicks(double seconds) {
		return (long) (seconds * 20);
	}

	public static long minutesToTicks(double minutes) {
		return (long) (minutes * 60 * 20);
	}

	public static long hoursToTicks(double hours) {
		return (long) (hours * 60 * 60 * 20);
	}

	public static double ticksToSeconds(long ticks) {
		return ticks / 20.0;
	}

	public static double ticksToMinutes(long ticks) {
		return ticks / (20.0 * 60);
	}

	public static BukkitTask run(Plugin plugin, Runnable task) {
		if (plugin == null || task == null) return null;
		return scheduler.runTask(plugin, task);
	}

	public static BukkitTask runLater(Plugin plugin, Runnable task, long delayTicks) {
		if (plugin == null || task == null) return null;
		return scheduler.runTaskLater(plugin, task, delayTicks);
	}

	public static BukkitTask runLaterSeconds(Plugin plugin, Runnable task, double seconds) {
		return runLater(plugin, task, secondsToTicks(seconds));
	}

	public static BukkitTask runLaterMinutes(Plugin plugin, Runnable task, double minutes) {
		return runLater(plugin, task, minutesToTicks(minutes));
	}

	public static BukkitTask runTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
		if (plugin == null || task == null) return null;
		return scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
	}

	public static BukkitTask runTimerSeconds(Plugin plugin, Runnable task, double delaySeconds, double periodSeconds) {
		return runTimer(plugin, task, secondsToTicks(delaySeconds), secondsToTicks(periodSeconds));
	}

	public static BukkitTask runTimerMinutes(Plugin plugin, Runnable task, double delayMinutes, double periodMinutes) {
		return runTimer(plugin, task, minutesToTicks(delayMinutes), minutesToTicks(periodMinutes));
	}

	public static BukkitTask runAsync(Plugin plugin, Runnable task) {
		if (plugin == null || task == null) return null;
		return scheduler.runTaskAsynchronously(plugin, task);
	}

	public static BukkitTask runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
		if (plugin == null || task == null) return null;
		return scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
	}

	public static BukkitTask runAsyncLaterSeconds(Plugin plugin, Runnable task, double seconds) {
		return runAsyncLater(plugin, task, secondsToTicks(seconds));
	}

	public static BukkitTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
		if (plugin == null || task == null) return null;
		return scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
	}

	public static BukkitTask runAsyncTimerSeconds(Plugin plugin, Runnable task, double delaySeconds, double periodSeconds) {
		return runAsyncTimer(plugin, task, secondsToTicks(delaySeconds), secondsToTicks(periodSeconds));
	}

	public static BukkitTask runTracked(Plugin plugin, Runnable task) {
		BukkitTask t = run(plugin, task);
		if (t != null) trackedTasks.add(t);
		return t;
	}

	public static BukkitTask runLaterTracked(Plugin plugin, Runnable task, long delayTicks) {
		BukkitTask t = runLater(plugin, task, delayTicks);
		if (t != null) trackedTasks.add(t);
		return t;
	}

	public static BukkitTask runTimerTracked(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
		BukkitTask t = runTimer(plugin, task, delayTicks, periodTicks);
		if (t != null) trackedTasks.add(t);
		return t;
	}

	public static void cancelTask(BukkitTask task) {
		if (task != null) {
			task.cancel();
			trackedTasks.remove(task);
		}
	}

	public static void cancelTask(int taskId) {
		scheduler.cancelTask(taskId);
	}

	public static void cancelAllTasks(Plugin plugin) {
		if (plugin == null) return;
		scheduler.cancelTasks(plugin);
		trackedTasks.clear();
	}

	public static void cancelAllTrackedTasks() {
		for (BukkitTask task : new ArrayList<>(trackedTasks)) {
			if (task != null) task.cancel();
		}
		trackedTasks.clear();
	}

	public static BukkitTask countdown(Plugin plugin, int seconds, CountdownCallback callback) {
		if (plugin == null || callback == null) return null;
		return runTimer(plugin, new Runnable() {
			private int remaining = seconds;
			@Override
			public void run() {
				if (remaining > 0) {
					callback.onTick(remaining);
					remaining--;
				} else {
					callback.onFinish();
					scheduler.cancelTask(0); // Will be replaced by actual task
				}
			}
		}, 0, 20);
	}

	public interface CountdownCallback {
		void onTick(int secondsRemaining);
		void onFinish();
	}

	public static boolean isRunning(BukkitTask task) {
		return task != null && !task.isCancelled();
	}

	public static int getTaskId(BukkitTask task) {
		return task == null ? -1 : task.getTaskId();
	}

	public static List<BukkitTask> getTrackedTasks() {
		return new ArrayList<>(trackedTasks);
	}

}
