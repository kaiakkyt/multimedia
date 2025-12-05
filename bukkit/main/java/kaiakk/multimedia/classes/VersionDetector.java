package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;

public final class VersionDetector {

	private VersionDetector() {}

	public enum ServerPlatform {
		LEAF,
		PUFFERFISH,
		PAPER,
		PURPUR,
		TUINITY,
		SPIGOT,
		CRAFTBUKKIT,
		UNKNOWN
	}

	public static ServerPlatform detectPlatform() {
		if (classExists("org.pufferfishmc.Pufferfish") || classExists("org.pufferfishmc.pufferfish.Pufferfish") || classExists("io.pufferfishmc.Pufferfish")) {
			return ServerPlatform.PUFFERFISH;
		}
		if (classExists("io.leafmc.Leaf") || classExists("io.leafmc.server.Leaf") || classExists("com.leafmc.server.Leaf")) {
			return ServerPlatform.LEAF;
		}
		if (classExists("com.destroystokyo.paper.PaperConfig") || classExists("com.destroystokyo.paper.Version")) {
			return ServerPlatform.PAPER;
		}
		if (classExists("net.pl3x.purpur.PurpurConfig") || classExists("org.purpurmc.purpur.Purpur")) {
			return ServerPlatform.PURPUR;
		}
		if (classExists("org.tuinity.Tuinity") || classExists("io.papermc.paper.configuration.PaperConfiguration")) {
			return ServerPlatform.TUINITY;
		}
		if (classExists("org.spigotmc.SpigotConfig") || classExists("org.spigotmc.Spigot")) {
			return ServerPlatform.SPIGOT;
		}

		try {
			String name = Bukkit.getServer().getName();
			if (name != null) {
				String lower = name.toLowerCase();
				if (lower.contains("paper")) return ServerPlatform.PAPER;
				if (lower.contains("purpur")) return ServerPlatform.PURPUR;
				if (lower.contains("pufferfish")) return ServerPlatform.PUFFERFISH;
				if (lower.contains("leaf")) return ServerPlatform.LEAF;
				if (lower.contains("tuinity") || lower.contains("tuin")) return ServerPlatform.TUINITY;
				if (lower.contains("spigot")) return ServerPlatform.SPIGOT;
				if (lower.contains("craftbukkit")) return ServerPlatform.CRAFTBUKKIT;
			}
		} catch (Throwable ignored) {
		}

		return ServerPlatform.UNKNOWN;
	}

	public static String getMinecraftVersion() {
		String bukkitVersion = Bukkit.getVersion();
		if (bukkitVersion != null) {
			java.util.regex.Matcher m = java.util.regex.Pattern.compile("MC: ([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)").matcher(bukkitVersion);
			if (m.find()) {
				return m.group(1);
			}
		}

		String bukkitShort = Bukkit.getBukkitVersion();
		if (bukkitShort != null) {
			java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)").matcher(bukkitShort);
			if (m2.find()) {
				return m2.group(1);
			}
		}

		return bukkitVersion != null ? bukkitVersion : (bukkitShort != null ? bukkitShort : "unknown");
	}

	public static boolean isLeaf() { return detectPlatform() == ServerPlatform.LEAF; }
	public static boolean isPufferfish() { return detectPlatform() == ServerPlatform.PUFFERFISH; }
	public static boolean isPaper() { return detectPlatform() == ServerPlatform.PAPER; }
	public static boolean isPurpur() { return detectPlatform() == ServerPlatform.PURPUR; }
	public static boolean isTuinity() { return detectPlatform() == ServerPlatform.TUINITY; }
	public static boolean isSpigot() { return detectPlatform() == ServerPlatform.SPIGOT; }
	public static boolean isCraftBukkit() { return detectPlatform() == ServerPlatform.CRAFTBUKKIT; }

	private static boolean classExists(String className) {
		try {
			Class.forName(className, false, VersionDetector.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (Throwable t) {
			return false;
		}
	}

}
