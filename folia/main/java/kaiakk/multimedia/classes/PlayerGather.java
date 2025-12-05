package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility to easily gather online/offline players, manage inventories, and teleport players.
 * Examples:
 *   List<Player> online = PlayerGather.getOnlinePlayers();
 *   OfflinePlayer offline = PlayerGather.getOfflinePlayer("PlayerName");
 *   PlayerGather.teleport(player, world, x, y, z);
 *   PlayerGather.clearInventory(player);
 */
public final class PlayerGather {

	private PlayerGather() {}

	public static List<Player> getOnlinePlayers() {
		return new ArrayList<>(Bukkit.getOnlinePlayers());
	}

	public static Player getOnlinePlayer(String name) {
		if (name == null) return null;
		return Bukkit.getPlayer(name);
	}

	public static Player getOnlinePlayer(UUID uuid) {
		if (uuid == null) return null;
		return Bukkit.getPlayer(uuid);
	}

	public static int getOnlinePlayerCount() {
		return Bukkit.getOnlinePlayers().size();
	}

	public static List<String> getOnlinePlayerNames() {
		return getOnlinePlayers().stream()
			.map(Player::getName)
			.collect(Collectors.toList());
	}

	public static OfflinePlayer getOfflinePlayer(String name) {
		if (name == null) return null;
		@SuppressWarnings("deprecation")
		OfflinePlayer op = Bukkit.getOfflinePlayer(name);
		return op;
	}

	public static OfflinePlayer getOfflinePlayer(UUID uuid) {
		if (uuid == null) return null;
		return Bukkit.getOfflinePlayer(uuid);
	}

	public static OfflinePlayer[] getAllOfflinePlayers() {
		return Bukkit.getOfflinePlayers();
	}

	public static List<OfflinePlayer> getAllOfflinePlayersList() {
		List<OfflinePlayer> list = new ArrayList<>();
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			if (op != null) list.add(op);
		}
		return list;
	}

	public static boolean hasPlayedBefore(String name) {
		OfflinePlayer op = getOfflinePlayer(name);
		return op != null && op.hasPlayedBefore();
	}

	public static List<Player> findOnlinePlayersStartingWith(String prefix) {
		if (prefix == null) return new ArrayList<>();
		String lower = prefix.toLowerCase();
		return getOnlinePlayers().stream()
			.filter(p -> p.getName().toLowerCase().startsWith(lower))
			.collect(Collectors.toList());
	}

	public static List<Player> findOnlinePlayersContaining(String substring) {
		if (substring == null) return new ArrayList<>();
		String lower = substring.toLowerCase();
		return getOnlinePlayers().stream()
			.filter(p -> p.getName().toLowerCase().contains(lower))
			.collect(Collectors.toList());
	}

	public static void clearInventory(Player player) {
		if (player == null) return;
		player.getInventory().clear();
	}

	public static void clearInventoryAndArmor(Player player) {
		if (player == null) return;
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
	}

	public static void giveItems(Player player, ItemStack... items) {
		if (player == null || items == null) return;
		for (ItemStack item : items) {
			if (item != null) InventoryHelper.giveItem(player, item);
		}
	}

	public static void giveItems(Player player, List<ItemStack> items) {
		if (player == null || items == null) return;
		for (ItemStack item : items) {
			if (item != null) InventoryHelper.giveItem(player, item);
		}
	}

	public static Inventory getInventory(Player player) {
		return player == null ? null : player.getInventory();
	}

	public static boolean hasItem(Player player, ItemStack item) {
		if (player == null || item == null) return false;
		return player.getInventory().contains(item.getType(), item.getAmount());
	}

	public static int countItem(Player player, ItemStack sample) {
		if (player == null || sample == null) return 0;
		int count = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && InventoryHelper.isSimilar(item, sample)) {
				count += item.getAmount();
			}
		}
		return count;
	}

	public static boolean removeItem(Player player, ItemStack item) {
		if (player == null || item == null) return false;
		return player.getInventory().removeItem(item).isEmpty();
	}

	public static boolean teleport(Player player, Location location) {
		if (player == null || location == null) return false;
		try {
			return player.teleport(location);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static boolean teleport(Player player, World world, int x, int y, int z) {
		if (world == null) return false;
		return teleport(player, new Location(world, x + 0.5, y, z + 0.5));
	}

	public static boolean teleport(Player player, World world, double x, double y, double z) {
		if (world == null) return false;
		return teleport(player, new Location(world, x, y, z));
	}

	public static boolean teleportToPlayer(Player player, Player target) {
		if (player == null || target == null) return false;
		return teleport(player, target.getLocation());
	}

	public static boolean teleportToSpawn(Player player, World world) {
		if (player == null || world == null) return false;
		return teleport(player, world.getSpawnLocation());
	}

	public static boolean teleportToWorldSpawn(Player player, String worldName) {
		World world = WorldEditor.getWorld(worldName);
		if (world == null) return false;
		return teleportToSpawn(player, world);
	}

	public static int teleportAll(List<Player> players, Location location) {
		if (players == null || location == null) return 0;
		int count = 0;
		for (Player p : players) {
			if (teleport(p, location)) count++;
		}
		return count;
	}

	public static int teleportAllOnline(Location location) {
		return teleportAll(getOnlinePlayers(), location);
	}

	public static boolean isOnline(String name) {
		return getOnlinePlayer(name) != null;
	}

	public static boolean isOnline(UUID uuid) {
		return getOnlinePlayer(uuid) != null;
	}

	public static boolean isOp(Player player) {
		return player != null && player.isOp();
	}

	public static boolean hasPermission(Player player, String permission) {
		return player != null && permission != null && player.hasPermission(permission);
	}

	public static void setHealth(Player player, double health) {
		if (player == null) return;
		try {
			@SuppressWarnings("deprecation")
			double maxHealth = player.getMaxHealth();
			player.setHealth(Math.max(0, Math.min(health, maxHealth)));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void setFoodLevel(Player player, int level) {
		if (player == null) return;
		player.setFoodLevel(Math.max(0, Math.min(level, 20)));
	}

	public static void heal(Player player) {
		if (player == null) return;
		try {
			@SuppressWarnings("deprecation")
			double maxHealth = player.getMaxHealth();
			player.setHealth(maxHealth);
			player.setFoodLevel(20);
			player.setSaturation(20f);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void setGameMode(Player player, org.bukkit.GameMode mode) {
		if (player == null || mode == null) return;
		player.setGameMode(mode);
	}

	public static void setFlyMode(Player player, boolean canFly, boolean isFlying) {
		if (player == null) return;
		player.setAllowFlight(canFly);
		if (canFly) player.setFlying(isFlying);
	}

}
