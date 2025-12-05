package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * CommandExecuter - Execute commands easily as console or player, with silent mode.
 * Reduces boilerplate for command dispatching and output control.
 */
public final class CommandExecuter {

	private static final Logger serverLogger = Bukkit.getLogger();
	private static final List<String> commandHistory = new ArrayList<>();
	private static boolean trackHistory = false;

	private CommandExecuter() {}

	/**
	 * Run a command as console.
	 * @param command Command to execute (without leading /)
	 * @return true if command was dispatched successfully
	 */
	public static boolean runAsConsole(String command) {
		if (command == null || command.trim().isEmpty()) return false;
		
		try {
			ConsoleCommandSender console = Bukkit.getConsoleSender();
			String cleanCommand = command.startsWith("/") ? command.substring(1) : command;
			
			if (trackHistory) {
				commandHistory.add("CONSOLE: " + cleanCommand);
			}
			
			return Bukkit.dispatchCommand(console, cleanCommand);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to run console command: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Run a command as a specific player.
	 * @param player Player to execute command as
	 * @param command Command to execute (without leading /)
	 * @return true if command was dispatched successfully
	 */
	public static boolean runAsPlayer(Player player, String command) {
		if (player == null || command == null || command.trim().isEmpty()) return false;
		
		try {
			String cleanCommand = command.startsWith("/") ? command.substring(1) : command;
			
			if (trackHistory) {
				commandHistory.add(player.getName() + ": " + cleanCommand);
			}
			
			return Bukkit.dispatchCommand(player, cleanCommand);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to run player command: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Run a command silently (no console output).
	 * @param sender Command sender (console or player)
	 * @param command Command to execute (without leading /)
	 * @return true if command was dispatched successfully
	 */
	public static boolean runSilently(CommandSender sender, String command) {
		if (sender == null || command == null || command.trim().isEmpty()) return false;
		
		SilentFilter filter = new SilentFilter();
		
		try {
			serverLogger.setFilter(filter);
			String cleanCommand = command.startsWith("/") ? command.substring(1) : command;
			
			if (trackHistory) {
				String senderName = sender instanceof Player ? ((Player) sender).getName() : "CONSOLE";
				commandHistory.add(senderName + " (silent): " + cleanCommand);
			}
			
			return Bukkit.dispatchCommand(sender, cleanCommand);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to run silent command: " + t.getMessage());
			return false;
		} finally {
			serverLogger.setFilter(null);
		}
	}

	/**
	 * Run a console command silently.
	 * @param command Command to execute (without leading /)
	 * @return true if command was dispatched successfully
	 */
	public static boolean runConsoleSilently(String command) {
		return runSilently(Bukkit.getConsoleSender(), command);
	}

	/**
	 * Run a player command silently.
	 * @param player Player to execute command as
	 * @param command Command to execute (without leading /)
	 * @return true if command was dispatched successfully
	 */
	public static boolean runPlayerSilently(Player player, String command) {
		if (player == null) return false;
		return runSilently(player, command);
	}

	/**
	 * Check if a command exists/is registered.
	 * @param commandName Command name (without leading /)
	 * @return true if command exists
	 */
	public static boolean commandExists(String commandName) {
		if (commandName == null || commandName.trim().isEmpty()) return false;
		
		String cleanCommand = commandName.startsWith("/") ? commandName.substring(1) : commandName;
		String[] parts = cleanCommand.split(" ");
		String baseName = parts[0].toLowerCase();
		
		try {
			return Bukkit.getPluginCommand(baseName) != null || 
				   Bukkit.getCommandAliases().containsKey(baseName);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Check if a player can execute a command (permission check).
	 * @param player Player to check
	 * @param commandName Command name
	 * @return true if player has permission
	 */
	public static boolean canExecute(Player player, String commandName) {
		if (player == null || commandName == null) return false;
		
		String cleanCommand = commandName.startsWith("/") ? commandName.substring(1) : commandName;
		String baseName = cleanCommand.split(" ")[0].toLowerCase();
		
		// Check if command exists first
		if (!commandExists(baseName)) return false;
		
		// Check permission
		org.bukkit.command.Command cmd = Bukkit.getPluginCommand(baseName);
		if (cmd != null && cmd.getPermission() != null) {
			return player.hasPermission(cmd.getPermission());
		}
		
		return true; // No specific permission required
	}

	/**
	 * Execute multiple commands as console.
	 * @param commands Commands to execute
	 * @return Number of successfully executed commands
	 */
	public static int runMultipleAsConsole(String... commands) {
		if (commands == null || commands.length == 0) return 0;
		
		int success = 0;
		for (String cmd : commands) {
			if (cmd != null && runAsConsole(cmd)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * Execute multiple commands as a player.
	 * @param player Player to execute commands as
	 * @param commands Commands to execute
	 * @return Number of successfully executed commands
	 */
	public static int runMultipleAsPlayer(Player player, String... commands) {
		if (player == null || commands == null || commands.length == 0) return 0;
		
		int success = 0;
		for (String cmd : commands) {
			if (cmd != null && runAsPlayer(player, cmd)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * Execute multiple commands silently.
	 * @param sender Command sender
	 * @param commands Commands to execute
	 * @return Number of successfully executed commands
	 */
	public static int runMultipleSilently(CommandSender sender, String... commands) {
		if (sender == null || commands == null || commands.length == 0) return 0;
		
		int success = 0;
		for (String cmd : commands) {
			if (cmd != null && runSilently(sender, cmd)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * Enable command history tracking.
	 * @param enabled true to enable tracking
	 */
	public static void setHistoryTracking(boolean enabled) {
		trackHistory = enabled;
	}

	/**
	 * Check if command history tracking is enabled.
	 * @return true if tracking is enabled
	 */
	public static boolean isHistoryTracking() {
		return trackHistory;
	}

	/**
	 * Get command history.
	 * @return List of executed commands
	 */
	public static List<String> getCommandHistory() {
		return new ArrayList<>(commandHistory);
	}

	/**
	 * Clear command history.
	 */
	public static void clearHistory() {
		commandHistory.clear();
	}

	/**
	 * Get the last N commands from history.
	 * @param count Number of commands to retrieve
	 * @return List of last N commands
	 */
	public static List<String> getLastCommands(int count) {
		if (count <= 0 || commandHistory.isEmpty()) return new ArrayList<>();
		
		int size = commandHistory.size();
		int fromIndex = Math.max(0, size - count);
		return new ArrayList<>(commandHistory.subList(fromIndex, size));
	}

	/**
	 * Dispatch a command with a custom sender.
	 * @param sender Command sender
	 * @param command Command to execute
	 * @return true if successful
	 */
	public static boolean dispatchCommand(CommandSender sender, String command) {
		if (sender == null || command == null || command.trim().isEmpty()) return false;
		
		try {
			String cleanCommand = command.startsWith("/") ? command.substring(1) : command;
			return Bukkit.dispatchCommand(sender, cleanCommand);
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to dispatch command: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Get all registered command names.
	 * @return List of command names
	 */
	public static List<String> getAllCommands() {
		List<String> commands = new ArrayList<>();
		try {
			commands.addAll(Bukkit.getCommandAliases().keySet());
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to get command list: " + t.getMessage());
		}
		return commands;
	}

	/**
	 * Get command aliases for a specific command.
	 * @param commandName Base command name
	 * @return List of aliases
	 */
	public static List<String> getCommandAliases(String commandName) {
		if (commandName == null) return new ArrayList<>();
		
		try {
			org.bukkit.command.Command cmd = Bukkit.getPluginCommand(commandName);
			if (cmd != null) {
				return cmd.getAliases();
			}
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to get command aliases: " + t.getMessage());
		}
		
		return new ArrayList<>();
	}

	private static class SilentFilter implements Filter {
		@Override
		public boolean isLoggable(LogRecord record) {
			// Suppress all log messages during silent command execution
			return false;
		}
	}
}
