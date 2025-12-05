package kaiakk.multimedia;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import kaiakk.multimedia.classes.*;

public final class Multimedia extends JavaPlugin {

    private void send(CommandSender sender, String text) {
        if (text == null) return;
        if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
            sender.sendMessage(ColorConverter.stripColor(text));
        } else {
            sender.sendMessage(text);
        }
    }

    @Override
    public void onEnable() {
        ConsoleLog.init(this);
        SchedulerHelper.setPlugin(this); // Initialize SchedulerHelper for Folia compatibility
        // Optional: Set log level (INFO shows everything, WARNING hides info, ERROR only shows errors)
        // ConsoleLog.setLevel(ConsoleLog.LogLevel.INFO);
        
        ConfigHelp.init(this);
        
        java.util.Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("general.enabled", true);
        defaults.put("general.enable-heartbeat", false);
        defaults.put("messages.welcome", "Welcome to Multimedia!");
        defaults.put("messages.unknown-subcommand", "Unknown subcommand. Usage: /multimedia [version]");
        defaults.put("logging.level", "INFO");
        ConfigHelp.ensureDefaults(defaults);
        
        // Set log level from config
        String logLevelStr = ConfigHelp.getString("logging.level", "INFO");
        try {
            ConsoleLog.setLevel(ConsoleLog.LogLevel.valueOf(logLevelStr.toUpperCase()));
        } catch (Exception e) {
            ConsoleLog.warn("Invalid log level in config: " + logLevelStr + ", using INFO");
        }
        
        ConsoleLog.info("Welcome to Multimedia!");
        ConsoleLog.info("Loading library and classes..");
        ConsoleLog.info("Server type: " + FoliaChecker.getServerType());

        if (!CommandHelper.register(this, "multimedia", (sender, label, args) -> {
            if (args == null || args.length == 0) {
                send(sender, "Multimedia v" + getDescription().getVersion());
                send(sender, "Platform: " + VersionDetector.detectPlatform().name());
                send(sender, "Minecraft: " + VersionDetector.getMinecraftVersion());
                send(sender, "Server Type: " + FoliaChecker.getServerType());
                return true;
            }

            if ("version".equalsIgnoreCase(args[0])) {
                send(sender, "Version: " + getDescription().getVersion());
                return true;
            }
            send(sender, "Unknown subcommand. Usage: /multimedia [version]");
            return true;
        })) {
            ConsoleLog.error("Failed to register /multimedia command!");
        }

        if (!TabCompleter.register(this, "multimedia", TabCompleter.simple("version", "help"))) {
            ConsoleLog.warn("Failed to register tab completer for /multimedia");
        }

        // Example: Schedule a task to run after 5 seconds
        SchedulerHelper.runLaterSeconds(this, () -> {
            ConsoleLog.info("Multimedia has been running for 5 seconds!");
        }, 5);

        // Example: Run a repeating task every minute (can be disabled via config)
        if (ConfigHelp.getBoolean("general.enable-heartbeat", false)) {
            SchedulerHelper.runTimerMinutes(this, () -> {
                ConsoleLog.info("Heartbeat: " + PlayerGather.getOnlinePlayerCount() + " players online");
            }, 1, 1);
        }

        ConsoleLog.info("Multimedia has been enabled!");
    }

    @Override
    public void onDisable() {
        ConsoleLog.info("Disabling Multimedia...");
        // Cancel all scheduled tasks
        SchedulerHelper.cancelAllTasks(this);
        ConsoleLog.info("Goodbye!");
    }
}
