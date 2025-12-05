package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers to simplify working with inventories and items.
 * Includes version-aware fallbacks for Material names (1.7.2+).
 */
public final class InventoryHelper {

    private static final Map<String, String[]> MATERIAL_ALIASES = new HashMap<>();
    
    static {
        // Map modern material names to their legacy equivalents
        // Format: modern name -> [1.13+ name, 1.12 name, 1.7-1.8 name]
        MATERIAL_ALIASES.put("OAK_PLANKS", new String[]{"OAK_PLANKS", "WOOD", "WOOD"});
        MATERIAL_ALIASES.put("GLASS_PANE", new String[]{"GLASS_PANE", "THIN_GLASS", "THIN_GLASS"});
        MATERIAL_ALIASES.put("EXPERIENCE_BOTTLE", new String[]{"EXPERIENCE_BOTTLE", "EXP_BOTTLE", "EXP_BOTTLE"});
        MATERIAL_ALIASES.put("BLACK_STAINED_GLASS_PANE", new String[]{"BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:15", "STAINED_GLASS_PANE"});
        MATERIAL_ALIASES.put("PLAYER_HEAD", new String[]{"PLAYER_HEAD", "SKULL_ITEM:3", "SKULL_ITEM"});
    }

	private InventoryHelper() {}

	public static Inventory createInventory(HumanEntity holder, String title, int rows) {
		int size = Math.max(1, Math.min(rows, 6)) * 9;
		return Bukkit.createInventory(holder, size, title == null ? "" : title);
	}

	public static void openInventory(Player player, Inventory inv) {
		if (player == null || inv == null) return;
		player.openInventory(inv);
	}

	/**
	 * Try to give an item to a player. If inventory is full, leftover items will be dropped at player's location.
	 * Returns true if all items were added to inventory (no leftovers), false otherwise.
	 */
	public static boolean giveItem(Player player, ItemStack item) {
		if (player == null || item == null) return false;
		PlayerInventory inv = player.getInventory();
		Map<Integer, ItemStack> leftovers = inv.addItem(item);
		if (leftovers == null || leftovers.isEmpty()) return true;
		// drop leftovers
		for (ItemStack left : leftovers.values()) {
			Location loc = player.getLocation();
			player.getWorld().dropItemNaturally(loc, left);
		}
		return false;
	}

	/** Safe variant that returns leftover ItemStack or null if none. */
	public static ItemStack giveItemAndReturnLeftover(Player player, ItemStack item) {
		if (player == null || item == null) return item;
		PlayerInventory inv = player.getInventory();
		Map<Integer, ItemStack> leftovers = inv.addItem(item);
		if (leftovers == null || leftovers.isEmpty()) return null;
		// combine leftovers into a single stack where possible (simple approach -> return first leftover)
		ItemStack first = leftovers.values().iterator().next();
		// drop the rest
		for (ItemStack left : leftovers.values()) {
			if (left == first) continue;
			player.getWorld().dropItemNaturally(player.getLocation(), left);
		}
		return first;
	}

	public static int findFirstEmptySlot(Inventory inv) {
		if (inv == null) return -1;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack it = inv.getItem(i);
			if (it == null || it.getType() == Material.AIR) return i;
		}
		return -1;
	}

	public static List<Integer> findSlots(Inventory inv, ItemStack sample) {
		List<Integer> out = new ArrayList<>();
		if (inv == null || sample == null) return out;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack it = inv.getItem(i);
			if (it == null) continue;
			if (isSimilar(it, sample)) out.add(i);
		}
		return out;
	}

	public static boolean isSimilar(ItemStack a, ItemStack b) {
		if (a == null || b == null) return false;
		if (a.getType() != b.getType()) return false;
		ItemMeta ma = a.getItemMeta();
		ItemMeta mb = b.getItemMeta();
		if (ma == null && mb == null) return true;
		if (ma == null || mb == null) return false;
		if (ma.hasDisplayName() != mb.hasDisplayName()) return false;
		if (ma.hasDisplayName() && !ma.getDisplayName().equals(mb.getDisplayName())) return false;
		if (ma.hasLore() != mb.hasLore()) return false;
		if (ma.hasLore() && !ma.getLore().equals(mb.getLore())) return false;
		return true;
	}

	public static void setSlot(Inventory inv, int slot, ItemStack item) {
		if (inv == null) return;
		if (slot < 0 || slot >= inv.getSize()) return;
		inv.setItem(slot, item);
	}

	public static void swapSlots(Inventory inv, int a, int b) {
		if (inv == null) return;
		if (a < 0 || a >= inv.getSize()) return;
		if (b < 0 || b >= inv.getSize()) return;
		ItemStack ia = inv.getItem(a);
		ItemStack ib = inv.getItem(b);
		inv.setItem(a, ib);
		inv.setItem(b, ia);
	}

	public static ItemInfo getItemInfo(ItemStack item) {
		if (item == null) return null;
		ItemInfo info = new ItemInfo();
		info.type = item.getType();
		info.amount = item.getAmount();
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			info.displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
			info.lore = meta.hasLore() ? meta.getLore() : null;
		}
		return info;
	}

	public static class ItemInfo {
		public Material type;
		public int amount;
		public String displayName;
		public List<String> lore;

		@Override
		public String toString() {
			return "ItemInfo{" +
					"type=" + type +
					", amount=" + amount +
					", displayName='" + displayName + '\'' +
					", lore=" + lore +
					'}';
		}
	}

	public static String serializeItem(ItemStack item) {
		if (item == null) return null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
			boos.writeObject(item);
			boos.flush();
			return java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ItemStack deserializeItem(String base64) {
		if (base64 == null) return null;
		byte[] data = java.util.Base64.getDecoder().decode(base64);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
			 BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {
			Object o = bois.readObject();
			if (o instanceof ItemStack) return (ItemStack) o;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void fill(Inventory inv, ItemStack item) {
		if (inv == null) return;
		for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, item);
	}

	public static void fillBorder(Inventory inv, ItemStack item) {
		if (inv == null) return;
		int size = inv.getSize();
		int cols = 9;
		int rows = size / cols;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
					inv.setItem(r * cols + c, item);
				}
			}
		}
	}

    public static List<ItemStack> getItems(Inventory inv) {
        List<ItemStack> out = new ArrayList<>();
        if (inv == null) return out;
        for (ItemStack it : inv.getContents()) if (it != null && it.getType() != Material.AIR) out.add(it);
        return out;
    }

    /**
     * Try to get a Material from a name with version-aware fallbacks.
     */
    public static Material getMaterial(String name) {
        if (name == null) return null;
        String raw = name.trim().toUpperCase();
        if (raw.isEmpty()) return null;
        
        // Check aliases first
        String[] aliases = MATERIAL_ALIASES.get(raw);
        if (aliases != null) {
            for (String alias : aliases) {
                try {
                    // Handle data value syntax (e.g., "STAINED_GLASS_PANE:15")
                    if (alias.contains(":")) {
                        String[] parts = alias.split(":");
                        return Material.valueOf(parts[0]);
                    }
                    return Material.valueOf(alias);
                } catch (Throwable ignored) {}
            }
        }
        
        // Direct match
        try { return Material.valueOf(raw); } catch (Throwable ignored) {}
        
        // Normalize separators
        String normal = raw.replace('-', '_').replace(' ', '_').replace('.', '_');
        try { return Material.valueOf(normal); } catch (Throwable ignored) {}
        
        return null;
    }

}