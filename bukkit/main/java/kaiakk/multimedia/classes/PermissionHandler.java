package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

/**
 * PermissionHandler - Easy permission management without boilerplate.
 * Supports individual and group permissions with simple API.
 */
public final class PermissionHandler {

	private static JavaPlugin plugin;
	private static final Map<String, PermissionAttachment> attachments = new HashMap<>();

	private PermissionHandler() {}

	public static void init(JavaPlugin pluginInstance) {
		plugin = pluginInstance;
	}

	/**
	 * Give a permission to a player.
	 * @param player Target player
	 * @param permission Permission node
	 * @return true if successful
	 */
	public static boolean givePermission(Player player, String permission) {
		return setPermission(player, permission, true);
	}

	/**
	 * Remove a permission from a player.
	 * @param player Target player
	 * @param permission Permission node
	 * @return true if successful
	 */
	public static boolean removePermission(Player player, String permission) {
		return setPermission(player, permission, false);
	}

	/**
	 * Set a permission for a player.
	 * @param player Target player
	 * @param permission Permission node
	 * @param value true to grant, false to revoke
	 * @return true if successful
	 */
	public static boolean setPermission(Player player, String permission, boolean value) {
		if (player == null || permission == null || plugin == null) return false;
		
		try {
			PermissionAttachment attachment = getOrCreateAttachment(player);
			attachment.setPermission(permission, value);
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to set permission: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Check if a player has a permission.
	 * @param player Target player
	 * @param permission Permission node
	 * @return true if player has permission
	 */
	public static boolean hasPermission(Player player, String permission) {
		if (player == null || permission == null) return false;
		return player.hasPermission(permission);
	}

	/**
	 * Check if a player has a permission (with op check).
	 * @param player Target player
	 * @param permission Permission node
	 * @param allowOp If true, ops automatically have permission
	 * @return true if player has permission
	 */
	public static boolean hasPermission(Player player, String permission, boolean allowOp) {
		if (player == null || permission == null) return false;
		if (allowOp && player.isOp()) return true;
		return player.hasPermission(permission);
	}

	/**
	 * Clear all custom permissions from a player.
	 * @param player Target player
	 * @return true if successful
	 */
	public static boolean clearPermissions(Player player) {
		if (player == null) return false;
		
		try {
			String key = player.getUniqueId().toString();
			PermissionAttachment attachment = attachments.remove(key);
			if (attachment != null) {
				attachment.remove();
			}
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to clear permissions: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Set multiple permissions for a player at once.
	 * @param player Target player
	 * @param permissions Map of permission → value
	 * @return true if successful
	 */
	public static boolean setPermissions(Player player, Map<String, Boolean> permissions) {
		if (player == null || permissions == null || permissions.isEmpty()) return false;
		
		try {
			PermissionAttachment attachment = getOrCreateAttachment(player);
			for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
				attachment.setPermission(entry.getKey(), entry.getValue());
			}
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to set permissions: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Give multiple permissions to a player.
	 * @param player Target player
	 * @param permissions Permission nodes to grant
	 * @return true if successful
	 */
	public static boolean givePermissions(Player player, String... permissions) {
		if (player == null || permissions == null || permissions.length == 0) return false;
		
		try {
			PermissionAttachment attachment = getOrCreateAttachment(player);
			for (String perm : permissions) {
				if (perm != null) {
					attachment.setPermission(perm, true);
				}
			}
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to give permissions: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Remove multiple permissions from a player.
	 * @param player Target player
	 * @param permissions Permission nodes to revoke
	 * @return true if successful
	 */
	public static boolean removePermissions(Player player, String... permissions) {
		if (player == null || permissions == null || permissions.length == 0) return false;
		
		try {
			PermissionAttachment attachment = getOrCreateAttachment(player);
			for (String perm : permissions) {
				if (perm != null) {
					attachment.unsetPermission(perm);
				}
			}
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to remove permissions: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Apply a permission group (preset) to a player.
	 * @param player Target player
	 * @param groupPermissions Map of permissions for the group
	 * @param clearExisting If true, clear existing permissions first
	 * @return true if successful
	 */
	public static boolean setGroupPermissions(Player player, Map<String, Boolean> groupPermissions, boolean clearExisting) {
		if (player == null || groupPermissions == null) return false;
		
		if (clearExisting) {
			clearPermissions(player);
		}
		
		return setPermissions(player, groupPermissions);
	}

	/**
	 * Get all effective permissions for a player.
	 * @param player Target player
	 * @return Set of PermissionAttachmentInfo objects
	 */
	public static Set<PermissionAttachmentInfo> getEffectivePermissions(Player player) {
		if (player == null) return Collections.emptySet();
		return player.getEffectivePermissions();
	}

	/**
	 * Get all permission nodes a player has.
	 * @param player Target player
	 * @return List of permission strings
	 */
	public static List<String> getPermissionNodes(Player player) {
		if (player == null) return Collections.emptyList();
		
		List<String> nodes = new ArrayList<>();
		for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
			if (info.getValue()) {
				nodes.add(info.getPermission());
			}
		}
		return nodes;
	}

	/**
	 * Check if a player has any of the given permissions.
	 * @param player Target player
	 * @param permissions Permission nodes to check
	 * @return true if player has at least one permission
	 */
	public static boolean hasAnyPermission(Player player, String... permissions) {
		if (player == null || permissions == null) return false;
		for (String perm : permissions) {
			if (perm != null && player.hasPermission(perm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a player has all of the given permissions.
	 * @param player Target player
	 * @param permissions Permission nodes to check
	 * @return true if player has all permissions
	 */
	public static boolean hasAllPermissions(Player player, String... permissions) {
		if (player == null || permissions == null) return false;
		for (String perm : permissions) {
			if (perm != null && !player.hasPermission(perm)) {
				return false;
			}
		}
		return true;
	}

	private static PermissionAttachment getOrCreateAttachment(Player player) {
		if (plugin == null) {
			throw new IllegalStateException("PermissionHandler not initialized. Call init() first.");
		}
		
		String key = player.getUniqueId().toString();
		PermissionAttachment attachment = attachments.get(key);
		
		if (attachment == null) {
			attachment = player.addAttachment(plugin);
			attachments.put(key, attachment);
		}
		
		return attachment;
	}

	/**
	 * Clean up attachments for offline players (call on plugin disable).
	 */
	public static void cleanup() {
		for (PermissionAttachment attachment : attachments.values()) {
			try {
				attachment.remove();
			} catch (Throwable ignored) {}
		}
		attachments.clear();
	}

	/**
	 * Remove attachment for a specific player when they leave.
	 * @param player Player who left
	 */
	public static void removePlayerAttachment(Player player) {
		if (player == null) return;
		clearPermissions(player);
	}

	/**
	 * Register a new permission node with the server.
	 * @param name Permission name
	 * @param defaultValue Default value (TRUE, FALSE, OP, NOT_OP)
	 * @param description Permission description
	 * @return true if registered successfully
	 */
	public static boolean registerPermission(String name, PermissionDefault defaultValue, String description) {
		if (name == null) return false;
		
		try {
			Permission perm = new Permission(name, description, defaultValue);
			Bukkit.getPluginManager().addPermission(perm);
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to register permission: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Unregister a permission node.
	 * @param name Permission name
	 * @return true if unregistered successfully
	 */
	public static boolean unregisterPermission(String name) {
		if (name == null) return false;
		
		try {
			Permission perm = Bukkit.getPluginManager().getPermission(name);
			if (perm != null) {
				Bukkit.getPluginManager().removePermission(perm);
				return true;
			}
			return false;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to unregister permission: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Check if a permission is registered with the server.
	 * @param name Permission name
	 * @return true if permission exists
	 */
	public static boolean isPermissionRegistered(String name) {
		if (name == null) return false;
		return Bukkit.getPluginManager().getPermission(name) != null;
	}
}
