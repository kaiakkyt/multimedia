package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.*;

/**
 * ProxyListener - BungeeCord/Velocity proxy integration helpers.
 * Handles plugin messaging, server detection, and proxy communication.
 */
public final class ProxyListener implements PluginMessageListener {

	private static JavaPlugin plugin;
	private static ProxyListener instance;
	private static boolean isBehindProxy = false;
	private static String currentServer = "unknown";
	private static final Map<String, List<String>> serverLists = new HashMap<>();
	private static final Map<UUID, ProxyPlayerInfo> playerProxyInfo = new HashMap<>();
	
	private ProxyListener() {}

	/**
	 * Initialize the ProxyListener.
	 * @param pluginInstance Plugin instance
	 */
	public static void init(JavaPlugin pluginInstance) {
		if (plugin != null) return; // Already initialized
		
		plugin = pluginInstance;
		instance = new ProxyListener();
		
		// Register BungeeCord channel
		try {
			Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", instance);
			ConsoleLog.info("ProxyListener initialized with BungeeCord messaging");
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to register BungeeCord channel: " + t.getMessage());
		}
		
		// Auto-detect if behind proxy
		detectProxy();
	}

	/**
	 * Cleanup on plugin disable.
	 */
	public static void cleanup() {
		if (plugin != null) {
			try {
				Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
				Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, "BungeeCord", instance);
			} catch (Throwable ignored) {}
		}
		playerProxyInfo.clear();
		serverLists.clear();
	}

	/**
	 * Detect if server is behind a BungeeCord proxy.
	 */
	private static void detectProxy() {
		try {
			// Check if BungeeCord is enabled in spigot.yml
			if (Bukkit.spigot() != null) {
				isBehindProxy = true;
				ConsoleLog.info("Detected server is behind a proxy");
			}
		} catch (Throwable t) {
			isBehindProxy = false;
		}
		
		// Try to get server name
		requestServerName();
	}

	/**
	 * Check if server is behind a proxy.
	 * @return true if behind BungeeCord/Velocity
	 */
	public static boolean isBehindProxy() {
		return isBehindProxy;
	}

	/**
	 * Get current server name.
	 * @return Server name or "unknown"
	 */
	public static String getCurrentServer() {
		return currentServer;
	}

	/**
	 * Send a plugin message to the proxy.
	 * @param player Player to send through (must be online)
	 * @param subchannel Subchannel name
	 * @param data Data to send
	 * @return true if sent successfully
	 */
	public static boolean sendPluginMessage(Player player, String subchannel, byte[] data) {
		if (plugin == null || player == null || subchannel == null) return false;
		
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(subchannel);
			if (data != null && data.length > 0) {
				out.write(data);
			}
			
			player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to send plugin message: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Send a simple subchannel message.
	 * @param player Player to send through
	 * @param subchannel Subchannel name
	 * @return true if sent successfully
	 */
	public static boolean sendSubchannel(Player player, String subchannel) {
		if (plugin == null || player == null || subchannel == null) return false;
		
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF(subchannel);
			
			player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to send subchannel: " + t.getMessage());
			return false;
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) return;
		
		try {
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			
			handleSubchannel(subchannel, in, player);
		} catch (Throwable t) {
			ConsoleLog.warn("Error receiving plugin message: " + t.getMessage());
		}
	}

	private static void handleSubchannel(String subchannel, ByteArrayDataInput in, Player player) {
		try {
			switch (subchannel) {
				case "GetServer":
					currentServer = in.readUTF();
					ConsoleLog.info("Current server: " + currentServer);
					break;
					
				case "GetServers":
					String[] servers = in.readUTF().split(", ");
					serverLists.put("all", Arrays.asList(servers));
					break;
					
				case "PlayerCount":
					String server = in.readUTF();
					int count = in.readInt();
					ConsoleLog.info("Player count on " + server + ": " + count);
					break;
					
				case "IP":
					String ip = in.readUTF();
					int port = in.readInt();
					if (player != null) {
						ProxyPlayerInfo info = getOrCreatePlayerInfo(player);
						info.setRealIp(ip);
						info.setPort(port);
					}
					break;
					
				case "UUID":
					String uuid = in.readUTF();
					ConsoleLog.info("Player UUID: " + uuid);
					break;
					
				case "UUIDOther":
					String playerName = in.readUTF();
					String playerUuid = in.readUTF();
					ConsoleLog.info("UUID for " + playerName + ": " + playerUuid);
					break;
					
				case "ServerIP":
					String serverName = in.readUTF();
					String serverIp = in.readUTF();
					int serverPort = in.readShort();
					ConsoleLog.info(serverName + " address: " + serverIp + ":" + serverPort);
					break;
			}
		} catch (Throwable t) {
			ConsoleLog.warn("Error handling subchannel " + subchannel + ": " + t.getMessage());
		}
	}

	/**
	 * Connect a player to another server.
	 * @param player Player to connect
	 * @param serverName Target server name
	 * @return true if request sent
	 */
	public static boolean connectPlayerToServer(Player player, String serverName) {
		if (plugin == null || player == null || serverName == null) return false;
		
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(serverName);
			
			player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to connect player to server: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Request server name from proxy.
	 */
	public static void requestServerName() {
		Player player = getAnyOnlinePlayer();
		if (player != null) {
			sendSubchannel(player, "GetServer");
		}
	}

	/**
	 * Request list of all servers from proxy.
	 */
	public static void requestServerList() {
		Player player = getAnyOnlinePlayer();
		if (player != null) {
			sendSubchannel(player, "GetServers");
		}
	}

	/**
	 * Request player count on a server.
	 * @param serverName Server name ("ALL" for network total)
	 */
	public static void requestPlayerCount(String serverName) {
		Player player = getAnyOnlinePlayer();
		if (player != null && serverName != null) {
			try {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("PlayerCount");
				out.writeUTF(serverName);
				
				player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			} catch (Throwable t) {
				ConsoleLog.warn("Failed to request player count: " + t.getMessage());
			}
		}
	}

	/**
	 * Request real IP address of a player.
	 * @param player Target player
	 */
	public static void requestPlayerIP(Player player) {
		if (player != null) {
			sendSubchannel(player, "IP");
		}
	}

	/**
	 * Get list of all servers (must be requested first).
	 * @return List of server names
	 */
	public static List<String> getServerList() {
		return serverLists.getOrDefault("all", new ArrayList<>());
	}

	/**
	 * Get proxy information for a player.
	 * @param player Target player
	 * @return ProxyPlayerInfo object
	 */
	public static ProxyPlayerInfo getPlayerInfo(Player player) {
		if (player == null) return null;
		return playerProxyInfo.get(player.getUniqueId());
	}

	private static ProxyPlayerInfo getOrCreatePlayerInfo(Player player) {
		UUID uuid = player.getUniqueId();
		ProxyPlayerInfo info = playerProxyInfo.get(uuid);
		if (info == null) {
			info = new ProxyPlayerInfo(player);
			playerProxyInfo.put(uuid, info);
		}
		return info;
	}

	/**
	 * Remove player info when they leave.
	 * @param player Player who left
	 */
	public static void removePlayerInfo(Player player) {
		if (player != null) {
			playerProxyInfo.remove(player.getUniqueId());
		}
	}

	private static Player getAnyOnlinePlayer() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		return players.isEmpty() ? null : players.iterator().next();
	}

	public static class ProxyPlayerInfo {
		private final Player player;
		private String realIp;
		private int port;
		private String currentServer;

		public ProxyPlayerInfo(Player player) {
			this.player = player;
		}

		public Player getPlayer() { return player; }
		public String getRealIp() { return realIp; }
		public int getPort() { return port; }
		public String getCurrentServer() { return currentServer; }

		void setRealIp(String ip) { this.realIp = ip; }
		void setPort(int port) { this.port = port; }
		void setCurrentServer(String server) { this.currentServer = server; }

		@Override
		public String toString() {
			return String.format("ProxyPlayerInfo[player=%s, ip=%s:%d, server=%s]",
				player.getName(), realIp, port, currentServer);
		}
	}
}
