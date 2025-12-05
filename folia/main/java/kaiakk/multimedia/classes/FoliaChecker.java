package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;

/**
 * FoliaChecker - Detect if running on Folia or standard Bukkit/Spigot.
 * Folia uses a regionized scheduler instead of the global Bukkit scheduler.
 */
public final class FoliaChecker {

	private static Boolean isFolia = null;

	private FoliaChecker() {}

	/**
	 * Check if the server is running Folia.
	 * @return true if Folia is detected
	 */
	public static boolean isFolia() {
		if (isFolia != null) return isFolia;
		
		try {
			// Folia has the GlobalRegionScheduler class
			Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
			isFolia = true;
			return true;
		} catch (ClassNotFoundException e) {
			isFolia = false;
			return false;
		}
	}

	/**
	 * Check if the server is standard Bukkit/Spigot/Paper (not Folia).
	 * @return true if not Folia
	 */
	public static boolean isBukkit() {
		return !isFolia();
	}

	/**
	 * Get the server type as a string.
	 * @return "Folia" or "Bukkit"
	 */
	public static String getServerType() {
		return isFolia() ? "Folia" : "Bukkit";
	}

	/**
	 * Get the Folia global region scheduler if available.
	 * @return GlobalRegionScheduler or null if not Folia
	 */
	public static Object getGlobalRegionScheduler() {
		if (!isFolia()) return null;
		
		try {
			return Bukkit.getServer().getClass()
				.getMethod("getGlobalRegionScheduler")
				.invoke(Bukkit.getServer());
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to get GlobalRegionScheduler: " + t.getMessage());
			return null;
		}
	}

	/**
	 * Get the Folia async scheduler if available.
	 * @return AsyncScheduler or null if not Folia
	 */
	public static Object getAsyncScheduler() {
		if (!isFolia()) return null;
		
		try {
			return Bukkit.getServer().getClass()
				.getMethod("getAsyncScheduler")
				.invoke(Bukkit.getServer());
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to get AsyncScheduler: " + t.getMessage());
			return null;
		}
	}

	/**
	 * Get the Folia region scheduler for an entity if available.
	 * @param entity Entity to get scheduler for
	 * @return RegionScheduler or null
	 */
	public static Object getEntityScheduler(org.bukkit.entity.Entity entity) {
		if (!isFolia() || entity == null) return null;
		
		try {
			return entity.getClass()
				.getMethod("getScheduler")
				.invoke(entity);
		} catch (Throwable t) {
			return null;
		}
	}

	/**
	 * Get the Folia region scheduler for a location if available.
	 * @param location Location to get scheduler for
	 * @return RegionScheduler or null
	 */
	public static Object getRegionScheduler(org.bukkit.Location location) {
		if (!isFolia() || location == null || location.getWorld() == null) return null;
		
		try {
			return Bukkit.getServer().getClass()
				.getMethod("getRegionScheduler")
				.invoke(Bukkit.getServer());
		} catch (Throwable t) {
			return null;
		}
	}

	/**
	 * Reset the cached detection (for testing purposes).
	 */
	public static void resetCache() {
		isFolia = null;
	}
}
