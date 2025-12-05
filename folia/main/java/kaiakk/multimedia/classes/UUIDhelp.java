package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UUIDhelp - UUID/username utilities with caching and Mojang API integration.
 * Makes player identification and lookup easy.
 */
public final class UUIDhelp {

	private static final String MOJANG_API_UUID = "https://api.mojang.com/users/profiles/minecraft/";
	private static final String MOJANG_API_NAME = "https://sessionserver.mojang.com/session/minecraft/profile/";
	
	private static final Map<String, UUID> nameToUuidCache = new ConcurrentHashMap<>();
	private static final Map<UUID, String> uuidToNameCache = new ConcurrentHashMap<>();
	@SuppressWarnings("unused") // Reserved for future cache expiry feature
	private static long cacheExpiryMs = 600000; // 10 minutes

	private UUIDhelp() {}

	/**
	 * Get UUID for a player name (checks cache, then online players, then Mojang API).
	 * @param playerName Player name
	 * @return UUID or null if not found
	 */
	public static UUID getUUID(String playerName) {
		if (playerName == null || playerName.trim().isEmpty()) return null;
		
		String name = playerName.toLowerCase();
		
		// Check cache first
		UUID cached = nameToUuidCache.get(name);
		if (cached != null) return cached;
		
		// Check online players
		Player online = Bukkit.getPlayerExact(playerName);
		if (online != null) {
			UUID uuid = online.getUniqueId();
			cacheUUID(name, uuid);
			return uuid;
		}
		
		// Check offline players
		@SuppressWarnings("deprecation")
		OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
		if (offline != null && offline.hasPlayedBefore()) {
			UUID uuid = offline.getUniqueId();
			cacheUUID(name, uuid);
			return uuid;
		}
		
		// Fetch from Mojang API
		return fetchUUIDFromMojang(playerName);
	}

	/**
	 * Get UUID synchronously (blocks thread - use carefully).
	 * @param playerName Player name
	 * @return UUID or null
	 */
	public static UUID getUUIDSync(String playerName) {
		return getUUID(playerName);
	}

	/**
	 * Get UUID asynchronously.
	 * @param playerName Player name
	 * @param callback Callback with result
	 */
	public static void getUUIDAsync(String playerName, UUIDCallback callback) {
		if (callback == null) return;
		
		JavaUtilities.runAsync(() -> {
			UUID uuid = getUUID(playerName);
			callback.onResult(uuid);
		});
	}

	/**
	 * Get username for a UUID.
	 * @param uuid Player UUID
	 * @return Username or null
	 */
	public static String getUsername(UUID uuid) {
		if (uuid == null) return null;
		
		// Check cache
		String cached = uuidToNameCache.get(uuid);
		if (cached != null) return cached;
		
		// Check online players
		Player online = Bukkit.getPlayer(uuid);
		if (online != null) {
			String name = online.getName();
			cacheName(uuid, name);
			return name;
		}
		
		// Check offline players
		OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
		if (offline != null && offline.hasPlayedBefore()) {
			String name = offline.getName();
			if (name != null) {
				cacheName(uuid, name);
				return name;
			}
		}
		
		// Fetch from Mojang API
		return fetchUsernameFromMojang(uuid);
	}

	/**
	 * Get username asynchronously.
	 * @param uuid Player UUID
	 * @param callback Callback with result
	 */
	public static void getUsernameAsync(UUID uuid, UsernameCallback callback) {
		if (callback == null) return;
		
		JavaUtilities.runAsync(() -> {
			String name = getUsername(uuid);
			callback.onResult(name);
		});
	}

	/**
	 * Check if a string is a valid UUID format.
	 * @param uuidString UUID string to check
	 * @return true if valid UUID format
	 */
	public static boolean isValidUUID(String uuidString) {
		if (uuidString == null || uuidString.trim().isEmpty()) return false;
		
		try {
			UUID.fromString(uuidString);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Parse UUID from string safely.
	 * @param uuidString UUID string
	 * @return UUID or null if invalid
	 */
	public static UUID parseUUID(String uuidString) {
		if (!isValidUUID(uuidString)) return null;
		
		try {
			return UUID.fromString(uuidString);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Format UUID with dashes (standard format).
	 * @param uuid UUID to format
	 * @return Formatted UUID string
	 */
	public static String formatUUID(UUID uuid) {
		return uuid != null ? uuid.toString() : null;
	}

	/**
	 * Format UUID without dashes (compact format).
	 * @param uuid UUID to format
	 * @return Compact UUID string
	 */
	public static String formatUUIDCompact(UUID uuid) {
		return uuid != null ? uuid.toString().replace("-", "") : null;
	}

	/**
	 * Parse compact UUID (without dashes) to standard UUID.
	 * @param compactUuid Compact UUID string (32 chars, no dashes)
	 * @return UUID or null if invalid
	 */
	public static UUID parseCompactUUID(String compactUuid) {
		if (compactUuid == null || compactUuid.length() != 32) return null;
		
		try {
			String formatted = compactUuid.substring(0, 8) + "-" +
							   compactUuid.substring(8, 12) + "-" +
							   compactUuid.substring(12, 16) + "-" +
							   compactUuid.substring(16, 20) + "-" +
							   compactUuid.substring(20, 32);
			return UUID.fromString(formatted);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Generate offline UUID for a player name (for offline-mode servers).
	 * @param playerName Player name
	 * @return Offline UUID
	 */
	public static UUID getOfflineUUID(String playerName) {
		if (playerName == null) return null;
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
	}

	/**
	 * Check if a UUID is an offline UUID.
	 * @param uuid UUID to check
	 * @param playerName Player name to verify against
	 * @return true if it's an offline UUID for that name
	 */
	public static boolean isOfflineUUID(UUID uuid, String playerName) {
		if (uuid == null || playerName == null) return false;
		return uuid.equals(getOfflineUUID(playerName));
	}

	/**
	 * Fetch UUID from Mojang API.
	 * @param playerName Player name
	 * @return UUID or null
	 */
	public static UUID fetchUUIDFromMojang(String playerName) {
		if (playerName == null || playerName.trim().isEmpty()) return null;
		
		try {
			URL url = new URL(MOJANG_API_UUID + playerName);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			
			int responseCode = conn.getResponseCode();
			if (responseCode != 200) return null;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(response.toString()).getAsJsonObject();
			String uuidString = json.get("id").getAsString();
			
			UUID uuid = parseCompactUUID(uuidString);
			if (uuid != null) {
				cacheUUID(playerName.toLowerCase(), uuid);
			}
			
			return uuid;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to fetch UUID from Mojang: " + t.getMessage());
			return null;
		}
	}

	/**
	 * Fetch username from Mojang API.
	 * @param uuid Player UUID
	 * @return Username or null
	 */
	public static String fetchUsernameFromMojang(UUID uuid) {
		if (uuid == null) return null;
		
		try {
			String compactUuid = formatUUIDCompact(uuid);
			URL url = new URL(MOJANG_API_NAME + compactUuid);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			
			int responseCode = conn.getResponseCode();
			if (responseCode != 200) return null;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(response.toString()).getAsJsonObject();
			String name = json.get("name").getAsString();
			
			if (name != null) {
				cacheName(uuid, name);
			}
			
			return name;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to fetch username from Mojang: " + t.getMessage());
			return null;
		}
	}

	/**
	 * Cache UUID for a player name.
	 * @param playerName Player name (lowercase)
	 * @param uuid UUID
	 */
	private static void cacheUUID(String playerName, UUID uuid) {
		if (playerName == null || uuid == null) return;
		nameToUuidCache.put(playerName.toLowerCase(), uuid);
		uuidToNameCache.put(uuid, playerName);
	}

	/**
	 * Cache username for a UUID.
	 * @param uuid UUID
	 * @param playerName Player name
	 */
	private static void cacheName(UUID uuid, String playerName) {
		if (uuid == null || playerName == null) return;
		uuidToNameCache.put(uuid, playerName);
		nameToUuidCache.put(playerName.toLowerCase(), uuid);
	}

	/**
	 * Clear entire cache.
	 */
	public static void clearCache() {
		nameToUuidCache.clear();
		uuidToNameCache.clear();
	}

	/**
	 * Remove specific player from cache.
	 * @param playerName Player name
	 */
	public static void removeCacheByName(String playerName) {
		if (playerName == null) return;
		UUID uuid = nameToUuidCache.remove(playerName.toLowerCase());
		if (uuid != null) {
			uuidToNameCache.remove(uuid);
		}
	}

	/**
	 * Remove specific UUID from cache.
	 * @param uuid UUID
	 */
	public static void removeCacheByUUID(UUID uuid) {
		if (uuid == null) return;
		String name = uuidToNameCache.remove(uuid);
		if (name != null) {
			nameToUuidCache.remove(name.toLowerCase());
		}
	}

	/**
	 * Set cache expiry time.
	 * @param milliseconds Expiry time in milliseconds
	 */
	public static void setCacheExpiry(long milliseconds) {
		cacheExpiryMs = milliseconds;
	}

	/**
	 * Get cache size.
	 * @return Number of cached entries
	 */
	public static int getCacheSize() {
		return nameToUuidCache.size();
	}

	/**
	 * Check if UUID is cached.
	 * @param playerName Player name
	 * @return true if cached
	 */
	public static boolean isCached(String playerName) {
		return playerName != null && nameToUuidCache.containsKey(playerName.toLowerCase());
	}

	/**
	 * Check if username is cached for UUID.
	 * @param uuid UUID
	 * @return true if cached
	 */
	public static boolean isCached(UUID uuid) {
		return uuid != null && uuidToNameCache.containsKey(uuid);
	}

	/**
	 * Get UUIDs for multiple players.
	 * @param playerNames Player names
	 * @return Map of name → UUID
	 */
	public static Map<String, UUID> getUUIDs(String... playerNames) {
		Map<String, UUID> result = new HashMap<>();
		if (playerNames == null) return result;
		
		for (String name : playerNames) {
			if (name != null) {
				UUID uuid = getUUID(name);
				if (uuid != null) {
					result.put(name, uuid);
				}
			}
		}
		return result;
	}

	/**
	 * Get usernames for multiple UUIDs.
	 * @param uuids UUIDs
	 * @return Map of UUID → name
	 */
	public static Map<UUID, String> getUsernames(UUID... uuids) {
		Map<UUID, String> result = new HashMap<>();
		if (uuids == null) return result;
		
		for (UUID uuid : uuids) {
			if (uuid != null) {
				String name = getUsername(uuid);
				if (name != null) {
					result.put(uuid, name);
				}
			}
		}
		return result;
	}

	@FunctionalInterface
	public interface UUIDCallback {
		void onResult(UUID uuid);
	}

	@FunctionalInterface
	public interface UsernameCallback {
		void onResult(String username);
	}
}
