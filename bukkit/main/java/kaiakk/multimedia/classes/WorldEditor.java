package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to simplify block placement, area filling, world management, and coordinate tracking.
 * Examples:
 *   WorldEditor.setBlock(location, "STONE");
 *   WorldEditor.fillArea(world, x1, y1, z1, x2, y2, z2, Material.GLASS);
 *   List<World> worlds = WorldEditor.getAllWorlds();
 *   Map<Player, Location> coords = WorldEditor.getAllPlayerLocations();
 */
public final class WorldEditor {

	private WorldEditor() {}

	public static boolean setBlock(Location loc, Material material) {
		if (loc == null || material == null) return false;
		try {
			Block block = loc.getBlock();
			block.setType(material);
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static boolean setBlock(Location loc, String materialName) {
		Material mat = InventoryHelper.getMaterial(materialName);
		if (mat == null) return false;
		return setBlock(loc, mat);
	}

	public static boolean setBlock(World world, int x, int y, int z, Material material) {
		if (world == null) return false;
		return setBlock(new Location(world, x, y, z), material);
	}

	public static boolean setBlock(World world, int x, int y, int z, String materialName) {
		if (world == null) return false;
		return setBlock(new Location(world, x, y, z), materialName);
	}

	/**
	 * Fill a rectangular area with the specified material.
	 * Coordinates will be normalized (min/max calculated automatically).
	 */
	public static int fillArea(World world, int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
		if (world == null || material == null) return 0;
		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2);
		int maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);
		int count = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (setBlock(world, x, y, z, material)) count++;
				}
			}
		}
		return count;
	}

	public static int fillArea(World world, int x1, int y1, int z1, int x2, int y2, int z2, String materialName) {
		Material mat = InventoryHelper.getMaterial(materialName);
		if (mat == null) return 0;
		return fillArea(world, x1, y1, z1, x2, y2, z2, mat);
	}

	public static int fillArea(Location loc1, Location loc2, Material material) {
		if (loc1 == null || loc2 == null) return 0;
		if (!loc1.getWorld().equals(loc2.getWorld())) return 0;
		return fillArea(loc1.getWorld(),
			loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
			loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(),
			material);
	}

	public static int fillArea(Location loc1, Location loc2, String materialName) {
		Material mat = InventoryHelper.getMaterial(materialName);
		if (mat == null) return 0;
		return fillArea(loc1, loc2, mat);
	}

	public static int fillHollowBox(World world, int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
		if (world == null || material == null) return 0;
		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2);
		int maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);
		int count = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					// Only place on edges
					if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
						if (setBlock(world, x, y, z, material)) count++;
					}
				}
			}
		}
		return count;
	}

	public static int fillHollowBox(World world, int x1, int y1, int z1, int x2, int y2, int z2, String materialName) {
		Material mat = InventoryHelper.getMaterial(materialName);
		if (mat == null) return 0;
		return fillHollowBox(world, x1, y1, z1, x2, y2, z2, mat);
	}

	public static List<World> getAllWorlds() {
		return new ArrayList<>(Bukkit.getWorlds());
	}

	public static World getWorld(String name) {
		if (name == null) return null;
		return Bukkit.getWorld(name);
	}

	public static World getMainWorld() {
		List<World> worlds = Bukkit.getWorlds();
		return worlds.isEmpty() ? null : worlds.get(0);
	}

	public static List<String> getWorldNames() {
		List<String> names = new ArrayList<>();
		for (World w : Bukkit.getWorlds()) {
			if (w != null) names.add(w.getName());
		}
		return names;
	}

	public static Map<Player, Location> getAllPlayerLocations() {
		Map<Player, Location> map = new HashMap<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p != null) map.put(p, p.getLocation());
		}
		return map;
	}

	public static Map<String, Location> getAllPlayerLocationsByName() {
		Map<String, Location> map = new HashMap<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p != null) map.put(p.getName(), p.getLocation());
		}
		return map;
	}

	public static List<Player> getPlayersInWorld(World world) {
		List<Player> list = new ArrayList<>();
		if (world == null) return list;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().equals(world)) list.add(p);
		}
		return list;
	}

	public static List<Player> getPlayersInWorld(String worldName) {
		World world = getWorld(worldName);
		return getPlayersInWorld(world);
	}

	public static int countPlayersInWorld(World world) {
		return getPlayersInWorld(world).size();
	}

	public static Material getBlockType(Location loc) {
		if (loc == null) return null;
		try {
			return loc.getBlock().getType();
		} catch (Throwable t) {
			return null;
		}
	}

	public static Material getBlockType(World world, int x, int y, int z) {
		if (world == null) return null;
		return getBlockType(new Location(world, x, y, z));
	}

	public static boolean isBlockAir(Location loc) {
		Material type = getBlockType(loc);
		return type == null || type == Material.AIR;
	}

	public static boolean isBlockAir(World world, int x, int y, int z) {
		return isBlockAir(new Location(world, x, y, z));
	}

	public static boolean isInRegion(Location loc, int x1, int y1, int z1, int x2, int y2, int z2) {
		if (loc == null) return false;
		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2);
		int maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);
		int bx = loc.getBlockX();
		int by = loc.getBlockY();
		int bz = loc.getBlockZ();
		return bx >= minX && bx <= maxX && by >= minY && by <= maxY && bz >= minZ && bz <= maxZ;
	}

	public static List<Player> getPlayersInRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		List<Player> list = new ArrayList<>();
		if (world == null) return list;
		for (Player p : getPlayersInWorld(world)) {
			if (isInRegion(p.getLocation(), x1, y1, z1, x2, y2, z2)) list.add(p);
		}
		return list;
	}

}
