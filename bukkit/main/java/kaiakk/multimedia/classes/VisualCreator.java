package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for creating visual effects: particles, fireworks, titles, action bars, boss bars.
 * Examples:
 *   VisualCreator.spawnParticle(player.getLocation(), "FLAME", 10);
 *   VisualCreator.sendTitle(player, "Welcome!", "Enjoy your stay", 10, 70, 20);
 *   VisualCreator.sendActionBar(player, "Health: 20/20");
 *   BossBar bar = VisualCreator.createBossBar("Boss Fight", BarColor.RED, BarStyle.SOLID);
 */
public final class VisualCreator {

	private VisualCreator() {}

	private static final Map<String, BossBar> activeBossBars = new HashMap<>();

	public static void spawnParticle(Location loc, Particle particle, int count) {
		if (loc == null || particle == null || loc.getWorld() == null) return;
		try {
			loc.getWorld().spawnParticle(particle, loc, count);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to spawn particle " + particle + ": " + t.getMessage());
		}
	}

	public static void spawnParticle(Location loc, String particleName, int count) {
		Particle particle = getParticle(particleName);
		if (particle != null) spawnParticle(loc, particle, count);
	}

	public static void spawnParticle(Location loc, Particle particle, int count, double offsetX, double offsetY, double offsetZ) {
		if (loc == null || particle == null || loc.getWorld() == null) return;
		try {
			loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to spawn particle: " + t.getMessage());
		}
	}

	public static void spawnParticle(Location loc, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (loc == null || particle == null || loc.getWorld() == null) return;
		try {
			loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to spawn particle: " + t.getMessage());
		}
	}

	public static void spawnParticleCircle(Location center, Particle particle, double radius, int points) {
		if (center == null || particle == null) return;
		for (int i = 0; i < points; i++) {
			double angle = 2 * Math.PI * i / points;
			double x = center.getX() + radius * Math.cos(angle);
			double z = center.getZ() + radius * Math.sin(angle);
			Location point = new Location(center.getWorld(), x, center.getY(), z);
			spawnParticle(point, particle, 1);
		}
	}

	public static void spawnParticleLine(Location start, Location end, Particle particle, double spacing) {
		if (start == null || end == null || particle == null) return;
		if (!start.getWorld().equals(end.getWorld())) return;
		double distance = start.distance(end);
		int points = (int) (distance / spacing);
		for (int i = 0; i <= points; i++) {
			double ratio = (double) i / points;
			double x = start.getX() + (end.getX() - start.getX()) * ratio;
			double y = start.getY() + (end.getY() - start.getY()) * ratio;
			double z = start.getZ() + (end.getZ() - start.getZ()) * ratio;
			Location point = new Location(start.getWorld(), x, y, z);
			spawnParticle(point, particle, 1);
		}
	}

	private static Particle getParticle(String name) {
		if (name == null) return null;
		try {
			return Particle.valueOf(name.toUpperCase().replace(" ", "_"));
		} catch (Throwable t) {
			ConsoleLog.warn("Unknown particle: " + name);
			return null;
		}
	}

	public static void spawnFirework(Location loc, FireworkEffect effect) {
		if (loc == null || effect == null || loc.getWorld() == null) return;
		try {
			Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(1);
			fw.setFireworkMeta(meta);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to spawn firework: " + t.getMessage());
		}
	}

	public static void spawnInstantFirework(Location loc, FireworkEffect effect) {
		if (loc == null || effect == null || loc.getWorld() == null) return;
		try {
			Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(0);
			fw.setFireworkMeta(meta);
			// Detonate immediately
			SchedulerHelper.runLater(null, () -> fw.detonate(), 1);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to spawn instant firework: " + t.getMessage());
		}
	}

	public static FireworkEffect.Builder createFireworkEffect() {
		return FireworkEffect.builder();
	}

	public static FireworkEffect simpleFirework(FireworkEffect.Type type, Color color) {
		return FireworkEffect.builder()
			.with(type)
			.withColor(color)
			.build();
	}

	public static FireworkEffect simpleFirework(String typeName, Color color) {
		FireworkEffect.Type type = getFireworkType(typeName);
		if (type == null) type = FireworkEffect.Type.BALL;
		return simpleFirework(type, color);
	}

	private static FireworkEffect.Type getFireworkType(String name) {
		if (name == null) return null;
		try {
			return FireworkEffect.Type.valueOf(name.toUpperCase().replace(" ", "_"));
		} catch (Throwable t) {
			return null;
		}
	}

	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		if (player == null) return;
		try {
			player.sendTitle(
				title == null ? "" : title,
				subtitle == null ? "" : subtitle,
				fadeIn, stay, fadeOut
			);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to send title: " + t.getMessage());
		}
	}

	public static void sendTitle(Player player, String title, String subtitle) {
		sendTitle(player, title, subtitle, 10, 70, 20);
	}

	public static void sendTitle(Player player, String title) {
		sendTitle(player, title, "", 10, 70, 20);
	}

	public static void clearTitle(Player player) {
		if (player == null) return;
		try {
			player.resetTitle();
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to clear title: " + t.getMessage());
		}
	}

	public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			sendTitle(p, title, subtitle, fadeIn, stay, fadeOut);
		}
	}

	public static void broadcastTitle(String title, String subtitle) {
		broadcastTitle(title, subtitle, 10, 70, 20);
	}

	public static void sendActionBar(Player player, String message) {
		if (player == null || message == null) return;
		try {
			// Try modern API first (1.16+)
			try {
				java.lang.reflect.Method method = Player.class.getMethod("sendActionBar", String.class);
				method.invoke(player, message);
				return;
			} catch (NoSuchMethodException ignored) {}
			
			// Fallback for older versions using packets
			String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
			
			Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
			Class<?> chatComponentClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
			Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
			
			java.lang.reflect.Method a = chatSerializerClass.getDeclaredMethod("a", String.class);
			Object chatComponent = a.invoke(null, "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}");
			
			java.lang.reflect.Constructor<?> packetConstructor = packetClass.getConstructor(chatComponentClass, byte.class);
			Object packet = packetConstructor.newInstance(chatComponent, (byte) 2);
			
			Object connection = handle.getClass().getField("playerConnection").get(handle);
			connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet")).invoke(connection, packet);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to send action bar: " + t.getMessage());
		}
	}

	public static void broadcastActionBar(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			sendActionBar(p, message);
		}
	}

	public static BossBar createBossBar(String title, BarColor color, BarStyle style) {
		if (title == null) title = "";
		if (color == null) color = BarColor.WHITE;
		if (style == null) style = BarStyle.SOLID;
		try {
			return Bukkit.createBossBar(title, color, style);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to create boss bar: " + t.getMessage());
			return null;
		}
	}

	public static BossBar createBossBar(String title) {
		return createBossBar(title, BarColor.WHITE, BarStyle.SOLID);
	}

	public static void showBossBar(BossBar bar, Player player) {
		if (bar == null || player == null) return;
		bar.addPlayer(player);
	}

	public static void hideBossBar(BossBar bar, Player player) {
		if (bar == null || player == null) return;
		bar.removePlayer(player);
	}

	public static void showBossBarToAll(BossBar bar) {
		if (bar == null) return;
		for (Player p : Bukkit.getOnlinePlayers()) {
			bar.addPlayer(p);
		}
	}

	public static void hideBossBarFromAll(BossBar bar) {
		if (bar == null) return;
		bar.removeAll();
	}

	public static void setBossBarProgress(BossBar bar, double progress) {
		if (bar == null) return;
		bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
	}

	public static void setBossBarTitle(BossBar bar, String title) {
		if (bar == null) return;
		bar.setTitle(title == null ? "" : title);
	}

	public static void setBossBarColor(BossBar bar, BarColor color) {
		if (bar == null || color == null) return;
		bar.setColor(color);
	}

	public static BossBar getOrCreateBossBar(String id, String title, BarColor color, BarStyle style) {
		if (activeBossBars.containsKey(id)) {
			return activeBossBars.get(id);
		}
		BossBar bar = createBossBar(title, color, style);
		if (bar != null) activeBossBars.put(id, bar);
		return bar;
	}

	public static void removeBossBar(String id) {
		BossBar bar = activeBossBars.remove(id);
		if (bar != null) {
			bar.removeAll();
		}
	}

	public static void removeAllBossBars() {
		for (BossBar bar : activeBossBars.values()) {
			if (bar != null) bar.removeAll();
		}
		activeBossBars.clear();
	}

	public static Color rgb(int r, int g, int b) {
		return Color.fromRGB(
			Math.max(0, Math.min(255, r)),
			Math.max(0, Math.min(255, g)),
			Math.max(0, Math.min(255, b))
		);
	}

	public static Color parseColor(String hex) {
		if (hex == null) return Color.WHITE;
		if (hex.startsWith("#")) hex = hex.substring(1);
		try {
			int rgb = Integer.parseInt(hex, 16);
			return Color.fromRGB(rgb);
		} catch (Throwable t) {
			ConsoleLog.warn("Invalid color hex: " + hex);
			return Color.WHITE;
		}
	}

	public static final Color RED = Color.RED;
	public static final Color GREEN = Color.GREEN;
	public static final Color BLUE = Color.BLUE;
	public static final Color YELLOW = Color.YELLOW;
	public static final Color ORANGE = Color.ORANGE;
	public static final Color PURPLE = Color.PURPLE;
	public static final Color WHITE = Color.WHITE;
	public static final Color BLACK = Color.BLACK;

}
