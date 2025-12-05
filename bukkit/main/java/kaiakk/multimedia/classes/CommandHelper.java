package kaiakk.multimedia.classes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Very small, easy command registration helper for other plugins.
 * Usage:
 *   CommandHelper.register(plugin, "multimedia", (sender, label, args) -> { ... });
 */
public final class CommandHelper {

	private CommandHelper() {}

	@FunctionalInterface
	public interface SimpleCommand {
		boolean handle(CommandSender sender, String label, String[] args);
	}

	/**
	 * Register a simple command handler for a command declared in plugin.yml.
	 * Returns true if registration succeeded.
	 */
	public static boolean register(JavaPlugin plugin, String name, SimpleCommand handler) {
		if (plugin == null || name == null || handler == null) return false;
		PluginCommand cmd = plugin.getCommand(name);
		if (cmd == null) {
			plugin.getLogger().warning("Command '/" + name + "' not found in plugin.yml; registration skipped.");
			return false;
		}

		cmd.setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				try {
					return handler.handle(sender, label, args);
				} catch (Throwable t) {
					plugin.getLogger().severe("Error executing command '/" + name + "': " + t.getMessage());
					t.printStackTrace();
					return false;
				}
			}
		});
		return true;
	}

}
