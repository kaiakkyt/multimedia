# Multimedia Plugin - Class Documentation

### WARNING! 
anything schedule or threading related may be different for folia! if you know how to use folia's systems, this shouldnt be a big problem!

## 1. Base64

**Purpose**: Encode and decode Base64 strings, files, and resources with ease.

### Basic Usage

```java
// Encode/decode strings
String encoded = Base64.encode("Hello World");
String decoded = Base64.decodeToString(encoded);

// Encode/decode bytes
byte[] data = "test".getBytes();
String base64 = Base64.encodeBytes(data);
byte[] original = Base64.decodeToBytes(base64);
```

### File Operations

```java
// Read file and encode to Base64
Path file = Paths.get("data.txt");
String encodedFile = Base64.encodeFileToBase64(file);

// Decode Base64 and write to file
Base64.decodeToFile(encodedData, outputPath);

// Safe file operations (returns null on error instead of throwing)
String safeEncoded = Base64.encodeFileToBase64Safe(file);
```

### Resource Handling

```java
// Encode a resource from your plugin JAR
String resourceData = Base64.encodeResourceToBase64(plugin, "config.yml");

// Decode and save resource
Base64.decodeToResource(base64Data, plugin, "saved.yml");
```

### Data URI Support

```java
// Create data URI from file
String dataUri = Base64.encodeFileToDataUri(file, "image/png");
// Result: "data:image/png;base64,iVBORw0KG..."

// Decode data URI back to bytes
byte[] imageData = Base64.decodeDataUri(dataUri);
```

### Plugin Data Folder Shortcuts

```java
// Encode file from plugin's data folder
String encoded = Base64.encodeFromDataFolder(plugin, "players.dat");

// Decode to plugin's data folder
Base64.decodeToDataFolder(plugin, encoded, "backup.dat");
```

---

## 2. ColorConverter

**Purpose**: Convert and manage color codes for Minecraft chat, supporting both legacy (`&`) and hex colors (`&#RRGGBB`).

### Basic Color Conversion

```java
// Convert & codes to § (section sign)
String colored = ColorConverter.translateAmpersandToSection("&aHello &bWorld");
// Result: "§aHello §bWorld"

// Convert § back to &
String ampersand = ColorConverter.translateSectionToAmpersand("§aHello §bWorld");
// Result: "&aHello &bWorld"
```

### Hex Color Support (1.16+)

```java
// Convert hex colors &#RRGGBB to Minecraft format
String hexColored = ColorConverter.colorize("&#FF5733This is orange &#00FF00and this is green");
// Also converts & codes automatically

// Example with gradient
String gradient = ColorConverter.colorize("&#FF0000R&#FF7F00a&#FFFF00i&#00FF00n&#0000FFb&#4B0082o&#9400D3w");
```

### Strip Colors

```java
// Remove all color codes from text
String plain = ColorConverter.stripColor("§aHello §bWorld");
// Result: "Hello World"

// Works with hex colors too
String stripped = ColorConverter.stripColor("§x§F§F§5§7§3§3Orange text");
// Result: "Orange text"
```

### Common Use Cases

```java
// Colorize config messages
String msg = ColorConverter.colorize(config.getString("messages.welcome"));
player.sendMessage(msg);

// Compare text without colors
String input = "§aApple";
String compare = "Apple";
if (ColorConverter.stripColor(input).equalsIgnoreCase(compare)) {
    // Match!
}

// Store colored text in config (use & format)
String stored = ColorConverter.translateSectionToAmpersand(coloredText);
config.set("message", stored);
```

---

## 3. CommandExecuter

**Purpose**: Execute commands as console or players, with support for silent execution and command validation.

### Basic Command Execution

```java
// Run command as console
CommandExecuter.runAsConsole("say Hello from console!");
CommandExecuter.runAsConsole("give PlayerName diamond 64");

// Run command as player
Player player = ...;
CommandExecuter.runAsPlayer(player, "spawn");
CommandExecuter.runAsPlayer(player, "kit starter");
```

### Silent Execution

```java
// Run command without console output
CommandExecuter.runConsoleSilently("say This won't show in console");

// Run player command silently
CommandExecuter.runPlayerSilently(player, "tp 0 100 0");

// Generic silent execution
CommandExecuter.runSilently(sender, "command");
```

### Command Validation

```java
// Check if command exists
if (CommandExecuter.commandExists("spawn")) {
    // Command is registered
}

// Check if player can execute command
if (CommandExecuter.canExecute(player, "gamemode")) {
    CommandExecuter.runAsPlayer(player, "gamemode creative");
}
```

### Batch Execution

```java
// Run multiple commands at once
List<String> commands = Arrays.asList(
    "say Starting server setup...",
    "time set day",
    "weather clear",
    "say Setup complete!"
);
CommandExecuter.runBatch(commands, true); // true = as console
```

### Command History

```java
// Enable command tracking
CommandExecuter.enableHistory();

// Get command history
List<String> history = CommandExecuter.getHistory();
for (String cmd : history) {
    ConsoleLog.info("Executed: " + cmd);
}

// Clear history
CommandExecuter.clearHistory();
```

### Advanced Features

```java
// Get command sender type
CommandSender sender = ...;
if (CommandExecuter.isConsole(sender)) {
    // Sender is console
}

// Run command with result check
boolean success = CommandExecuter.runAsConsole("reload");
if (success) {
    ConsoleLog.info("Reload successful");
} else {
    ConsoleLog.warn("Reload failed");
}
```

---

## 4. CommandHelper

**Purpose**: Simplified command registration with lambda support, eliminating CommandExecutor boilerplate.

### Basic Command Registration

```java
@Override
public void onEnable() {
    // Register command with lambda
    CommandHelper.register(this, "multimedia", (sender, label, args) -> {
        sender.sendMessage("Hello from Multimedia!");
        return true;
    });
}
```

### Handling Arguments

```java
CommandHelper.register(this, "teleport", (sender, label, args) -> {
    if (args.length < 3) {
        sender.sendMessage("Usage: /teleport <x> <y> <z>");
        return false;
    }
    
    if (!(sender instanceof Player)) {
        sender.sendMessage("Only players can teleport!");
        return false;
    }
    
    Player player = (Player) sender;
    try {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        
        Location loc = new Location(player.getWorld(), x, y, z);
        player.teleport(loc);
        player.sendMessage("Teleported to " + x + ", " + y + ", " + z);
        return true;
    } catch (NumberFormatException e) {
        sender.sendMessage("Invalid coordinates!");
        return false;
    }
});
```

### Subcommands

```java
CommandHelper.register(this, "admin", (sender, label, args) -> {
    if (args.length == 0) {
        sender.sendMessage("Usage: /admin <reload|info|clear>");
        return false;
    }
    
    switch (args[0].toLowerCase()) {
        case "reload":
            reloadConfig();
            sender.sendMessage("Config reloaded!");
            return true;
            
        case "info":
            sender.sendMessage("Server version: " + Bukkit.getVersion());
            sender.sendMessage("Players online: " + Bukkit.getOnlinePlayers().size());
            return true;
            
        case "clear":
            if (sender instanceof Player) {
                ((Player) sender).getInventory().clear();
                sender.sendMessage("Inventory cleared!");
            }
            return true;
            
        default:
            sender.sendMessage("Unknown subcommand: " + args[0]);
            return false;
    }
});
```

### Permission Checks

```java
CommandHelper.register(this, "op-command", (sender, label, args) -> {
    if (!sender.hasPermission("plugin.admin")) {
        sender.sendMessage(ChatColor.RED + "No permission!");
        return false;
    }
    
    // Admin-only logic here
    sender.sendMessage("Admin command executed!");
    return true;
});
```

### Error Handling

```java
// CommandHelper automatically catches exceptions and logs them
CommandHelper.register(this, "risky", (sender, label, args) -> {
    // If this throws an exception, it will be caught and logged
    performRiskyOperation();
    sender.sendMessage("Operation completed!");
    return true;
});
```

### Requirements

**Important**: Commands must be declared in `plugin.yml` before registration:

```yaml
commands:
  multimedia:
    description: Main plugin command
    usage: /<command>
  teleport:
    description: Teleport command
    usage: /teleport <x> <y> <z>
  admin:
    description: Admin commands
    usage: /admin <subcommand>
```

---

## 5. ConfigHelp

**Purpose**: Simplified config.yml management with automatic defaults and type-safe getters.

### Initialization

```java
@Override
public void onEnable() {
    // Initialize ConfigHelp (loads or creates config.yml)
    ConfigHelp.init(this);
}
```

### Setting Defaults

```java
// Create default config values
Map<String, Object> defaults = new HashMap<>();
defaults.put("messages.welcome", "&aWelcome to the server!");
defaults.put("messages.goodbye", "&cGoodbye!");
defaults.put("settings.max-players", 100);
defaults.put("settings.enabled", true);
defaults.put("settings.cooldown", 5.5);

// Apply defaults (won't overwrite existing values)
ConfigHelp.ensureDefaults(defaults);

// Single default
ConfigHelp.ensureDefault("prefix", "[Server]");
```

### Reading Values

```java
// String with fallback
String welcome = ConfigHelp.getString("messages.welcome", "Default welcome");

// Integer with fallback
int maxPlayers = ConfigHelp.getInt("settings.max-players", 50);

// Boolean with fallback
boolean enabled = ConfigHelp.getBoolean("settings.enabled", true);

// Double with fallback
double cooldown = ConfigHelp.getDouble("settings.cooldown", 3.0);

// String list
List<String> allowedWorlds = ConfigHelp.getStringList("allowed-worlds");
```

### Writing Values

```java
// Set value and save immediately
ConfigHelp.set("last-restart", System.currentTimeMillis());
ConfigHelp.set("motd", "&6Welcome to &bMy Server");

// Check if key exists
if (ConfigHelp.has("custom-setting")) {
    // Key exists in config
}
```

### Reload Config

```java
// Reload config from disk
ConfigHelp.reload();

// Save config manually
ConfigHelp.save();
```

### Complete Example

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Initialize
        ConfigHelp.init(this);
        
        // Setup defaults
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("messages.welcome", "&aWelcome!");
        defaults.put("messages.goodbye", "&cSee you later!");
        defaults.put("settings.spawn-protection", 16);
        defaults.put("settings.pvp-enabled", true);
        defaults.put("features.fly", false);
        defaults.put("features.godmode", false);
        
        ConfigHelp.ensureDefaults(defaults);
        
        // Use config values
        boolean pvp = ConfigHelp.getBoolean("settings.pvp-enabled", true);
        getLogger().info("PVP enabled: " + pvp);
    }
    
    public void sendWelcome(Player player) {
        String msg = ConfigHelp.getString("messages.welcome", "Welcome!");
        String colored = ColorConverter.colorize(msg);
        player.sendMessage(colored);
    }
}
```

### Best Practices

```java
// Always provide fallback values
String value = ConfigHelp.getString("path", "fallback");

// Use descriptive paths
ConfigHelp.set("economy.starting-balance", 1000);
ConfigHelp.set("world.pvp-worlds", Arrays.asList("world", "world_nether"));

// Check existence before reading
if (ConfigHelp.has("optional-feature")) {
    int value = ConfigHelp.getInt("optional-feature", 0);
    // Use value
}
```

---

## 6. ConsoleLog

**Purpose**: Simplified logging with log levels and formatted output.

### Initialization

```java
@Override
public void onEnable() {
    // Initialize with your plugin
    ConsoleLog.init(this);
}
```

### Basic Logging

```java
// Info messages
ConsoleLog.info("Plugin started successfully");

// Warnings
ConsoleLog.warn("Config file is outdated");

// Errors
ConsoleLog.error("Failed to connect to database");

// Error with exception
try {
    riskyOperation();
} catch (Exception e) {
    ConsoleLog.error("Operation failed", e);
}
```

### Formatted Logging

```java
// Printf-style formatting
ConsoleLog.infof("Loaded %d players in %.2f seconds", count, time);
ConsoleLog.warnf("Player %s exceeded limit: %d/%d", name, current, max);
ConsoleLog.errorf("Invalid value: %s (expected: %s)", value, expected);
```

### Log Levels

```java
// Set minimum log level
ConsoleLog.setLevel(ConsoleLog.LogLevel.INFO);    // Default: logs everything
ConsoleLog.setLevel(ConsoleLog.LogLevel.WARNING); // Only warnings and errors
ConsoleLog.setLevel(ConsoleLog.LogLevel.ERROR);   // Only errors

// Get current level
ConsoleLog.LogLevel level = ConsoleLog.getLevel();
```

### Complete Example

```java
public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        ConsoleLog.init(this);
        ConsoleLog.info("Starting initialization...");
        
        try {
            loadConfig();
            ConsoleLog.info("Config loaded successfully");
            
            connectDatabase();
            ConsoleLog.info("Database connected");
            
            int playerCount = loadPlayers();
            ConsoleLog.infof("Loaded %d players", playerCount);
            
        } catch (Exception e) {
            ConsoleLog.error("Initialization failed", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        ConsoleLog.info("Plugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        ConsoleLog.info("Plugin disabled");
    }
}
```

---

## 7. ErrorHandler

**Purpose**: Centralized error handling with detailed stack trace logging.

### Basic Error Handling

```java
// Handle exception with context
try {
    loadData();
} catch (Exception e) {
    ErrorHandler.handle(plugin, "Failed to load data", e);
}

// Handle exception without context
try {
    saveData();
} catch (Exception e) {
    ErrorHandler.handle(plugin, e);
}
```

### Warning Messages

```java
// Log warning with exception
try {
    optionalFeature();
} catch (Exception e) {
    ErrorHandler.warn(plugin, "Optional feature unavailable", e);
}
```

### Info Messages

```java
// Log informational message
ErrorHandler.info(plugin, "Data migration completed");
```

### Complete Example

```java
public void processPlayerData(Player player) {
    try {
        // Risky operation
        PlayerData data = loadPlayerData(player.getUniqueId());
        data.update();
        savePlayerData(data);
        
    } catch (IOException e) {
        ErrorHandler.handle(plugin, "IO error while processing player data", e);
        player.sendMessage(ChatColor.RED + "An error occurred. Please contact an admin.");
        
    } catch (Exception e) {
        ErrorHandler.handle(plugin, "Unexpected error in player data processing", e);
    }
}

public void initializeFeature() {
    try {
        setupFeature();
        ErrorHandler.info(plugin, "Feature initialized successfully");
    } catch (UnsupportedOperationException e) {
        ErrorHandler.warn(plugin, "Feature not supported on this server version", e);
    } catch (Exception e) {
        ErrorHandler.handle(plugin, "Failed to initialize feature", e);
        throw e; // Re-throw if critical
    }
}
```

### Stack Trace Details

ErrorHandler automatically logs:
- Full exception message
- Complete stack trace
- Cause chain (if present)
- Package context for easier debugging

---

## 8. InventoryHelper

**Purpose**: Simplified inventory and item management with version-aware material handling.

### Creating Inventories

```java
// Create custom inventory
Inventory inv = InventoryHelper.createInventory(player, "My Custom GUI", 3); // 3 rows

// Open inventory for player
InventoryHelper.openInventory(player, inv);
```

### Giving Items

```java
// Give item to player (drops excess if inventory full)
ItemStack diamond = new ItemStack(Material.DIAMOND, 64);
boolean allAdded = InventoryHelper.giveItem(player, diamond);

if (!allAdded) {
    player.sendMessage("Some items were dropped!");
}

// Give with leftover handling
ItemStack leftover = InventoryHelper.giveItemAndReturnLeftover(player, diamond);
if (leftover != null) {
    player.sendMessage("Couldn't fit " + leftover.getAmount() + " items");
}
```

### Taking Items

```java
// Take specific amount of material
ItemStack taken = InventoryHelper.takeItem(player, Material.GOLD_INGOT, 10);
if (taken != null) {
    player.sendMessage("Took " + taken.getAmount() + " gold ingots");
}

// Check if player has enough
if (InventoryHelper.hasItem(player, Material.DIAMOND, 5)) {
    // Player has at least 5 diamonds
}

// Remove all of a material
int removed = InventoryHelper.removeAll(player, Material.DIRT);
player.sendMessage("Removed " + removed + " dirt blocks");
```

### Inventory Queries

```java
// Find first empty slot
int emptySlot = InventoryHelper.findFirstEmptySlot(inventory);

// Find slots containing an item
List<Integer> slots = InventoryHelper.findSlots(inventory, itemStack);

// Count items
int count = InventoryHelper.countItem(player, Material.EMERALD);
player.sendMessage("You have " + count + " emeralds");

// Check if inventory is full
if (InventoryHelper.isInventoryFull(player)) {
    player.sendMessage("Your inventory is full!");
}
```

### Item Serialization

```java
// Serialize item to Base64
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
item.getItemMeta().setDisplayName("Legendary Sword");
String serialized = InventoryHelper.serializeItem(item);

// Save to config
config.set("items.legendary-sword", serialized);

// Deserialize back
String data = config.getString("items.legendary-sword");
ItemStack restored = InventoryHelper.deserializeItem(data);

// Serialize entire inventory
String invData = InventoryHelper.serializeInventory(player.getInventory());

// Deserialize and restore
InventoryHelper.deserializeInventory(invData, player.getInventory());
```

### Material Handling

```java
// Get material with version fallbacks
Material mat = InventoryHelper.getMaterial("OAK_PLANKS"); // Works on 1.7-1.20+

// Create ItemStack with version-aware material
ItemStack item = InventoryHelper.createItem("PLAYER_HEAD", 1);
```

### GUI Helpers

```java
// Fill inventory with item
ItemStack glass = new ItemStack(Material.GLASS_PANE);
InventoryHelper.fillInventory(inventory, glass);

// Fill border with item
InventoryHelper.fillBorder(inventory, glass);

// Set item at slot with click prevention
InventoryHelper.setGuiItem(inventory, 13, displayItem);
```

### Complete GUI Example

```java
public void openShop(Player player) {
    Inventory shop = InventoryHelper.createInventory(player, "Item Shop", 3);
    
    // Create shop items
    ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
    ItemMeta meta = diamond.getItemMeta();
    meta.setDisplayName(ChatColor.AQUA + "Diamond - $100");
    diamond.setItemMeta(meta);
    
    ItemStack gold = new ItemStack(Material.GOLD_INGOT, 1);
    ItemMeta goldMeta = gold.getItemMeta();
    goldMeta.setDisplayName(ChatColor.GOLD + "Gold Ingot - $50");
    gold.setItemMeta(goldMeta);
    
    // Add to inventory
    shop.setItem(11, diamond);
    shop.setItem(13, gold);
    
    // Fill borders
    ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    InventoryHelper.fillBorder(shop, border);
    
    // Open for player
    InventoryHelper.openInventory(player, shop);
}
```

---

## 9. JavaUtilities

**Purpose**: System monitoring, memory management, and thread utilities.

### Memory Information

```java
// Get current memory usage
JavaUtilities.MemoryInfo info = JavaUtilities.getMemoryUsage();

ConsoleLog.info("Max Memory: " + info.getMaxMB() + "MB");
ConsoleLog.info("Used Memory: " + info.getUsedMB() + "MB");
ConsoleLog.info("Free Memory: " + info.getFreeMB() + "MB");
ConsoleLog.info("Usage: " + info.getUsedPercent() + "%");

// Get formatted memory string
String memStr = JavaUtilities.getMemoryString();
// Output: "Used: 512MB / 2048MB (25.0%)"
```

### Garbage Collection

```java
// Force garbage collection (use sparingly - can cause lag!)
boolean success = JavaUtilities.forceGC();
if (success) {
    ConsoleLog.info("Garbage collection triggered");
}
```

### Memory Warning System

```java
@Override
public void onEnable() {
    JavaUtilities.init(this);
    
    // Register memory warning at 80% usage
    JavaUtilities.registerMemoryWarning(80.0, (info) -> {
        ConsoleLog.warn("Memory usage is high: " + info.getUsedPercent() + "%");
        ConsoleLog.warn("Used: " + info.getUsedMB() + "MB / " + info.getMaxMB() + "MB");
        
        // Take action
        clearCaches();
        JavaUtilities.forceGC();
    });
    
    // Start monitoring (checks every 30 seconds)
    JavaUtilities.startMemoryMonitor();
}
```

### Thread Information

```java
// Get thread count
int threads = JavaUtilities.getThreadCount();
ConsoleLog.info("Active threads: " + threads);

// Get thread details
List<JavaUtilities.ThreadInfo> threadList = JavaUtilities.getThreadDetails();
for (JavaUtilities.ThreadInfo thread : threadList) {
    ConsoleLog.info(thread.getName() + " - " + thread.getState());
}
```

### System Information

```java
// Get system info
JavaUtilities.SystemInfo sysInfo = JavaUtilities.getSystemInfo();

ConsoleLog.info("OS: " + sysInfo.getOsName());
ConsoleLog.info("OS Version: " + sysInfo.getOsVersion());
ConsoleLog.info("Java Version: " + sysInfo.getJavaVersion());
ConsoleLog.info("CPU Cores: " + sysInfo.getAvailableProcessors());
ConsoleLog.info("Max Memory: " + sysInfo.getMaxMemoryMB() + "MB");

// Get formatted system info
String systemString = JavaUtilities.getSystemInfoString();
ConsoleLog.info(systemString);
```

### Async Execution

```java
// Run task asynchronously
JavaUtilities.runAsync(() -> {
    // Heavy computation here
    processLargeDataset();
});

// Run with result
CompletableFuture<String> future = JavaUtilities.supplyAsync(() -> {
    return calculateExpensiveValue();
});

future.thenAccept(result -> {
    ConsoleLog.info("Result: " + result);
});
```

### Complete Monitoring Example

```java
public class MonitoringPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        JavaUtilities.init(this);
        
        // Setup memory warnings
        JavaUtilities.registerMemoryWarning(75.0, this::handleMemoryWarning);
        JavaUtilities.registerMemoryWarning(90.0, this::handleCriticalMemory);
        JavaUtilities.startMemoryMonitor();
        
        // Log system info
        logSystemInfo();
        
        // Schedule periodic status check
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, 
            this::logStatus, 0L, 20L * 60 * 5); // Every 5 minutes
    }
    
    private void handleMemoryWarning(JavaUtilities.MemoryInfo info) {
        ConsoleLog.warn("Memory warning: " + info.getUsedPercent() + "%");
        clearNonEssentialCaches();
    }
    
    private void handleCriticalMemory(JavaUtilities.MemoryInfo info) {
        ConsoleLog.error("CRITICAL: Memory at " + info.getUsedPercent() + "%");
        performEmergencyCleanup();
        JavaUtilities.forceGC();
    }
    
    private void logSystemInfo() {
        JavaUtilities.SystemInfo info = JavaUtilities.getSystemInfo();
        ConsoleLog.info("=== System Information ===");
        ConsoleLog.info("OS: " + info.getOsName() + " " + info.getOsVersion());
        ConsoleLog.info("Java: " + info.getJavaVersion());
        ConsoleLog.info("CPU Cores: " + info.getAvailableProcessors());
        ConsoleLog.info("Max Memory: " + info.getMaxMemoryMB() + "MB");
    }
    
    private void logStatus() {
        String mem = JavaUtilities.getMemoryString();
        int threads = JavaUtilities.getThreadCount();
        ConsoleLog.info("Status - " + mem + " | Threads: " + threads);
    }
}
```

---

## 10. Mathematics

**Purpose**: Common mathematical operations and utilities for game development.

### Basic Math

```java
// Clamp value between min and max
int health = Mathematics.clamp(playerHealth, 0, 20);
double damage = Mathematics.clamp(calculatedDamage, 0.0, 100.0);

// Linear interpolation
double progress = Mathematics.lerp(0.0, 100.0, 0.5); // Result: 50.0

// Map value from one range to another
double scaled = Mathematics.map(50, 0, 100, 0, 1); // Result: 0.5

// Approximate equality with epsilon
if (Mathematics.approxEquals(3.14159, Math.PI, 0.001)) {
    // Values are approximately equal
}
```

### Integer Operations

```java
// Greatest common divisor
long gcd = Mathematics.gcd(48, 18); // Result: 6

// Least common multiple
long lcm = Mathematics.lcm(12, 18); // Result: 36

// Prime check
if (Mathematics.isPrime(17)) {
    // 17 is prime
}
```

### Combinatorics

```java
// Factorial
BigInteger fact = Mathematics.factorial(10); // 10! = 3,628,800

// Permutation (nPr)
BigInteger perm = Mathematics.permutation(10, 3); // 10P3 = 720

// Combination (nCr)
BigInteger comb = Mathematics.combination(10, 3); // 10C3 = 120
```

### Random Utilities

```java
// Random integer in range [min, max]
int roll = Mathematics.randomInt(1, 6); // Dice roll

// Random double in range [min, max]
double chance = Mathematics.randomDouble(0.0, 1.0);

// Random from list
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
String random = Mathematics.randomFromList(names);

// Weighted random selection
Map<String, Integer> weights = new HashMap<>();
weights.put("common", 70);
weights.put("rare", 25);
weights.put("legendary", 5);
String rarity = Mathematics.weightedRandom(weights);
```

### Number Formatting

```java
// Format with commas
String formatted = Mathematics.formatNumber(1234567); // "1,234,567"

// Format as percentage
String percent = Mathematics.formatPercent(0.756); // "75.6%"

// Format with decimal places
String precise = Mathematics.formatDecimal(3.14159, 2); // "3.14"

// Abbreviate large numbers
String abbrev = Mathematics.abbreviateNumber(1500000); // "1.5M"
```

### Angle & Geometry

```java
// Convert degrees to radians
double rad = Mathematics.toRadians(180.0); // PI

// Convert radians to degrees
double deg = Mathematics.toDegrees(Math.PI); // 180.0

// Normalize angle to [0, 360)
double normalized = Mathematics.normalizeAngle(450.0); // 90.0

// Calculate distance between points (2D)
double dist = Mathematics.distance(x1, y1, x2, y2);

// Calculate distance between points (3D)
double dist3d = Mathematics.distance3D(x1, y1, z1, x2, y2, z2);
```

### Safe Operations

```java
// Safe division (returns 0 if divisor is 0)
double result = Mathematics.safeDivide(10.0, 0.0); // 0.0 instead of exception

// Safe parse integer
int value = Mathematics.parseIntSafe("123", 0); // 123
int invalid = Mathematics.parseIntSafe("abc", -1); // -1 (fallback)

// Safe parse double
double dValue = Mathematics.parseDoubleSafe("3.14", 0.0); // 3.14
```

### Game Development Examples

```java
// Damage calculation with random variance
public double calculateDamage(double base, double variance) {
    double min = base - variance;
    double max = base + variance;
    return Mathematics.randomDouble(min, max);
}

// Experience curve
public int getExpForLevel(int level) {
    return (int) Mathematics.map(level, 1, 100, 0, 1000000);
}

// Drop chance system
public boolean shouldDrop(double dropChance) {
    return Mathematics.randomDouble(0, 100) < dropChance;
}

// Health regeneration with interpolation
public void regenerateHealth(Player player, long ticks) {
    double progress = Mathematics.clamp(ticks / 200.0, 0.0, 1.0);
    double health = Mathematics.lerp(1.0, 20.0, progress);
    player.setHealth(health);
}

// Circle of players (spawn positions)
public List<Location> getCirclePositions(Location center, double radius, int players) {
    List<Location> positions = new ArrayList<>();
    for (int i = 0; i < players; i++) {
        double angle = 2 * Math.PI * i / players;
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        positions.add(new Location(center.getWorld(), x, center.getY(), z));
    }
    return positions;
}
```

---

## 11. PermissionHandler

**Purpose**: Runtime permission management without depending on permission plugins.

### Initialization

```java
@Override
public void onEnable() {
    // Initialize PermissionHandler
    PermissionHandler.init(this);
}
```

### Basic Permission Management

```java
// Give permission to player
PermissionHandler.givePermission(player, "myplugin.vip");

// Remove permission
PermissionHandler.removePermission(player, "myplugin.vip");

// Set permission (true = grant, false = revoke)
PermissionHandler.setPermission(player, "myplugin.admin", true);

// Check if player has permission
if (PermissionHandler.hasPermission(player, "myplugin.fly")) {
    // Player has fly permission
}
```

### Permission Checks with Op

```java
// Check permission (ops automatically pass if allowOp is true)
boolean canUse = PermissionHandler.hasPermission(player, "myplugin.command", true);

// Strict check (ops must have explicit permission)
boolean strictCheck = PermissionHandler.hasPermission(player, "myplugin.admin", false);
```

### Multiple Permissions

```java
// Give multiple permissions at once
PermissionHandler.givePermissions(player, 
    "myplugin.command1", 
    "myplugin.command2", 
    "myplugin.feature"
);

// Set multiple permissions with map
Map<String, Boolean> perms = new HashMap<>();
perms.put("myplugin.build", true);
perms.put("myplugin.break", true);
perms.put("myplugin.pvp", false);
PermissionHandler.setPermissions(player, perms);
```

### Group Permissions

```java
// Define permission groups
Map<String, Boolean> vipPerms = new HashMap<>();
vipPerms.put("myplugin.fly", true);
vipPerms.put("myplugin.kit.vip", true);
vipPerms.put("myplugin.homes.5", true);

// Apply group (clearExisting = false keeps other permissions)
PermissionHandler.setGroupPermissions(player, vipPerms, false);

// Replace all permissions with group (clearExisting = true)
PermissionHandler.setGroupPermissions(player, vipPerms, true);
```

### Permission Queries

```java
// List all permissions for player
List<String> perms = PermissionHandler.getPlayerPermissions(player);
for (String perm : perms) {
    ConsoleLog.info("Player has: " + perm);
}

// Check multiple permissions at once
List<String> required = Arrays.asList("perm1", "perm2", "perm3");
if (PermissionHandler.hasAllPermissions(player, required)) {
    // Player has all required permissions
}

// Check if player has any of the permissions
if (PermissionHandler.hasAnyPermission(player, required)) {
    // Player has at least one permission
}
```

### Clearing Permissions

```java
// Clear all custom permissions from player
PermissionHandler.clearPermissions(player);

// Clear specific permission
PermissionHandler.removePermission(player, "myplugin.temp");
```

### Complete Example - Rank System

```java
public class RankManager {
    
    private final Map<String, Map<String, Boolean>> ranks = new HashMap<>();
    
    public RankManager() {
        // Define ranks
        Map<String, Boolean> member = new HashMap<>();
        member.put("myplugin.chat", true);
        member.put("myplugin.home", true);
        ranks.put("member", member);
        
        Map<String, Boolean> vip = new HashMap<>();
        vip.put("myplugin.chat", true);
        vip.put("myplugin.home", true);
        vip.put("myplugin.fly", true);
        vip.put("myplugin.kit.vip", true);
        ranks.put("vip", vip);
        
        Map<String, Boolean> admin = new HashMap<>();
        admin.put("myplugin.*", true);
        ranks.put("admin", admin);
    }
    
    public void setRank(Player player, String rank) {
        Map<String, Boolean> perms = ranks.get(rank.toLowerCase());
        if (perms == null) {
            ConsoleLog.warn("Unknown rank: " + rank);
            return;
        }
        
        // Clear existing permissions and apply rank
        PermissionHandler.clearPermissions(player);
        PermissionHandler.setGroupPermissions(player, perms, true);
        
        player.sendMessage(ColorConverter.colorize(
            "&aYour rank has been set to &e" + rank
        ));
        ConsoleLog.info("Set rank " + rank + " for " + player.getName());
    }
    
    public void grantTemporaryPermission(Player player, String permission, long seconds) {
        PermissionHandler.givePermission(player, permission);
        player.sendMessage("Granted temporary permission: " + permission);
        
        // Remove after delay
        SchedulerHelper.runLaterSeconds(plugin, () -> {
            PermissionHandler.removePermission(player, permission);
            player.sendMessage("Temporary permission expired: " + permission);
        }, seconds);
    }
}
```

### Permission Registration

```java
// Register permissions with Bukkit (optional, for /permissions command)
PermissionHandler.registerPermission("myplugin.admin", 
    "Admin permission", 
    PermissionDefault.OP
);

PermissionHandler.registerPermission("myplugin.vip", 
    "VIP features", 
    PermissionDefault.FALSE
);
```

---

## 12. PlayerGather

**Purpose**: Simplified player lookup, teleportation, and inventory management.

### Getting Players

```java
// Get all online players
List<Player> online = PlayerGather.getOnlinePlayers();

// Get online player by name
Player player = PlayerGather.getOnlinePlayer("Notch");

// Get online player by UUID
UUID uuid = UUID.fromString("...");
Player player = PlayerGather.getOnlinePlayer(uuid);

// Get online player count
int count = PlayerGather.getOnlinePlayerCount();

// Get list of online player names
List<String> names = PlayerGather.getOnlinePlayerNames();
```

### Offline Players

```java
// Get offline player by name
OfflinePlayer offline = PlayerGather.getOfflinePlayer("Notch");

// Get offline player by UUID
OfflinePlayer offline = PlayerGather.getOfflinePlayer(uuid);

// Get all offline players
OfflinePlayer[] allOffline = PlayerGather.getAllOfflinePlayers();

// Get as list
List<OfflinePlayer> offlineList = PlayerGather.getAllOfflinePlayersList();

// Check if player has played before
if (PlayerGather.hasPlayedBefore("Notch")) {
    // Player has joined the server before
}
```

### Player Search

```java
// Find players with names starting with prefix
List<Player> matches = PlayerGather.findOnlinePlayersStartingWith("Not");
// Returns players like "Notch", "NotchDev", etc.

// Find players with names containing substring
List<Player> contains = PlayerGather.findOnlinePlayersContaining("otch");
// Returns players like "Notch", "Scotch", etc.
```

### Inventory Management

```java
// Clear player inventory
PlayerGather.clearInventory(player);

// Clear inventory and armor
PlayerGather.clearInventoryAndArmor(player);

// Give items
ItemStack diamond = new ItemStack(Material.DIAMOND, 64);
ItemStack gold = new ItemStack(Material.GOLD_INGOT, 32);
PlayerGather.giveItems(player, diamond, gold);

// Give items from list
List<ItemStack> rewards = getRewards();
PlayerGather.giveItems(player, rewards);
```

### Teleportation

```java
// Teleport to location
Location spawn = new Location(world, 0, 100, 0);
PlayerGather.teleport(player, spawn);

// Teleport to coordinates
PlayerGather.teleport(player, world, 100, 64, 200);

// Teleport to another player
Player target = PlayerGather.getOnlinePlayer("Target");
PlayerGather.teleportToPlayer(player, target);

// Teleport with safety check (finds safe location)
boolean safe = PlayerGather.teleportSafely(player, location);
if (!safe) {
    player.sendMessage("No safe location found!");
}
```

### Player State Checks

```java
// Check if player is flying
if (PlayerGather.isFlying(player)) {
    // Player is flying
}

// Check if player is sneaking
if (PlayerGather.isSneaking(player)) {
    // Player is sneaking
}

// Check if player is sprinting
if (PlayerGather.isSprinting(player)) {
    // Player is sprinting
}

// Check if player is in water
if (PlayerGather.isInWater(player)) {
    // Player is in water
}
```

### Player Properties

```java
// Get player health
double health = PlayerGather.getHealth(player);

// Set player health
PlayerGather.setHealth(player, 20.0);

// Get player food level
int food = PlayerGather.getFoodLevel(player);

// Set player food level
PlayerGather.setFoodLevel(player, 20);

// Get player experience
int exp = PlayerGather.getExperience(player);

// Get player level
int level = PlayerGather.getLevel(player);

// Set player level
PlayerGather.setLevel(player, 50);
```

### Complete Example - Admin Tools

```java
public class AdminTools {
    
    public void healAll() {
        List<Player> players = PlayerGather.getOnlinePlayers();
        for (Player player : players) {
            PlayerGather.setHealth(player, 20.0);
            PlayerGather.setFoodLevel(player, 20);
            player.sendMessage(ColorConverter.colorize("&aYou have been healed!"));
        }
        ConsoleLog.info("Healed " + players.size() + " players");
    }
    
    public void teleportAll(Location destination) {
        List<Player> players = PlayerGather.getOnlinePlayers();
        int count = 0;
        for (Player player : players) {
            if (PlayerGather.teleport(player, destination)) {
                count++;
            }
        }
        ConsoleLog.info("Teleported " + count + " players");
    }
    
    public void clearInventories() {
        for (Player player : PlayerGather.getOnlinePlayers()) {
            if (player.hasPermission("admin.bypass")) continue;
            PlayerGather.clearInventoryAndArmor(player);
            player.sendMessage("Your inventory has been cleared!");
        }
    }
    
    public void giveStarterKit(Player player) {
        List<ItemStack> kit = Arrays.asList(
            new ItemStack(Material.STONE_SWORD),
            new ItemStack(Material.STONE_PICKAXE),
            new ItemStack(Material.COOKED_BEEF, 32),
            new ItemStack(Material.TORCH, 64)
        );
        PlayerGather.giveItems(player, kit);
        player.sendMessage("Starter kit given!");
    }
    
    public void findPlayer(CommandSender sender, String search) {
        List<Player> matches = PlayerGather.findOnlinePlayersContaining(search);
        if (matches.isEmpty()) {
            sender.sendMessage("No players found matching: " + search);
        } else {
            sender.sendMessage("Found " + matches.size() + " player(s):");
            for (Player p : matches) {
                sender.sendMessage("  - " + p.getName());
            }
        }
    }
}
```

---

## 13. ProxyListener

**Purpose**: BungeeCord/Velocity proxy integration for multi-server setups.

### Initialization

```java
@Override
public void onEnable() {
    // Initialize proxy listener
    ProxyListener.init(this);
}

@Override
public void onDisable() {
    // Cleanup
    ProxyListener.cleanup();
}
```

### Proxy Detection

```java
// Check if server is behind a proxy
if (ProxyListener.isBehindProxy()) {
    ConsoleLog.info("Server is behind BungeeCord/Velocity");
} else {
    ConsoleLog.info("Server is standalone");
}

// Get current server name
String serverName = ProxyListener.getCurrentServer();
ConsoleLog.info("Current server: " + serverName);
```

### Server Communication

```java
// Send player to another server
boolean sent = ProxyListener.connectPlayerToServer(player, "lobby");
if (sent) {
    player.sendMessage("Connecting to lobby...");
} else {
    player.sendMessage("Failed to connect to lobby");
}

// Request list of servers from proxy
ProxyListener.requestServerList();

// Get cached server list (after request)
List<String> servers = ProxyListener.getServerList();
for (String server : servers) {
    ConsoleLog.info("Available server: " + server);
}
```

### Player Information

```java
// Get player's server
String playerServer = ProxyListener.getPlayerServer(player);

// Get player count on server
int count = ProxyListener.getPlayerCount("lobby");
ConsoleLog.info("Players on lobby: " + count);

// Get player count on current server
int localCount = ProxyListener.getLocalPlayerCount();

// Get total network player count
int totalPlayers = ProxyListener.getTotalPlayerCount();
```

### Plugin Messages

```java
// Send custom plugin message
ByteArrayDataOutput out = ByteStreams.newDataOutput();
out.writeUTF("CustomChannel");
out.writeUTF("Hello from server!");
ProxyListener.sendPluginMessage(player, "MySubChannel", out.toByteArray());

// Forward message to another server
ProxyListener.forwardToServer(player, "lobby", "CustomData", data);

// Forward message to all servers
ProxyListener.forwardToAllServers(player, "Broadcast", message);
```

### Complete Example - Multi-Server Hub

```java
public class NetworkManager {
    
    private Map<String, Integer> serverPlayerCounts = new HashMap<>();
    
    public void initialize() {
        ProxyListener.init(plugin);
        
        if (!ProxyListener.isBehindProxy()) {
            ConsoleLog.warn("Not behind proxy - network features disabled");
            return;
        }
        
        // Update server list every 30 seconds
        SchedulerHelper.runTimerSeconds(plugin, () -> {
            ProxyListener.requestServerList();
            updatePlayerCounts();
        }, 0, 30);
    }
    
    public void openServerSelector(Player player) {
        List<String> servers = ProxyListener.getServerList();
        if (servers.isEmpty()) {
            player.sendMessage("No servers available");
            return;
        }
        
        Inventory gui = InventoryHelper.createInventory(player, "Select Server", 3);
        
        int slot = 0;
        for (String server : servers) {
            ItemStack icon = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = icon.getItemMeta();
            
            meta.setDisplayName(ColorConverter.colorize("&a" + server));
            int playerCount = serverPlayerCounts.getOrDefault(server, 0);
            meta.setLore(Arrays.asList(
                ColorConverter.colorize("&7Players: &e" + playerCount),
                ColorConverter.colorize("&7Click to connect")
            ));
            
            icon.setItemMeta(meta);
            gui.setItem(slot++, icon);
        }
        
        player.openInventory(gui);
    }
    
    public void connectToLobby(Player player) {
        if (ProxyListener.connectPlayerToServer(player, "lobby")) {
            player.sendMessage(ColorConverter.colorize("&aConnecting to lobby..."));
        } else {
            player.sendMessage(ColorConverter.colorize("&cFailed to connect to lobby"));
        }
    }
    
    public void broadcastNetworkMessage(String message) {
        // Send to all servers via proxy
        Player anyPlayer = Bukkit.getOnlinePlayers().iterator().next();
        if (anyPlayer != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(message);
            ProxyListener.forwardToAllServers(anyPlayer, "NetworkBroadcast", out.toByteArray());
        }
    }
    
    private void updatePlayerCounts() {
        List<String> servers = ProxyListener.getServerList();
        for (String server : servers) {
            int count = ProxyListener.getPlayerCount(server);
            serverPlayerCounts.put(server, count);
        }
    }
}
```

### Event Handling

```java
// Listen for player server switch
@EventHandler
public void onPluginMessage(PluginMessageEvent event) {
    if (event.getTag().equals("BungeeCord")) {
        // Handle incoming proxy message
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subchannel = in.readUTF();
        
        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            int count = in.readInt();
            ConsoleLog.info(server + " has " + count + " players");
        }
    }
}
```

---

## 14. SchedulerHelper

**Purpose**: Simplified task scheduling without BukkitRunnable boilerplate.

### Initialization

```java
// Set plugin instance (optional, for some methods)
SchedulerHelper.setPlugin(this);
```

### Time Conversion

```java
// Convert time units to ticks
long ticks = SchedulerHelper.secondsToTicks(5.0); // 100 ticks
long ticks = SchedulerHelper.minutesToTicks(2.0); // 2400 ticks
long ticks = SchedulerHelper.hoursToTicks(1.0);   // 72000 ticks

// Convert ticks to time units
double seconds = SchedulerHelper.ticksToSeconds(100); // 5.0
double minutes = SchedulerHelper.ticksToMinutes(2400); // 2.0
```

### Synchronous Tasks

```java
// Run immediately (next tick)
SchedulerHelper.run(plugin, () -> {
    // Code runs on main thread
    player.sendMessage("Hello!");
});

// Run with delay
SchedulerHelper.runLater(plugin, () -> {
    player.sendMessage("Delayed message!");
}, 100L); // 100 ticks = 5 seconds

// Run with delay in seconds
SchedulerHelper.runLaterSeconds(plugin, () -> {
    player.sendMessage("5 seconds later...");
}, 5.0);

// Run with delay in minutes
SchedulerHelper.runLaterMinutes(plugin, () -> {
    ConsoleLog.info("10 minutes have passed");
}, 10.0);
```

### Repeating Tasks

```java
// Run repeatedly
BukkitTask task = SchedulerHelper.runTimer(plugin, () -> {
    // Runs every second
    broadcastMessage("Tick!");
}, 0L, 20L); // delay=0, period=20 ticks

// Run repeatedly in seconds
SchedulerHelper.runTimerSeconds(plugin, () -> {
    updateScoreboard();
}, 0.0, 1.0); // Runs every second

// Run repeatedly in minutes
SchedulerHelper.runTimerMinutes(plugin, () -> {
    saveData();
}, 5.0, 5.0); // Runs every 5 minutes
```

### Asynchronous Tasks

```java
// Run async immediately
SchedulerHelper.runAsync(plugin, () -> {
    // Heavy computation off main thread
    processLargeDataset();
});

// Run async with delay
SchedulerHelper.runAsyncLater(plugin, () -> {
    fetchFromDatabase();
}, 100L);

// Run async with delay in seconds
SchedulerHelper.runAsyncLaterSeconds(plugin, () -> {
    downloadFile();
}, 5.0);

// Run async repeatedly
SchedulerHelper.runAsyncTimer(plugin, () -> {
    checkExternalAPI();
}, 0L, 1200L); // Every 60 seconds

// Run async repeatedly in seconds
SchedulerHelper.runAsyncTimerSeconds(plugin, () -> {
    updateWebStats();
}, 0.0, 30.0); // Every 30 seconds
```

### Task Tracking

```java
// Run with automatic tracking
BukkitTask task = SchedulerHelper.runTracked(plugin, () -> {
    doSomething();
});

// Run repeating task with tracking
BukkitTask timer = SchedulerHelper.runTimerTracked(plugin, () -> {
    updateData();
}, 0L, 20L);

// Cancel specific tracked task
SchedulerHelper.cancelTask(task);

// Cancel all tracked tasks for plugin
SchedulerHelper.cancelAllTrackedTasks(plugin);

// Get all tracked tasks
List<BukkitTask> tasks = SchedulerHelper.getTrackedTasks();
```

### Task Cancellation

```java
// Cancel specific task
BukkitTask task = SchedulerHelper.runTimer(plugin, () -> {
    // Repeating task
}, 0L, 20L);

// Cancel later
task.cancel();

// Or use helper
SchedulerHelper.cancelTask(task);

// Cancel all tasks for plugin
SchedulerHelper.cancelAllTasks(plugin);

// Check if task is running
if (SchedulerHelper.isTaskRunning(task)) {
    // Task is still active
}
```

### Countdown Helper

```java
// Countdown timer
SchedulerHelper.countdown(plugin, 10, (remaining) -> {
    // Called each second
    Bukkit.broadcastMessage("Starting in " + remaining + "...");
}, () -> {
    // Called when countdown finishes
    Bukkit.broadcastMessage("Go!");
    startGame();
});

// Countdown with custom interval
SchedulerHelper.countdownSeconds(plugin, 60.0, 5.0, (remaining) -> {
    // Called every 5 seconds
    ConsoleLog.info(remaining + " seconds remaining");
}, () -> {
    ConsoleLog.info("Time's up!");
});
```

### Complete Example - Game Manager

```java
public class GameManager {
    
    private BukkitTask gameTask;
    private BukkitTask countdownTask;
    private int gameTime = 0;
    
    public void startGameCountdown() {
        SchedulerHelper.countdown(plugin, 10, (remaining) -> {
            Bukkit.broadcastMessage(ColorConverter.colorize(
                "&e&lGame starting in &c" + remaining + " &e&lseconds!"
            ));
            
            // Play sound
            for (Player p : Bukkit.getOnlinePlayers()) {
                SoundPlayer.play(p, "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
            }
        }, this::startGame);
    }
    
    private void startGame() {
        Bukkit.broadcastMessage(ColorConverter.colorize("&a&lGAME STARTED!"));
        gameTime = 0;
        
        // Game timer (updates every second)
        gameTask = SchedulerHelper.runTimerSeconds(plugin, () -> {
            gameTime++;
            updateGameScoreboard();
            
            // End game after 5 minutes
            if (gameTime >= 300) {
                endGame();
            }
        }, 0.0, 1.0);
        
        // Async stat updates (every 10 seconds)
        SchedulerHelper.runAsyncTimerSeconds(plugin, () -> {
            updateDatabaseStats();
        }, 10.0, 10.0);
    }
    
    private void endGame() {
        if (gameTask != null) {
            gameTask.cancel();
        }
        
        Bukkit.broadcastMessage(ColorConverter.colorize("&c&lGAME ENDED!"));
        
        // Award prizes after delay
        SchedulerHelper.runLaterSeconds(plugin, () -> {
            awardPrizes();
        }, 3.0);
    }
    
    public void scheduleSave() {
        // Auto-save every 5 minutes
        SchedulerHelper.runTimerMinutes(plugin, () -> {
            SchedulerHelper.runAsync(plugin, () -> {
                saveAllData();
                ConsoleLog.info("Auto-save completed");
            });
        }, 5.0, 5.0);
    }
}
```

---

## 15. ScoreBoards

**Purpose**: Easy scoreboard creation and management for per-player scoreboards.

### Creating Scoreboards

```java
// Create basic scoreboard
Objective objective = ScoreBoards.createScoreboard(player, "&6&lMy Server");

// Create with initial lines
ScoreBoards.createScoreboard(player, "&6&lMy Server",
    "&7",
    "&eKills: &f0",
    "&eDeaths: &f0",
    "&7",
    "&bplay.server.com"
);
```

### Managing Lines

```java
// Set a line (line 1 = bottom, higher = up)
ScoreBoards.setLine(player, 5, "&ePlayer: &f" + player.getName());
ScoreBoards.setLine(player, 4, "&7");
ScoreBoards.setLine(player, 3, "&aCoins: &f" + coins);
ScoreBoards.setLine(player, 2, "&cKills: &f" + kills);
ScoreBoards.setLine(player, 1, "&7play.server.com");

// Update existing line
ScoreBoards.updateLine(player, 3, "&aCoins: &f" + newCoins);

// Remove a line
ScoreBoards.removeLine(player, 4);
```

### Scoreboard Title

```java
// Change title
ScoreBoards.setTitle(player, "&c&lNew Title");

// Animated title example
SchedulerHelper.runTimerSeconds(plugin, new Runnable() {
    private int state = 0;
    private final String[] titles = {
        "&6&lMy Server",
        "&e&lMy Server",
        "&6&lMy Server"
    };
    
    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ScoreBoards.setTitle(p, titles[state]);
        }
        state = (state + 1) % titles.length;
    }
}, 0.0, 0.5);
```

### Visibility Management

```java
// Show scoreboard to player
ScoreBoards.showToPlayer(player);

// Hide scoreboard from player
ScoreBoards.hideFromPlayer(player);

// Toggle visibility
boolean visible = ScoreBoards.isVisible(player);
if (visible) {
    ScoreBoards.hideFromPlayer(player);
} else {
    ScoreBoards.showToPlayer(player);
}
```

### Clearing & Removal

```java
// Clear all lines (keeps scoreboard)
ScoreBoards.clearLines(player);

// Remove scoreboard completely
ScoreBoards.removeScoreboard(player);

// Clear scoreboard (alias for clearLines)
ScoreBoards.clearScoreboard(player);
```

### Queries

```java
// Check if player has scoreboard
if (ScoreBoards.hasScoreboard(player)) {
    // Player has active scoreboard
}

// Get line content
String lineText = ScoreBoards.getLine(player, 3);

// Get all lines
Map<Integer, String> lines = ScoreBoards.getAllLines(player);
```

### Animated Scoreboards

```java
// Create animated scoreboard system
public class AnimatedScoreboard {
    
    private int updateTicks = 0;
    
    public void start() {
        SchedulerHelper.runTimerSeconds(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
            updateTicks++;
        }, 0.0, 0.5); // Update every 0.5 seconds
    }
    
    private void updateScoreboard(Player player) {
        if (!ScoreBoards.hasScoreboard(player)) {
            ScoreBoards.createScoreboard(player, getAnimatedTitle());
        }
        
        // Animated title
        ScoreBoards.setTitle(player, getAnimatedTitle());
        
        // Dynamic lines
        ScoreBoards.setLine(player, 10, "&7" + getDate());
        ScoreBoards.setLine(player, 9, "&7");
        ScoreBoards.setLine(player, 8, "&ePlayer: &f" + player.getName());
        ScoreBoards.setLine(player, 7, "&7");
        ScoreBoards.setLine(player, 6, "&aCoins: &f" + getCoins(player));
        ScoreBoards.setLine(player, 5, "&cKills: &f" + getKills(player));
        ScoreBoards.setLine(player, 4, "&9Deaths: &f" + getDeaths(player));
        ScoreBoards.setLine(player, 3, "&7");
        ScoreBoards.setLine(player, 2, "&6Rank: " + getRank(player));
        ScoreBoards.setLine(player, 1, "&7");
        ScoreBoards.setLine(player, 0, getAnimatedIP());
    }
    
    private String getAnimatedTitle() {
        String[] colors = {"&6", "&e", "&6", "&e"};
        int index = (updateTicks / 2) % colors.length;
        return colors[index] + "&lMY SERVER";
    }
    
    private String getAnimatedIP() {
        String[] frames = {
            "&bplay.server.com",
            "&3play.server.com",
            "&bplay.server.com"
        };
        int index = (updateTicks / 4) % frames.length;
        return frames[index];
    }
}
```

### Complete Example - Game Stats

```java
public class GameStatsBoard {
    
    public void createStatsBoard(Player player) {
        ScoreBoards.createScoreboard(player, "&6&lBED WARS");
        updateStats(player);
    }
    
    public void updateStats(Player player) {
        GameStats stats = getPlayerStats(player);
        
        ScoreBoards.setLine(player, 10, "&7" + getCurrentDate());
        ScoreBoards.setLine(player, 9, "&r");
        ScoreBoards.setLine(player, 8, "&fTeam: " + getTeamColor(player) + getTeamName(player));
        ScoreBoards.setLine(player, 7, "&r");
        ScoreBoards.setLine(player, 6, "&fKills: &a" + stats.kills);
        ScoreBoards.setLine(player, 5, "&fFinal Kills: &c" + stats.finalKills);
        ScoreBoards.setLine(player, 4, "&fBeds Broken: &e" + stats.bedsBroken);
        ScoreBoards.setLine(player, 3, "&r");
        
        // Show alive teams
        int line = 2;
        for (Team team : getAliveTeams()) {
            String status = team.hasBed() ? "&a✓" : "&c✗";
            ScoreBoards.setLine(player, line--, 
                team.getColor() + team.getName() + " " + status);
        }
        
        ScoreBoards.setLine(player, 1, "&r");
        ScoreBoards.setLine(player, 0, "&eplay.server.com");
    }
    
    public void removeStatsBoard(Player player) {
        ScoreBoards.removeScoreboard(player);
    }
}
```

---

## 16. SoundPlayer

**Purpose**: Version-aware sound playing with automatic fallbacks for older Minecraft versions.

### Basic Sound Playing

```java
// Play sound to player
SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP");

// Play with volume and pitch
SoundPlayer.play(player, "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.5f);
// volume: 1.0 = normal, pitch: 1.0 = normal (higher = faster/higher pitch)

// Play sound from Sound enum
Sound sound = Sound.ENTITY_PLAYER_LEVELUP;
SoundPlayer.play(player, sound);
SoundPlayer.play(player, sound, 0.5f, 1.0f); // Quieter
```

### Playing at Locations

```java
// Play sound at location (all nearby players hear it)
Location loc = player.getLocation();
SoundPlayer.playAt(loc, "BLOCK_ANVIL_USE", 1.0f, 1.0f);

// Play sound at coordinates
SoundPlayer.playAt(player.getWorld(), 100, 64, 200, "ENTITY_LIGHTNING_BOLT_THUNDER");

// Play for all players in radius
SoundPlayer.playInRadius(loc, "ENTITY_FIREWORK_ROCKET_BLAST", 50.0, 1.0f, 1.0f);
```

### Sound Categories

```java
// Play with specific category (1.11+)
SoundPlayer.play(player, "ENTITY_VILLAGER_YES", SoundCategory.VOICE, 1.0f, 1.0f);
SoundPlayer.play(player, "MUSIC_DISC_CAT", SoundCategory.MUSIC, 0.5f, 1.0f);
SoundPlayer.play(player, "ENTITY_ZOMBIE_AMBIENT", SoundCategory.HOSTILE, 1.0f, 0.8f);
```

### Sound from Config

```java
// Play sound configured in config.yml
// Config: sounds.join: "ENTITY_PLAYER_LEVELUP"
SoundPlayer.playFromConfig(plugin, player, "sounds.join");

// With volume and pitch from config
// Config: sounds.error: {sound: "BLOCK_ANVIL_LAND", volume: 1.0, pitch: 0.5}
SoundPlayer.playFromConfig(plugin, player, "sounds.error");
```

### Stop Sounds

```java
// Stop specific sound for player
SoundPlayer.stopSound(player, "MUSIC_DISC_CAT");

// Stop all sounds for player
SoundPlayer.stopAllSounds(player);

// Stop sounds in category
SoundPlayer.stopSounds(player, SoundCategory.MUSIC);
```

### Sound Utilities

```java
// Get Sound enum from string (with fallbacks for old versions)
Sound sound = SoundPlayer.getSound("ENTITY_PLAYER_LEVELUP");
if (sound != null) {
    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
}

// List all available sounds
List<String> sounds = SoundPlayer.getAllSoundNames();
for (String soundName : sounds) {
    ConsoleLog.info("Available: " + soundName);
}

// Check if sound exists
if (SoundPlayer.soundExists("ENTITY_PLAYER_LEVELUP")) {
    // Sound is available in this version
}
```

### Complete Example - Sound Manager

```java
public class SoundManager {
    
    private final Map<String, SoundConfig> configuredSounds = new HashMap<>();
    
    public void loadSounds() {
        // Load from config
        configuredSounds.put("success", new SoundConfig(
            "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f
        ));
        configuredSounds.put("error", new SoundConfig(
            "BLOCK_ANVIL_LAND", 1.0f, 0.5f
        ));
        configuredSounds.put("click", new SoundConfig(
            "UI_BUTTON_CLICK", 0.5f, 1.0f
        ));
        configuredSounds.put("purchase", new SoundConfig(
            "ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.2f
        ));
    }
    
    public void playSuccess(Player player) {
        playConfiguredSound(player, "success");
    }
    
    public void playError(Player player) {
        playConfiguredSound(player, "error");
    }
    
    public void playClick(Player player) {
        playConfiguredSound(player, "click");
    }
    
    private void playConfiguredSound(Player player, String key) {
        SoundConfig config = configuredSounds.get(key);
        if (config != null) {
            SoundPlayer.play(player, config.sound, config.volume, config.pitch);
        }
    }
    
    public void playCountdown(Player player, int seconds) {
        for (int i = seconds; i > 0; i--) {
            final int count = i;
            SchedulerHelper.runLaterSeconds(plugin, () -> {
                float pitch = 1.0f + (0.1f * (seconds - count));
                SoundPlayer.play(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, pitch);
            }, seconds - i);
        }
        
        // Final sound
        SchedulerHelper.runLaterSeconds(plugin, () -> {
            SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.5f);
        }, seconds);
    }
    
    public void playLevelUpSequence(Player player) {
        SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 0.8f);
        SchedulerHelper.runLater(plugin, () -> {
            SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f);
        }, 5L);
        SchedulerHelper.runLater(plugin, () -> {
            SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.2f);
        }, 10L);
    }
    
    private static class SoundConfig {
        String sound;
        float volume;
        float pitch;
        
        SoundConfig(String sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
```

### Common Sound Examples

```java
// UI Sounds
SoundPlayer.play(player, "UI_BUTTON_CLICK"); // Button click
SoundPlayer.play(player, "BLOCK_NOTE_BLOCK_PLING"); // Success ding
SoundPlayer.play(player, "BLOCK_ANVIL_LAND", 1.0f, 0.5f); // Error thud

// Player Events
SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP"); // Level up
SoundPlayer.play(player, "ENTITY_EXPERIENCE_ORB_PICKUP"); // Collect item
SoundPlayer.play(player, "ENTITY_PLAYER_HURT"); // Damage taken
SoundPlayer.play(player, "ENTITY_PLAYER_DEATH"); // Death

// Combat
SoundPlayer.play(player, "ENTITY_ARROW_SHOOT"); // Arrow shot
SoundPlayer.play(player, "ENTITY_ARROW_HIT"); // Arrow hit
SoundPlayer.play(player, "ENTITY_PLAYER_ATTACK_STRONG"); // Critical hit
SoundPlayer.play(player, "ITEM_SHIELD_BLOCK"); // Block attack

// World
SoundPlayer.play(player, "ENTITY_LIGHTNING_BOLT_THUNDER"); // Thunder
SoundPlayer.play(player, "ENTITY_GENERIC_EXPLODE"); // Explosion
SoundPlayer.play(player, "BLOCK_PORTAL_TRAVEL"); // Portal
SoundPlayer.play(player, "BLOCK_CHEST_OPEN"); // Open chest

// Ambient
SoundPlayer.play(player, "ENTITY_VILLAGER_YES", 1.0f, 1.2f); // Success
SoundPlayer.play(player, "ENTITY_VILLAGER_NO", 1.0f, 0.8f); // Failure
SoundPlayer.play(player, "ENTITY_ENDERMAN_TELEPORT"); // Teleport
```

---

## 17. TabCompleter

**Purpose**: Easy tab completion registration for commands.

### Basic Tab Completion

```java
// Simple static completions
TabCompleter.register(plugin, "mycmd", "start", "stop", "reload", "help");

// Player types "/mycmd st" → suggests "start", "stop"
```

### Dynamic Completions

```java
// Completion based on sender
TabCompleter.register(plugin, "mycmd", sender -> {
    if (sender.hasPermission("admin")) {
        return Arrays.asList("start", "stop", "reload", "admin");
    } else {
        return Arrays.asList("start", "help");
    }
});
```

### Argument-Based Completions

```java
// Different completions for each argument
TabCompleter.register(plugin, "teleport", (sender, args) -> {
    if (args.length == 1) {
        // First argument: player names
        return PlayerGather.getOnlinePlayerNames();
    } else if (args.length == 2) {
        // Second argument: world names
        return Bukkit.getWorlds().stream()
            .map(World::getName)
            .collect(Collectors.toList());
    }
    return Collections.emptyList();
});
```

### Context-Aware Completions

```java
TabCompleter.register(plugin, "item", (sender, args) -> {
    if (!(sender instanceof Player)) {
        return Collections.emptyList();
    }
    
    Player player = (Player) sender;
    
    if (args.length == 1) {
        // Subcommands
        return Arrays.asList("give", "take", "clear", "list");
    } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
        // Material names for "give" subcommand
        return Arrays.stream(Material.values())
            .map(Material::name)
            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
            .limit(20)
            .collect(Collectors.toList());
    } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
        // Amount suggestions
        return Arrays.asList("1", "16", "32", "64");
    }
    
    return Collections.emptyList();
});
```

### Advanced Filtering

```java
// Custom completer with filtering
TabCompleter.register(plugin, "warp", (sender, args) -> {
    if (args.length != 1) return Collections.emptyList();
    
    String partial = args[0].toLowerCase();
    List<String> warps = getAvailableWarps(sender);
    
    // Filter by partial input
    return warps.stream()
        .filter(warp -> warp.toLowerCase().startsWith(partial))
        .sorted()
        .collect(Collectors.toList());
});
```

### Complete Example - Admin Command

```java
public class AdminCommand {
    
    public void registerCommand() {
        // Register command
        CommandHelper.register(plugin, "admin", this::handleCommand);
        
        // Register tab completion
        TabCompleter.register(plugin, "admin", this::tabComplete);
    }
    
    private boolean handleCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /admin <subcommand>");
            return false;
        }
        
        switch (args[0].toLowerCase()) {
            case "teleport":
                return handleTeleport(sender, args);
            case "gamemode":
                return handleGamemode(sender, args);
            case "give":
                return handleGive(sender, args);
            case "ban":
                return handleBan(sender, args);
            default:
                sender.sendMessage("Unknown subcommand: " + args[0]);
                return false;
        }
    }
    
    private List<String> tabComplete(CommandSender sender, String[] args) {
        // First argument: subcommands
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                "teleport", "gamemode", "give", "ban", "kick", "reload"
            );
            return filterByInput(subcommands, args[0]);
        }
        
        // Second argument depends on subcommand
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "teleport":
                case "gamemode":
                case "give":
                case "ban":
                case "kick":
                    // Player names
                    return filterByInput(PlayerGather.getOnlinePlayerNames(), args[1]);
            }
        }
        
        // Third argument
        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "gamemode":
                    // Gamemode names
                    return filterByInput(
                        Arrays.asList("survival", "creative", "adventure", "spectator"),
                        args[2]
                    );
                case "give":
                    // Material names (limited)
                    return Arrays.stream(Material.values())
                        .map(Material::name)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .limit(20)
                        .collect(Collectors.toList());
            }
        }
        
        // Fourth argument for give: amount
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1", "16", "32", "64");
        }
        
        return Collections.emptyList();
    }
    
    private List<String> filterByInput(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
            .filter(opt -> opt.toLowerCase().startsWith(lower))
            .collect(Collectors.toList());
    }
}
```

### Helper Methods

```java
// Create simple completer from array
TabCompleter.ArgsCompleter completer = TabCompleter.simple("option1", "option2", "option3");

// Use the completer
TabCompleter.register(plugin, "mycmd", completer);
```

### Multi-Level Completions

```java
public class EconomyCommand {
    
    public void registerCompletion() {
        TabCompleter.register(plugin, "economy", (sender, args) -> {
            // /economy <give|take|set|balance> <player> [amount]
            
            if (args.length == 1) {
                return filterMatches(
                    Arrays.asList("give", "take", "set", "balance", "top"),
                    args[0]
                );
            }
            
            if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if (sub.equals("give") || sub.equals("take") || 
                    sub.equals("set") || sub.equals("balance")) {
                    return filterMatches(PlayerGather.getOnlinePlayerNames(), args[1]);
                }
            }
            
            if (args.length == 3) {
                String sub = args[0].toLowerCase();
                if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                    return Arrays.asList("100", "500", "1000", "5000", "10000");
                }
            }
            
            return Collections.emptyList();
        });
    }
    
    private List<String> filterMatches(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
            .filter(opt -> opt.toLowerCase().startsWith(lower))
            .sorted()
            .collect(Collectors.toList());
    }
}
```

---

## 18. UUIDhelp

**Purpose**: UUID/username lookup with caching and Mojang API integration.

### Basic UUID Operations

```java
// Get UUID from player name
UUID uuid = UUIDhelp.getUUID("Notch");
if (uuid != null) {
    ConsoleLog.info("UUID: " + uuid.toString());
}

// Get username from UUID
UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
String name = UUIDhelp.getUsername(uuid);
ConsoleLog.info("Username: " + name);
```

### Async Operations

```java
// Get UUID asynchronously (doesn't block main thread)
UUIDhelp.getUUIDAsync("Notch", uuid -> {
    if (uuid != null) {
        ConsoleLog.info("Found UUID: " + uuid);
        // Continue with UUID
        loadPlayerData(uuid);
    } else {
        ConsoleLog.warn("Player not found");
    }
});

// Get username asynchronously
UUID uuid = player.getUniqueId();
UUIDhelp.getUsernameAsync(uuid, name -> {
    if (name != null) {
        player.sendMessage("Your username: " + name);
    }
});
```

### UUID Validation

```java
// Check if string is valid UUID format
if (UUIDhelp.isValidUUID("069a79f4-44e9-4726-a5be-fca90e38aaf5")) {
    // Valid UUID format
}

// Parse UUID string (returns null if invalid)
UUID uuid = UUIDhelp.parseUUID("069a79f4-44e9-4726-a5be-fca90e38aaf5");

// Parse without dashes
UUID uuid = UUIDhelp.parseUUID("069a79f444e94726a5befca90e38aaf5");
```

### Offline UUID

```java
// Generate offline mode UUID (for offline servers)
UUID offlineUuid = UUIDhelp.getOfflineUUID("PlayerName");

// This UUID is deterministic for the same name
UUID uuid1 = UUIDhelp.getOfflineUUID("Steve");
UUID uuid2 = UUIDhelp.getOfflineUUID("Steve");
// uuid1.equals(uuid2) == true
```

### Mojang API

```java
// Fetch from Mojang API directly
UUID mojangUuid = UUIDhelp.fetchUUIDFromMojang("Notch");

// Fetch username from Mojang
String mojangName = UUIDhelp.fetchUsernameFromMojang(uuid);

// Check if Mojang API is available
if (UUIDhelp.isMojangAPIAvailable()) {
    // Can query Mojang
}
```

### Cache Management

```java
// Cache is automatic, but you can manage it

// Manually cache a UUID
UUIDhelp.cacheUUID("Notch", uuid);

// Manually cache a username
UUIDhelp.cacheUsername(uuid, "Notch");

// Clear entire cache
UUIDhelp.clearCache();

// Clear specific player from cache
UUIDhelp.clearPlayerCache("Notch");

// Get cache size
int size = UUIDhelp.getCacheSize();
ConsoleLog.info("Cache contains " + size + " entries");
```

### Bulk Operations

```java
// Get multiple UUIDs at once
List<String> names = Arrays.asList("Notch", "jeb_", "Dinnerbone");
Map<String, UUID> results = UUIDhelp.bulkGetUUIDs(names);

for (Map.Entry<String, UUID> entry : results.entrySet()) {
    ConsoleLog.info(entry.getKey() + " -> " + entry.getValue());
}

// Get multiple usernames at once
List<UUID> uuids = Arrays.asList(uuid1, uuid2, uuid3);
Map<UUID, String> usernames = UUIDhelp.bulkGetUsernames(uuids);
```

### Complete Example - Player Profile System

```java
public class PlayerProfileManager {
    
    public void loadProfile(String playerName, Consumer<PlayerProfile> callback) {
        // Get UUID asynchronously
        UUIDhelp.getUUIDAsync(playerName, uuid -> {
            if (uuid == null) {
                ConsoleLog.warn("Player not found: " + playerName);
                callback.accept(null);
                return;
            }
            
            // Load profile from database
            SchedulerHelper.runAsync(plugin, () -> {
                PlayerProfile profile = loadFromDatabase(uuid);
                
                // Update username in profile if changed
                String currentName = UUIDhelp.getUsername(uuid);
                if (currentName != null && !currentName.equals(profile.getName())) {
                    profile.setName(currentName);
                    updateDatabase(profile);
                }
                
                // Return on main thread
                SchedulerHelper.run(plugin, () -> {
                    callback.accept(profile);
                });
            });
        });
    }
    
    public void findPlayerByUUID(CommandSender sender, String uuidString) {
        // Validate UUID
        if (!UUIDhelp.isValidUUID(uuidString)) {
            sender.sendMessage(ColorConverter.colorize("&cInvalid UUID format"));
            return;
        }
        
        UUID uuid = UUIDhelp.parseUUID(uuidString);
        
        // Get username
        UUIDhelp.getUsernameAsync(uuid, name -> {
            if (name != null) {
                sender.sendMessage(ColorConverter.colorize(
                    "&aPlayer: &f" + name + "\n&aUUID: &f" + uuid
                ));
            } else {
                sender.sendMessage(ColorConverter.colorize("&cPlayer not found"));
            }
        });
    }
    
    public void migrateOfflineToOnline() {
        ConsoleLog.info("Starting UUID migration...");
        
        // Get all offline players
        List<String> playerNames = getAllStoredPlayerNames();
        
        int migrated = 0;
        for (String name : playerNames) {
            // Get offline UUID
            UUID offlineUuid = UUIDhelp.getOfflineUUID(name);
            
            // Get online UUID
            UUID onlineUuid = UUIDhelp.getUUID(name);
            
            if (onlineUuid != null && !onlineUuid.equals(offlineUuid)) {
                migratePlayerData(offlineUuid, onlineUuid);
                migrated++;
            }
        }
        
        ConsoleLog.info("Migrated " + migrated + " player profiles");
    }
    
    public void showPlayerHistory(Player viewer, String targetName) {
        UUIDhelp.getUUIDAsync(targetName, uuid -> {
            if (uuid == null) {
                viewer.sendMessage("Player not found");
                return;
            }
            
            // Get name history from Mojang
            List<String> nameHistory = UUIDhelp.getNameHistory(uuid);
            
            viewer.sendMessage(ColorConverter.colorize("&6Name History for " + targetName));
            viewer.sendMessage(ColorConverter.colorize("&7UUID: " + uuid));
            
            for (int i = 0; i < nameHistory.size(); i++) {
                String prefix = (i == nameHistory.size() - 1) ? "&a→ " : "  ";
                viewer.sendMessage(ColorConverter.colorize(prefix + nameHistory.get(i)));
            }
        });
    }
}
```

### UUID Formatting

```java
// Convert UUID to string with dashes
UUID uuid = player.getUniqueId();
String formatted = UUIDhelp.formatUUID(uuid);
// "069a79f4-44e9-4726-a5be-fca90e38aaf5"

// Convert to string without dashes
String compact = UUIDhelp.compactUUID(uuid);
// "069a79f444e94726a5befca90e38aaf5"

// Parse both formats
UUID parsed1 = UUIDhelp.parseUUID("069a79f4-44e9-4726-a5be-fca90e38aaf5");
UUID parsed2 = UUIDhelp.parseUUID("069a79f444e94726a5befca90e38aaf5");
// Both work correctly
```

---

## 19. VersionDetector

**Purpose**: Detect server platform and Minecraft version.

### Platform Detection

```java
// Detect server platform
VersionDetector.ServerPlatform platform = VersionDetector.detectPlatform();

switch (platform) {
    case PAPER:
        ConsoleLog.info("Running on Paper");
        break;
    case SPIGOT:
        ConsoleLog.info("Running on Spigot");
        break;
    case PURPUR:
        ConsoleLog.info("Running on Purpur");
        break;
    case PUFFERFISH:
        ConsoleLog.info("Running on Pufferfish");
        break;
    case LEAF:
        ConsoleLog.info("Running on Leaf");
        break;
    case CRAFTBUKKIT:
        ConsoleLog.info("Running on CraftBukkit");
        break;
    default:
        ConsoleLog.warn("Unknown platform");
}
```

### Platform Checks

```java
// Check specific platforms
if (VersionDetector.isPaper()) {
    // Use Paper-specific features
    enablePaperFeatures();
}

if (VersionDetector.isPurpur()) {
    // Use Purpur-specific features
    enablePurpurFeatures();
}

if (VersionDetector.isSpigot()) {
    // Spigot compatible
}

if (VersionDetector.isPufferfish()) {
    // Pufferfish optimizations
}

if (VersionDetector.isLeaf()) {
    // Leaf features
}
```

### Minecraft Version

```java
// Get Minecraft version
String mcVersion = VersionDetector.getMinecraftVersion();
ConsoleLog.info("Minecraft version: " + mcVersion); // e.g., "1.20.1"

// Parse version for comparison
String[] parts = mcVersion.split("\\.");
int major = Integer.parseInt(parts[0]); // 1
int minor = Integer.parseInt(parts[1]); // 20
int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0; // 1

// Check if version is 1.16+
if (minor >= 16) {
    // Hex colors supported
    enableHexColors();
}
```

### Complete Example - Feature Manager

```java
public class FeatureManager {
    
    private boolean hexColorsSupported = false;
    private boolean componentsSupported = false;
    private boolean persistentDataSupported = false;
    
    public void initialize() {
        detectFeatures();
        logServerInfo();
    }
    
    private void detectFeatures() {
        String version = VersionDetector.getMinecraftVersion();
        
        // Parse version
        String[] parts = version.split("\\.");
        if (parts.length >= 2) {
            try {
                int minor = Integer.parseInt(parts[1]);
                
                // Hex colors: 1.16+
                hexColorsSupported = minor >= 16;
                
                // Modern components: 1.13+
                componentsSupported = minor >= 13;
                
                // PersistentDataContainer: 1.14+
                persistentDataSupported = minor >= 14;
                
            } catch (NumberFormatException e) {
                ConsoleLog.warn("Failed to parse version: " + version);
            }
        }
        
        // Check platform-specific features
        if (VersionDetector.isPaper()) {
            // Paper has additional features
            ConsoleLog.info("Paper detected - enabling Paper features");
        }
    }
    
    private void logServerInfo() {
        ConsoleLog.info("=== Server Information ===");
        ConsoleLog.info("Platform: " + VersionDetector.detectPlatform());
        ConsoleLog.info("Minecraft: " + VersionDetector.getMinecraftVersion());
        ConsoleLog.info("Hex Colors: " + (hexColorsSupported ? "Yes" : "No"));
        ConsoleLog.info("Components: " + (componentsSupported ? "Yes" : "No"));
        ConsoleLog.info("PDC: " + (persistentDataSupported ? "Yes" : "No"));
    }
    
    public String colorize(String text) {
        if (hexColorsSupported) {
            // Use hex color support
            return ColorConverter.colorize(text);
        } else {
            // Strip hex colors, use legacy only
            String noHex = text.replaceAll("&#[a-fA-F0-9]{6}", "");
            return ColorConverter.translateAmpersandToSection(noHex);
        }
    }
    
    public void sendMessage(Player player, String message) {
        if (componentsSupported && VersionDetector.isPaper()) {
            // Use Paper's modern component system
            sendComponentMessage(player, message);
        } else {
            // Use legacy chat
            player.sendMessage(colorize(message));
        }
    }
    
    public boolean canUsePersistentData() {
        return persistentDataSupported;
    }
}
```

### Platform-Specific Features

```java
public class PlatformFeatures {
    
    public void enablePlatformFeatures() {
        if (VersionDetector.isPaper()) {
            // Paper features
            enableAsyncChunkLoading();
            enableTimings();
        }
        
        if (VersionDetector.isPurpur()) {
            // Purpur has all Paper features plus more
            enablePurpurConfig();
            enablePurpurGameplay();
        }
        
        if (VersionDetector.isPufferfish()) {
            // Pufferfish optimizations
            ConsoleLog.info("Pufferfish optimizations active");
        }
    }
    
    public void checkCompatibility() {
        VersionDetector.ServerPlatform platform = VersionDetector.detectPlatform();
        
        switch (platform) {
            case PAPER:
            case PURPUR:
            case PUFFERFISH:
            case LEAF:
                ConsoleLog.info("✓ Full compatibility");
                break;
            case SPIGOT:
                ConsoleLog.warn("⚠ Limited features on Spigot");
                break;
            case CRAFTBUKKIT:
                ConsoleLog.error("✗ CraftBukkit not recommended");
                break;
            default:
                ConsoleLog.warn("? Unknown platform - may have issues");
        }
    }
}
```

---

## 20. VisualCreator

**Purpose**: Create visual effects including particles, fireworks, titles, action bars, and boss bars.

### Particles

```java
// Spawn particle at location
Location loc = player.getLocation();
VisualCreator.spawnParticle(loc, Particle.FLAME, 10);

// Spawn with offset (spread)
VisualCreator.spawnParticle(loc, Particle.HEART, 5, 0.5, 0.5, 0.5);

// Spawn with speed
VisualCreator.spawnParticle(loc, Particle.EXPLOSION_LARGE, 1, 0, 0, 0, 0.1);

// Using particle name (version-aware)
VisualCreator.spawnParticle(loc, "FLAME", 10);
```

### Particle Shapes

```java
// Spawn particles in circle
Location center = player.getLocation().add(0, 1, 0);
VisualCreator.spawnParticleCircle(center, Particle.FLAME, 2.0, 20);
// radius = 2.0, points = 20

// Spawn particles in line
Location start = player.getLocation();
Location end = player.getLocation().add(10, 0, 0);
VisualCreator.spawnParticleLine(start, end, Particle.REDSTONE, 0.5);
// spacing = 0.5 blocks

// Spawn particles in sphere
VisualCreator.spawnParticleSphere(center, Particle.ENCHANTMENT_TABLE, 2.0, 50);

// Spawn particles in helix
VisualCreator.spawnParticleHelix(center, Particle.PORTAL, 2.0, 3.0, 50);
// radius = 2.0, height = 3.0, points = 50
```

### Fireworks

```java
// Create firework effect
FireworkEffect effect = FireworkEffect.builder()
    .with(FireworkEffect.Type.BALL_LARGE)
    .withColor(Color.RED, Color.YELLOW)
    .withFade(Color.ORANGE)
    .withFlicker()
    .withTrail()
    .build();

// Spawn firework
VisualCreator.spawnFirework(player.getLocation(), effect);

// Spawn instant firework (explodes immediately)
VisualCreator.spawnInstantFirework(player.getLocation(), effect);

// Predefined firework effects
VisualCreator.spawnRandomFirework(player.getLocation());
VisualCreator.spawnColorfulFirework(player.getLocation(), Color.BLUE, Color.WHITE);
```

### Titles

```java
// Send title to player
VisualCreator.sendTitle(player, "&6&lWelcome!", "&eEnjoy your stay");

// With timing (fadeIn, stay, fadeOut in ticks)
VisualCreator.sendTitle(player, "&a&lVICTORY!", "&7You won the game", 10, 70, 20);

// Title only
VisualCreator.sendTitle(player, "&c&lWARNING!");

// Subtitle only
VisualCreator.sendSubtitle(player, "&7This is a subtitle");

// Clear title
VisualCreator.clearTitle(player);
```

### Action Bar

```java
// Send action bar message
VisualCreator.sendActionBar(player, "&aHealth: &c❤ &f20/20");

// Update action bar repeatedly
BukkitTask task = SchedulerHelper.runTimerSeconds(plugin, () -> {
    double health = player.getHealth();
    double maxHealth = player.getMaxHealth();
    String bar = createHealthBar(health, maxHealth);
    VisualCreator.sendActionBar(player, bar);
}, 0, 0.5); // Update every 0.5 seconds
```

### Boss Bars

```java
// Create boss bar
BossBar bar = VisualCreator.createBossBar(
    "&c&lBoss Fight", 
    BarColor.RED, 
    BarStyle.SEGMENTED_10
);

// Add player to see boss bar
bar.addPlayer(player);

// Update boss bar
bar.setProgress(0.75); // 75%
bar.setTitle(ColorConverter.colorize("&c&lBoss: 75% HP"));

// Remove boss bar
bar.removeAll();

// Boss bar with automatic management
VisualCreator.showBossBar(player, "bossbar-key", 
    "&c&lBoss Fight", BarColor.RED, BarStyle.SOLID, 1.0);

// Update managed boss bar
VisualCreator.updateBossBar(player, "bossbar-key", 0.5); // 50%

// Remove managed boss bar
VisualCreator.removeBossBar(player, "bossbar-key");
```

### Color Utilities

```java
// Parse color from hex
Color color = VisualCreator.fromHex("#FF5733");

// Create RGB color
Color custom = VisualCreator.fromRGB(255, 87, 51);

// Predefined colors
Color red = VisualCreator.RED;
Color green = VisualCreator.GREEN;
Color blue = VisualCreator.BLUE;
```

### Complete Example - Effect Manager

```java
public class EffectManager {
    
    public void playLevelUpEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Particle circle
        VisualCreator.spawnParticleCircle(loc, Particle.VILLAGER_HAPPY, 1.5, 30);
        
        // Sound
        SoundPlayer.play(player, "ENTITY_PLAYER_LEVELUP");
        
        // Title
        VisualCreator.sendTitle(player, "&6&lLEVEL UP!", "&eYou reached level " + player.getLevel());
        
        // Firework
        FireworkEffect effect = FireworkEffect.builder()
            .with(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW, Color.ORANGE)
            .withFlicker()
            .build();
        VisualCreator.spawnInstantFirework(loc, effect);
    }
    
    public void playTeleportEffect(Location from, Location to) {
        // Departure effect
        VisualCreator.spawnParticleHelix(from.clone().add(0, 0.5, 0), 
            Particle.PORTAL, 1.0, 2.0, 40);
        SoundPlayer.playAt(from, "ENTITY_ENDERMAN_TELEPORT");
        
        // Arrival effect (delayed)
        SchedulerHelper.runLater(plugin, () -> {
            VisualCreator.spawnParticleSphere(to.clone().add(0, 1, 0),
                Particle.PORTAL, 1.5, 30);
            SoundPlayer.playAt(to, "ENTITY_ENDERMAN_TELEPORT", 1.0f, 1.2f);
        }, 10L);
    }
    
    public void showBossHealthBar(Player player, String bossName, double health, double maxHealth) {
        double progress = Mathematics.clamp(health / maxHealth, 0.0, 1.0);
        
        // Color based on health
        BarColor color;
        if (progress > 0.66) {
            color = BarColor.GREEN;
        } else if (progress > 0.33) {
            color = BarColor.YELLOW;
        } else {
            color = BarColor.RED;
        }
        
        String title = ColorConverter.colorize(
            "&c&l" + bossName + " &7- &f" + (int)health + "/" + (int)maxHealth
        );
        
        VisualCreator.showBossBar(player, "boss", title, color, BarStyle.SOLID, progress);
    }
    
    public void playWinEffect(Player winner) {
        Location loc = winner.getLocation().add(0, 2, 0);
        
        // Multiple fireworks
        for (int i = 0; i < 5; i++) {
            SchedulerHelper.runLater(plugin, () -> {
                FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(
                        Color.RED, Color.BLUE, Color.GREEN, 
                        Color.YELLOW, Color.PURPLE
                    )
                    .withFlicker()
                    .withTrail()
                    .build();
                VisualCreator.spawnFirework(loc, effect);
            }, i * 10L);
        }
        
        // Title
        VisualCreator.sendTitle(winner, "&6&lVICTORY!", "&e&lYou won the game!", 10, 80, 20);
        
        // Particle effect
        SchedulerHelper.runTimer(plugin, new BukkitRunnable() {
            private int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 60) {
                    cancel();
                    return;
                }
                Location particleLoc = winner.getLocation().add(0, 2, 0);
                VisualCreator.spawnParticleCircle(particleLoc, Particle.FIREWORKS_SPARK, 1.5, 20);
            }
        }, 0L, 2L);
    }
}
```

---

## 21. WorldEditor

**Purpose**: Simplify block placement, area filling, world management, and coordinate tracking.

### Basic Block Placement

```java
// Set block at location
Location loc = new Location(world, 100, 64, 200);
WorldEditor.setBlock(loc, Material.STONE);

// Set block with material name (version-aware)
WorldEditor.setBlock(loc, "STONE");

// Set block at coordinates
WorldEditor.setBlock(world, 100, 64, 200, Material.DIAMOND_BLOCK);
WorldEditor.setBlock(world, 100, 64, 200, "DIAMOND_BLOCK");
```

### Area Filling

```java
// Fill rectangular area (coordinates are normalized automatically)
int count = WorldEditor.fillArea(world, 
    0, 64, 0,    // corner 1
    10, 70, 10,  // corner 2
    Material.GLASS
);
ConsoleLog.info("Placed " + count + " blocks");

// Fill with material name
WorldEditor.fillArea(world, x1, y1, z1, x2, y2, z2, "STONE");

// Fill between two locations
Location pos1 = player.getLocation();
Location pos2 = player.getTargetBlock(null, 100).getLocation();
WorldEditor.fillArea(pos1, pos2, Material.STONE);
```

### Hollow Structures

```java
// Create hollow box (only walls, no interior)
WorldEditor.fillHollowBox(world, 
    0, 64, 0,
    10, 70, 10,
    Material.GLASS
);

// Create room with hollow box
WorldEditor.fillHollowBox(world, x1, y1, z1, x2, y2, z2, "STONE_BRICKS");
```

### Replacing Blocks

```java
// Replace specific material in area
int replaced = WorldEditor.replace(world, 
    x1, y1, z1, x2, y2, z2,
    Material.STONE,     // Replace this
    Material.DIAMOND_ORE // With this
);

// Replace multiple materials
WorldEditor.replaceMultiple(world, x1, y1, z1, x2, y2, z2,
    Arrays.asList(Material.DIRT, Material.GRASS_BLOCK),
    Material.STONE
);
```

### World Management

```java
// Get all worlds
List<World> worlds = WorldEditor.getAllWorlds();
for (World w : worlds) {
    ConsoleLog.info("World: " + w.getName());
}

// Get world by name
World world = WorldEditor.getWorld("world_nether");

// Get main world
World mainWorld = WorldEditor.getMainWorld();

// Create new world
World newWorld = WorldEditor.createWorld("custom_world", World.Environment.NORMAL);
```

### Player Locations

```java
// Get all player locations
Map<Player, Location> locations = WorldEditor.getAllPlayerLocations();
for (Map.Entry<Player, Location> entry : locations.entrySet()) {
    Player p = entry.getKey();
    Location loc = entry.getValue();
    ConsoleLog.info(p.getName() + " at " + loc.getBlockX() + "," + loc.getBlockZ());
}

// Get players in specific world
List<Player> playersInWorld = WorldEditor.getPlayersInWorld(world);

// Get players in radius
List<Player> nearby = WorldEditor.getPlayersInRadius(location, 50.0);
```

### Block Information

```java
// Get block at location
Block block = WorldEditor.getBlockAt(world, x, y, z);

// Get block type
Material type = WorldEditor.getBlockType(location);

// Check if block is air
if (WorldEditor.isAir(location)) {
    // Location is empty
}

// Check if block is solid
if (WorldEditor.isSolid(location)) {
    // Block is solid
}

// Get highest block at coordinates
Location highest = WorldEditor.getHighestBlock(world, x, z);
```

### Distance & Region Checks

```java
// Calculate distance between locations
double distance = WorldEditor.distance(loc1, loc2);

// Calculate 2D distance (ignoring Y)
double distance2D = WorldEditor.distance2D(loc1, loc2);

// Check if location is in region
boolean inRegion = WorldEditor.isInRegion(location, corner1, corner2);

// Check if location is in sphere
boolean inSphere = WorldEditor.isInSphere(location, center, radius);

// Get all blocks in radius
List<Block> blocks = WorldEditor.getBlocksInRadius(center, 5.0);
```

### Complete Example - Build Tools

```java
public class BuildTools {
    
    public void createPlatform(Player player, int size) {
        Location center = player.getLocation();
        World world = center.getWorld();
        int x = center.getBlockX();
        int y = center.getBlockY() - 1;
        int z = center.getBlockZ();
        
        // Create platform
        int placed = WorldEditor.fillArea(world,
            x - size, y, z - size,
            x + size, y, z + size,
            Material.STONE
        );
        
        player.sendMessage(ColorConverter.colorize(
            "&aCreated platform with " + placed + " blocks"
        ));
    }
    
    public void createHouse(Location corner) {
        World world = corner.getWorld();
        int x = corner.getBlockX();
        int y = corner.getBlockY();
        int z = corner.getBlockZ();
        
        // Floor
        WorldEditor.fillArea(world, x, y, z, x+10, y, z+10, "STONE");
        
        // Walls (hollow)
        WorldEditor.fillHollowBox(world, x, y+1, z, x+10, y+5, z+10, "STONE_BRICKS");
        
        // Roof
        WorldEditor.fillArea(world, x, y+6, z, x+10, y+6, z+10, "OAK_PLANKS");
        
        // Door
        WorldEditor.setBlock(world, x+5, y+1, z, Material.AIR);
        WorldEditor.setBlock(world, x+5, y+2, z, Material.AIR);
        
        ConsoleLog.info("House created at " + x + "," + y + "," + z);
    }
    
    public void clearArea(CommandSender sender, Location pos1, Location pos2) {
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            sender.sendMessage("Locations must be in same world!");
            return;
        }
        
        sender.sendMessage("Clearing area...");
        
        // Clear async to avoid lag
        SchedulerHelper.runAsync(plugin, () -> {
            int count = WorldEditor.fillArea(pos1, pos2, Material.AIR);
            
            // Send result on main thread
            SchedulerHelper.run(plugin, () -> {
                sender.sendMessage(ColorConverter.colorize(
                    "&aCleared " + count + " blocks"
                ));
            });
        });
    }
    
    public void copyRegion(Location from1, Location from2, Location to) {
        World fromWorld = from1.getWorld();
        World toWorld = to.getWorld();
        
        // Calculate dimensions
        int minX = Math.min(from1.getBlockX(), from2.getBlockX());
        int maxX = Math.max(from1.getBlockX(), from2.getBlockX());
        int minY = Math.min(from1.getBlockY(), from2.getBlockY());
        int maxY = Math.max(from1.getBlockY(), from2.getBlockY());
        int minZ = Math.min(from1.getBlockZ(), from2.getBlockZ());
        int maxZ = Math.max(from1.getBlockZ(), from2.getBlockZ());
        
        int offsetX = to.getBlockX() - minX;
        int offsetY = to.getBlockY() - minY;
        int offsetZ = to.getBlockZ() - minZ;
        
        // Copy blocks
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material type = WorldEditor.getBlockType(
                        new Location(fromWorld, x, y, z)
                    );
                    WorldEditor.setBlock(toWorld, 
                        x + offsetX, y + offsetY, z + offsetZ, 
                        type
                    );
                }
            }
        }
    }
    
    public void createCircle(Location center, int radius, Material material) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) {
                    WorldEditor.setBlock(world, 
                        centerX + x, centerY, centerZ + z, 
                        material
                    );
                }
            }
        }
    }
    
    public void createSphere(Location center, int radius, Material material) {
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius*radius) {
                        WorldEditor.setBlock(world, 
                            centerX + x, centerY + y, centerZ + z, 
                            material
                        );
                    }
                }
            }
        }
    }
    
    public void replaceInRegion(Player player, Location pos1, Location pos2, 
                                Material find, Material replace) {
        player.sendMessage("Replacing blocks...");
        
        SchedulerHelper.runAsync(plugin, () -> {
            int replaced = WorldEditor.replace(pos1.getWorld(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                find, replace
            );
            
            SchedulerHelper.run(plugin, () -> {
                player.sendMessage(ColorConverter.colorize(
                    "&aReplaced " + replaced + " blocks"
                ));
            });
        });
    }
}
```

### World Utilities

```java
// Set world spawn
WorldEditor.setWorldSpawn(world, new Location(world, 0, 100, 0));

// Get world spawn
Location spawn = WorldEditor.getWorldSpawn(world);

// Set world time
WorldEditor.setTime(world, 0); // Day
WorldEditor.setTime(world, 13000); // Night

// Set weather
WorldEditor.clearWeather(world);
WorldEditor.setStorm(world, true);
WorldEditor.setThundering(world, true);

// World border
WorldEditor.setWorldBorder(world, 1000.0); // Radius in blocks
WorldEditor.setWorldBorderCenter(world, 0, 0);
```

### Region Tools

```java
// Count blocks in region
int count = WorldEditor.countBlocks(world, x1, y1, z1, x2, y2, z2, Material.DIAMOND_ORE);

// Get block distribution in region
Map<Material, Integer> distribution = WorldEditor.getBlockDistribution(
    world, x1, y1, z1, x2, y2, z2
);

for (Map.Entry<Material, Integer> entry : distribution.entrySet()) {
    ConsoleLog.info(entry.getKey() + ": " + entry.getValue());
}

// Check if region is empty
if (WorldEditor.isRegionEmpty(world, x1, y1, z1, x2, y2, z2)) {
    // Region contains only air
}
```

---