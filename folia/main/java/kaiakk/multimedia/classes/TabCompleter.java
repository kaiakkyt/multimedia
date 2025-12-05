package kaiakk.multimedia.classes;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Tiny, easy tab-completion helper.
 * Usage examples:
 *  - TabCompleter.register(plugin, "mycmd", sender -> List.of("one","two","three"));
 *  - TabCompleter.register(plugin, "mycmd", (sender, args) -> { ... });
 */
public final class TabCompleter {

	private TabCompleter() {}

	@FunctionalInterface
	public interface ArgsCompleter {
		List<String> complete(CommandSender sender, String[] args);
	}

	/**
	 * Register a completer that receives the sender and current args.
	 */
	public static boolean register(JavaPlugin plugin, String command, ArgsCompleter completer) {
		if (plugin == null || command == null || completer == null) return false;
		PluginCommand cmd = plugin.getCommand(command);
		if (cmd == null) {
			plugin.getLogger().warning("TabCompleter: command '/" + command + "' not found in plugin.yml; registration skipped.");
			return false;
		}

		cmd.setTabCompleter((sender, cmd2, label, args) -> {
			try {
				List<String> out = completer.complete(sender, args);
				return out == null ? Collections.emptyList() : out;
			} catch (Throwable t) {
				plugin.getLogger().severe("Error in tab completer for '/" + command + "': " + t.getMessage());
				t.printStackTrace();
				return Collections.emptyList();
			}
		});
		return true;
	}

	/**
	 * Register a simple supplier that ignores args (useful for static completions).
	 */
	public static boolean register(JavaPlugin plugin, String command, Function<CommandSender, List<String>> supplier) {
		if (supplier == null) return false;
		return register(plugin, command, (sender, args) -> supplier.apply(sender));
	}

	/**
	 * Very small convenience: register using a fixed set of options which will be
	 * auto-filtered by the last argument. Example:
	 *   TabCompleter.register(plugin, "cmd", "start", "stop", "reload");
	 */
	public static boolean register(JavaPlugin plugin, String command, String... options) {
		if (options == null) return register(plugin, command, (ArgsCompleter) (s, a) -> Collections.emptyList());
		return register(plugin, command, simple(options));
	}

	/**
	 * Helper that builds a completer from a fixed list of options and performs
	 * prefix-filtering against the last typed argument.
	 */
	public static ArgsCompleter simple(String... options) {
		List<String> base = options == null ? Collections.emptyList() : java.util.Arrays.asList(options);
		return (sender, args) -> {
			if (args == null || args.length == 0) return base;
			String last = args[args.length - 1].toLowerCase();
			return base.stream().filter(s -> s.toLowerCase().startsWith(last)).collect(java.util.stream.Collectors.toList());
		};
	}

}
