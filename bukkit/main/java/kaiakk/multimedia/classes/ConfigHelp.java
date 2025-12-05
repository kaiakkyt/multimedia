package kaiakk.multimedia.classes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;

/**
 * Tiny helper to make using/generating `config.yml` extremely simple.
 * Usage:
 *   ConfigHelp.init(plugin); // loads or creates config.yml
 *   ConfigHelp.ensureDefaults(Map.of("path.some", "value", "other", 123));
 *   String s = ConfigHelp.getString("path.some", "fallback");
 *   ConfigHelp.set("path.some", "new");
 */
public final class ConfigHelp {

	private static JavaPlugin plugin;
	private static FileConfiguration cfg;

	private ConfigHelp() {}

	/** Initialize helper with your plugin. Will load/create `config.yml`. */
	public static void init(JavaPlugin p) {
		if (p == null) return;
		plugin = p;
		// Ensure default config exists in plugin folder (copy from jar if present)
		try {
			plugin.saveDefaultConfig();
		} catch (Throwable ignored) {}
		cfg = plugin.getConfig();
	}

	/** Ensure the given defaults exist (won't overwrite existing values). */
	public static void ensureDefaults(Map<String, Object> defaults) {
		if (defaults == null || defaults.isEmpty() || cfg == null) return;
		boolean changed = false;
		for (Map.Entry<String, Object> e : defaults.entrySet()) {
			if (!cfg.contains(e.getKey())) {
				cfg.set(e.getKey(), e.getValue());
				changed = true;
			}
		}
		if (changed) save();
	}

	/** Convenience when you only have a single default. */
	public static void ensureDefault(String path, Object value) {
		if (path == null || cfg == null) return;
		if (!cfg.contains(path)) {
			cfg.set(path, value);
			save();
		}
	}

	public static boolean has(String path) { return cfg != null && cfg.contains(path); }

	public static String getString(String path, String fallback) {
		if (cfg == null) return fallback;
		if (!cfg.contains(path)) return fallback;
		return cfg.getString(path, fallback);
	}

	public static int getInt(String path, int fallback) {
		if (cfg == null) return fallback;
		if (!cfg.contains(path)) return fallback;
		return cfg.getInt(path, fallback);
	}

	public static boolean getBoolean(String path, boolean fallback) {
		if (cfg == null) return fallback;
		if (!cfg.contains(path)) return fallback;
		return cfg.getBoolean(path, fallback);
	}

	public static double getDouble(String path, double fallback) {
		if (cfg == null) return fallback;
		if (!cfg.contains(path)) return fallback;
		return cfg.getDouble(path, fallback);
	}

	public static java.util.List<String> getStringList(String path) {
		if (cfg == null) return Collections.emptyList();
		return cfg.getStringList(path);
	}

	/** Set value and immediately save config. */
	public static void set(String path, Object value) {
		if (cfg == null) return;
		cfg.set(path, value);
		save();
	}

	public static void save() {
		if (plugin == null || cfg == null) return;
		try {
			plugin.saveConfig();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void reload() {
		if (plugin == null) return;
		try {
			plugin.reloadConfig();
			cfg = plugin.getConfig();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/** Generate defaults from provided map and initialize if needed. */
	public static void generateAndInit(JavaPlugin p, Map<String, Object> defaults) {
		init(p);
		if (defaults != null && !defaults.isEmpty()) ensureDefaults(defaults);
	}

	/** Returns the underlying FileConfiguration for advanced use (may be null until init). */
	public static FileConfiguration getConfig() { return cfg; }

}
