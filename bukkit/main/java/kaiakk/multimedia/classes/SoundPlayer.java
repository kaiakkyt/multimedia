package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple helper to play sounds easily with version-aware fallbacks.
 * Supports Minecraft 1.7.2+ by using sound name mappings and fallbacks.
 * Examples:
 *   SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP");
 *   SoundPlayer.playAt(location, "ENTITY_EXPERIENCE_ORB_PICKUP", 1f, 1f);
 *   SoundPlayer.playFromConfig(plugin, player, "sounds.join");
 */
public final class SoundPlayer {

	private SoundPlayer() {}

	private static final Map<String, String[]> SOUND_ALIASES = new HashMap<>();
	
	static {
		// Map modern sound names to their legacy equivalents (1.7-1.8 era)
		// Format: modern name -> [1.9+ name, 1.8 name, 1.7 name, fallback]
		SOUND_ALIASES.put("ENTITY_PLAYER_LEVELUP", new String[]{"ENTITY_PLAYER_LEVELUP", "LEVEL_UP", "LEVEL_UP", "ORB_PICKUP"});
		SOUND_ALIASES.put("ENTITY_EXPERIENCE_ORB_PICKUP", new String[]{"ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP", "ORB_PICKUP", "CLICK"});
		SOUND_ALIASES.put("UI_BUTTON_CLICK", new String[]{"UI_BUTTON_CLICK", "CLICK", "CLICK", "WOOD_CLICK"});
		SOUND_ALIASES.put("BLOCK_NOTE_BLOCK_PLING", new String[]{"BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING", "NOTE_PLING", "NOTE_PIANO"});
		SOUND_ALIASES.put("ENTITY_VILLAGER_YES", new String[]{"ENTITY_VILLAGER_YES", "VILLAGER_YES", "VILLAGER_YES", "VILLAGER_IDLE"});
		SOUND_ALIASES.put("ENTITY_VILLAGER_NO", new String[]{"ENTITY_VILLAGER_NO", "VILLAGER_NO", "VILLAGER_NO", "VILLAGER_IDLE"});
		SOUND_ALIASES.put("BLOCK_ANVIL_USE", new String[]{"BLOCK_ANVIL_USE", "ANVIL_USE", "ANVIL_USE", "ANVIL_LAND"});
		SOUND_ALIASES.put("ENTITY_ITEM_PICKUP", new String[]{"ENTITY_ITEM_PICKUP", "ITEM_PICKUP", "ITEM_PICKUP", "ITEM_PICKUP"});
		SOUND_ALIASES.put("BLOCK_CHEST_OPEN", new String[]{"BLOCK_CHEST_OPEN", "CHEST_OPEN", "CHEST_OPEN", "CHEST_OPEN"});
		SOUND_ALIASES.put("BLOCK_CHEST_CLOSE", new String[]{"BLOCK_CHEST_CLOSE", "CHEST_CLOSE", "CHEST_CLOSE", "CHEST_CLOSE"});
	}

	/** Try to resolve a Sound from a user-friendly name with version-aware fallbacks. */
	public static Sound getSound(String name) {
		if (name == null) return null;
		String raw = name.trim();
		if (raw.isEmpty()) return null;
		
		// Check if we have version-aware aliases for this sound
		String[] aliases = SOUND_ALIASES.get(raw.toUpperCase());
		if (aliases != null) {
			for (String alias : aliases) {
				try { return Sound.valueOf(alias); } catch (Throwable ignored) {}
			}
		}
		
		// direct match
		try { return Sound.valueOf(raw); } catch (Throwable ignored) {}
		// normalize common separators
		String normal = raw.toUpperCase().replace('-', '_').replace(' ', '_').replace('.', '_');
		try { return Sound.valueOf(normal); } catch (Throwable ignored) {}
		
		// Check aliases with normalized name
		aliases = SOUND_ALIASES.get(normal);
		if (aliases != null) {
			for (String alias : aliases) {
				try { return Sound.valueOf(alias); } catch (Throwable ignored) {}
			}
		}
		
		// try fuzzy contains match
		String needle = normal.replace("_", "");
		for (Sound s : Sound.values()) {
			String cand = s.name().replace("_", "");
			if (cand.contains(needle) || needle.contains(cand)) return s;
		}
		return null;
	}

	public static void play(Player player, Sound sound) {
		play(player, sound, 1.0f, 1.0f);
	}

	public static void play(Player player, Sound sound, float volume, float pitch) {
		if (player == null || sound == null) return;
		try {
			player.playSound(player.getLocation(), sound, volume, pitch);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void play(Player player, String soundName) {
		play(player, soundName, 1.0f, 1.0f);
	}

	public static void play(Player player, String soundName, float volume, float pitch) {
		Sound s = getSound(soundName);
		if (s == null) return;
		play(player, s, volume, pitch);
	}

	public static void playAt(Location loc, Sound sound, float volume, float pitch) {
		if (loc == null || sound == null) return;
		try {
			loc.getWorld().playSound(loc, sound, volume, pitch);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void playAt(Location loc, String soundName, float volume, float pitch) {
		Sound s = getSound(soundName);
		if (s == null) return;
		playAt(loc, s, volume, pitch);
	}

	public static void broadcast(String soundName, float volume, float pitch) {
		Sound s = getSound(soundName);
		if (s == null) return;
		for (Player p : Bukkit.getOnlinePlayers()) play(p, s, volume, pitch);
	}

	/**
	 * Play a configured sound defined under a config path. Expected entries:
	 *   path.name: SOUND_NAME
	 *   path.volume: 1.0
	 *   path.pitch: 1.0
	 */
	public static void playFromConfig(JavaPlugin plugin, Player player, String configPath) {
		if (plugin == null || player == null || configPath == null) return;
		try {
			String name = ConfigHelp.getString(configPath + ".name", null);
			if (name == null) return;
			float vol = (float) ConfigHelp.getDouble(configPath + ".volume", 1.0);
			float pitch = (float) ConfigHelp.getDouble(configPath + ".pitch", 1.0);
			play(player, name, vol, pitch);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static List<String> availableSounds() {
		List<String> out = new ArrayList<>();
		for (Sound s : Sound.values()) out.add(s.name());
		return out;
	}

}
