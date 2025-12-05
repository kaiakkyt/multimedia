package kaiakk.multimedia.classes;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * JavaUtilities - Memory, GC, thread management, and system info helpers.
 * Provides automatic memory monitoring, manual GC triggers, and system diagnostics.
 */
public final class JavaUtilities {

	private static JavaPlugin plugin;
	private static final List<MemoryWarningListener> memoryListeners = new ArrayList<>();
	private static BukkitRunnable memoryMonitorTask;
	private static long memoryThresholdBytes = 0;

	private JavaUtilities() {}

	public static void init(JavaPlugin pluginInstance) {
		plugin = pluginInstance;
	}

	/**
	 * Get current memory usage information.
	 * @return MemoryInfo object with usage details
	 */
	public static MemoryInfo getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = allocatedMemory - freeMemory;
		
		return new MemoryInfo(maxMemory, allocatedMemory, freeMemory, usedMemory);
	}

	/**
	 * Force garbage collection. Use sparingly as it can cause lag.
	 * @return true if GC was triggered
	 */
	public static boolean forceGC() {
		try {
			System.gc();
			System.runFinalization();
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to force GC: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Get memory usage as a formatted string.
	 * @return String with memory info (e.g., "Used: 512MB / 2GB")
	 */
	public static String getMemoryString() {
		MemoryInfo info = getMemoryUsage();
		return String.format("Used: %dMB / %dMB (%.1f%%)",
			info.getUsedMB(), info.getMaxMB(), info.getUsedPercent());
	}

	/**
	 * Register a memory warning listener that triggers when memory exceeds threshold.
	 * @param thresholdPercent Percentage (0-100) to trigger warning
	 * @param listener Callback when threshold is exceeded
	 */
	public static void registerMemoryWarning(double thresholdPercent, MemoryWarningListener listener) {
		if (listener == null || thresholdPercent <= 0 || thresholdPercent > 100) return;
		
		Runtime runtime = Runtime.getRuntime();
		memoryThresholdBytes = (long) (runtime.maxMemory() * (thresholdPercent / 100.0));
		
		if (!memoryListeners.contains(listener)) {
			memoryListeners.add(listener);
		}
		
		startMemoryMonitor();
	}

	/**
	 * Start automatic memory monitoring (checks every 30 seconds).
	 */
	public static void startMemoryMonitor() {
		if (plugin == null) {
			ConsoleLog.warn("JavaUtilities not initialized. Call init() first.");
			return;
		}
		
		if (memoryMonitorTask != null) return; // Already running
		
		// Use SchedulerHelper for Folia compatibility
		if (FoliaChecker.isFolia()) {
			SchedulerHelper.runAsyncTimer(plugin, () -> checkMemory(), 600L, 600L);
		} else {
			memoryMonitorTask = new BukkitRunnable() {
				@Override
				public void run() {
					checkMemory();
				}
			};
			memoryMonitorTask.runTaskTimerAsynchronously(plugin, 600L, 600L);
		}
	}
	
	private static void checkMemory() {
		MemoryInfo info = getMemoryUsage();
		if (info.getUsed() > memoryThresholdBytes && !memoryListeners.isEmpty()) {
			for (MemoryWarningListener listener : memoryListeners) {
				try {
					listener.onMemoryWarning(info);
				} catch (Throwable t) {
					ConsoleLog.warn("Memory listener error: " + t.getMessage());
				}
			}
		}
	}

	/**
	 * Stop automatic memory monitoring.
	 */
	public static void stopMemoryMonitor() {
		if (memoryMonitorTask != null) {
			memoryMonitorTask.cancel();
			memoryMonitorTask = null;
		}
	}

	/**
	 * Clear all memory warning listeners.
	 */
	public static void clearMemoryListeners() {
		memoryListeners.clear();
	}

	/**
	 * Get the number of active threads.
	 * @return Thread count
	 */
	public static int getThreadCount() {
		return Thread.activeCount();
	}

	/**
	 * Get detailed thread information.
	 * @return ThreadInfo object
	 */
	public static ThreadInfo getThreadInfo() {
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		return new ThreadInfo(
			threadBean.getThreadCount(),
			threadBean.getPeakThreadCount(),
			threadBean.getDaemonThreadCount(),
			threadBean.getTotalStartedThreadCount()
		);
	}

	/**
	 * Run a task asynchronously and return a CompletableFuture.
	 * @param task Task to run
	 * @param <T> Return type
	 * @return CompletableFuture with result
	 */
	public static <T> CompletableFuture<T> runAsync(Callable<T> task) {
		CompletableFuture<T> future = new CompletableFuture<>();
		if (plugin == null) {
			future.completeExceptionally(new IllegalStateException("JavaUtilities not initialized"));
			return future;
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					T result = task.call();
					future.complete(result);
				} catch (Throwable t) {
					future.completeExceptionally(t);
				}
			}
		}.runTaskAsynchronously(plugin);
		
		return future;
	}

	/**
	 * Run a task asynchronously (no return value).
	 * @param task Task to run
	 */
	public static void runAsync(Runnable task) {
		if (plugin == null) {
			ConsoleLog.warn("JavaUtilities not initialized. Call init() first.");
			return;
		}
		
		if (FoliaChecker.isFolia()) {
			SchedulerHelper.runAsync(plugin, task);
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						task.run();
					} catch (Throwable t) {
						ConsoleLog.warn("Async task error: " + t.getMessage());
					}
				}
			}.runTaskAsynchronously(plugin);
		}
	}

	/**
	 * Get comprehensive system information.
	 * @return SystemInfo object
	 */
	public static SystemInfo getSystemInfo() {
		Runtime runtime = Runtime.getRuntime();
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		int availableProcessors = runtime.availableProcessors();
		double systemLoad = osBean.getSystemLoadAverage();
		
		MemoryInfo memInfo = getMemoryUsage();
		ThreadInfo threadInfo = getThreadInfo();
		
		return new SystemInfo(osName, osVersion, osArch, javaVersion, javaVendor,
			availableProcessors, systemLoad, memInfo, threadInfo);
	}

	/**
	 * Get system info as a formatted string.
	 * @return Multiline string with system details
	 */
	public static String getSystemInfoString() {
		SystemInfo info = getSystemInfo();
		StringBuilder sb = new StringBuilder();
		sb.append("=== System Information ===\n");
		sb.append("OS: ").append(info.getOsName()).append(" ").append(info.getOsVersion()).append(" (").append(info.getOsArch()).append(")\n");
		sb.append("Java: ").append(info.getJavaVersion()).append(" (").append(info.getJavaVendor()).append(")\n");
		sb.append("Processors: ").append(info.getAvailableProcessors()).append("\n");
		sb.append("System Load: ").append(info.getSystemLoad() >= 0 ? String.format("%.2f", info.getSystemLoad()) : "N/A").append("\n");
		sb.append("Memory: ").append(getMemoryString()).append("\n");
		sb.append("Threads: ").append(info.getThreadInfo().getCurrentThreads()).append(" active, ");
		sb.append(info.getThreadInfo().getPeakThreads()).append(" peak\n");
		return sb.toString();
	}

	public static class MemoryInfo {
		private final long max, allocated, free, used;

		public MemoryInfo(long max, long allocated, long free, long used) {
			this.max = max;
			this.allocated = allocated;
			this.free = free;
			this.used = used;
		}

		public long getMax() { return max; }
		public long getAllocated() { return allocated; }
		public long getFree() { return free; }
		public long getUsed() { return used; }
		
		public long getMaxMB() { return max / 1024 / 1024; }
		public long getAllocatedMB() { return allocated / 1024 / 1024; }
		public long getFreeMB() { return free / 1024 / 1024; }
		public long getUsedMB() { return used / 1024 / 1024; }
		
		public double getUsedPercent() { 
			return max > 0 ? (used * 100.0 / max) : 0; 
		}

		@Override
		public String toString() {
			return String.format("Memory[Used=%dMB, Max=%dMB, %.1f%%]", 
				getUsedMB(), getMaxMB(), getUsedPercent());
		}
	}

	public static class ThreadInfo {
		private final int currentThreads, peakThreads, daemonThreads;
		private final long totalStartedThreads;

		public ThreadInfo(int current, int peak, int daemon, long totalStarted) {
			this.currentThreads = current;
			this.peakThreads = peak;
			this.daemonThreads = daemon;
			this.totalStartedThreads = totalStarted;
		}

		public int getCurrentThreads() { return currentThreads; }
		public int getPeakThreads() { return peakThreads; }
		public int getDaemonThreads() { return daemonThreads; }
		public long getTotalStartedThreads() { return totalStartedThreads; }

		@Override
		public String toString() {
			return String.format("Threads[Current=%d, Peak=%d, Daemon=%d, Total=%d]",
				currentThreads, peakThreads, daemonThreads, totalStartedThreads);
		}
	}

	public static class SystemInfo {
		private final String osName, osVersion, osArch, javaVersion, javaVendor;
		private final int availableProcessors;
		private final double systemLoad;
		private final MemoryInfo memoryInfo;
		private final ThreadInfo threadInfo;

		public SystemInfo(String osName, String osVersion, String osArch, String javaVersion,
						  String javaVendor, int processors, double systemLoad,
						  MemoryInfo memInfo, ThreadInfo threadInfo) {
			this.osName = osName;
			this.osVersion = osVersion;
			this.osArch = osArch;
			this.javaVersion = javaVersion;
			this.javaVendor = javaVendor;
			this.availableProcessors = processors;
			this.systemLoad = systemLoad;
			this.memoryInfo = memInfo;
			this.threadInfo = threadInfo;
		}

		public String getOsName() { return osName; }
		public String getOsVersion() { return osVersion; }
		public String getOsArch() { return osArch; }
		public String getJavaVersion() { return javaVersion; }
		public String getJavaVendor() { return javaVendor; }
		public int getAvailableProcessors() { return availableProcessors; }
		public double getSystemLoad() { return systemLoad; }
		public MemoryInfo getMemoryInfo() { return memoryInfo; }
		public ThreadInfo getThreadInfo() { return threadInfo; }

		@Override
		public String toString() {
			return String.format("System[OS=%s %s, Java=%s, Processors=%d, %s, %s]",
				osName, osVersion, javaVersion, availableProcessors, memoryInfo, threadInfo);
		}
	}

	@FunctionalInterface
	public interface MemoryWarningListener {
		void onMemoryWarning(MemoryInfo memoryInfo);
	}
}
