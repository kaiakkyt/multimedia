package kaiakk.multimedia.classes;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorHandler {
	private ErrorHandler() {}

	public static void handle(Plugin plugin, String context, Throwable t) {
		Logger logger = plugin != null ? plugin.getLogger() : Logger.getLogger("Multimedia");
		if (context != null && !context.trim().isEmpty()) {
			if (t != null) logger.severe(context + ": " + t.toString());
			else logger.severe(context);
		} else if (t != null) {
			logger.severe(t.toString());
		}

		if (t != null) {
			for (StackTraceElement st : t.getStackTrace()) {
				logger.severe("  at " + st.toString());
			}
			Throwable cause = t.getCause();
			if (cause != null) {
				logger.severe("Caused by: " + cause.toString());
				for (StackTraceElement st : cause.getStackTrace()) logger.severe("  at " + st.toString());
			}
		}
	}

	public static void handle(Plugin plugin, Throwable t) {
		handle(plugin, null, t);
	}

	public static void warn(Plugin plugin, String msg, Throwable t) {
		Logger logger = plugin != null ? plugin.getLogger() : Logger.getLogger("Multimedia");
		if (msg != null) logger.warning(msg + (t == null ? "" : ": " + (t.getMessage() == null ? t.toString() : t.getMessage())));
		if (t != null) logger.log(Level.WARNING, "", t);
	}

	public static void info(Plugin plugin, String msg) {
		Logger logger = plugin != null ? plugin.getLogger() : Logger.getLogger("Multimedia");
		if (msg != null) logger.info(msg);
	}
}
