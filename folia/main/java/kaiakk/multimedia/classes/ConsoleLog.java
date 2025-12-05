package kaiakk.multimedia.classes;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple console logging helper.
 * Initialize with `ConsoleLog.init(plugin)` from your plugin's `onEnable()`.
 * Set log level with `ConsoleLog.setLevel(LogLevel.INFO)`.
 */
public final class ConsoleLog {

	public enum LogLevel {
		INFO(Level.INFO),
		WARNING(Level.WARNING),
		ERROR(Level.SEVERE);
		
		private final Level level;
		LogLevel(Level level) { this.level = level; }
		public Level getLevel() { return level; }
	}

	private static Logger logger = null;
	private static String prefix = "";
	private static LogLevel minLevel = LogLevel.INFO;

	private ConsoleLog() {}

	public static void init(JavaPlugin plugin) {
		if (plugin != null) {
			logger = plugin.getLogger();
			try {
				String name = plugin.getDescription().getName();
				if (name != null && !name.isEmpty()) prefix = "";
			} catch (Throwable ignored) {}
		}
	}

	/**
	 * Set the minimum log level. Messages below this level will be ignored.
	 * Default is INFO (logs everything).
	 */
	public static void setLevel(LogLevel level) {
		if (level != null) minLevel = level;
	}

	public static LogLevel getLevel() {
		return minLevel;
	}

	public static void info(String msg) {
		log(Level.INFO, msg);
	}

	public static void warn(String msg) {
		log(Level.WARNING, msg);
	}

	public static void error(String msg) {
		log(Level.SEVERE, msg);
	}

	public static void error(String msg, Throwable t) {
		log(Level.SEVERE, msg);
		if (t != null) t.printStackTrace();
	}

	public static void infof(String fmt, Object... args) { info(String.format(fmt, args)); }
	public static void warnf(String fmt, Object... args) { warn(String.format(fmt, args)); }
	public static void errorf(String fmt, Object... args) { error(String.format(fmt, args)); }

	private static void log(Level level, String msg) {
		// Filter by minimum log level
		if (level.intValue() < minLevel.getLevel().intValue()) return;
		
		String out = prefix + (msg == null ? "" : msg);
		if (logger != null) {
			logger.log(level, out);
		} else {
			Logger.getLogger("Multimedia").log(level, out);
		}
	}

}
