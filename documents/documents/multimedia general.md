# Multimedia - Plugin Development Library

## Overview

**Multimedia** is a comprehensive utility library for Bukkit/Spigot/Folia plugin development that dramatically simplifies common plugin tasks. It provides 21 pre-built utility classes covering everything from command registration to world editing, with full cross-version support from Minecraft 1.7.2 to 1.21+.

### Key Features

- ✅ **21 Utility Classes** - Commands, config, permissions, scheduling, sounds, scoreboards, and more
- ✅ **Cross-Version Compatible** - Works on Minecraft 1.7.2 through 1.21+ with automatic fallbacks
- ✅ **Folia Support** - Built-in regionized scheduler compatibility
- ✅ **Proxy Communication** - BungeeCord plugin messaging helpers
- ✅ **Zero Boilerplate** - Simple one-liner APIs that replace dozens of lines of code
- ✅ **Beginner Friendly** - Easy to learn, yet powerful for advanced use cases

### What Multimedia Provides

| Class | Purpose |
|-------|---------|
| **Base64** | Encode/decode Base64 strings, files, and resources |
| **ColorConverter** | Convert color codes, hex colors (1.16+), strip formatting |
| **CommandExecuter** | Execute commands as console or players, silently or with validation |
| **CommandHelper** | Simplified command registration with lambda support |
| **ConfigHelp** | Easy config.yml management with defaults and type-safe getters |
| **ConsoleLog** | Formatted logging with log levels (INFO, WARNING, ERROR) |
| **ErrorHandler** | Centralized error handling with stack traces |
| **InventoryHelper** | Inventory management, item serialization, GUI creation |
| **JavaUtilities** | System monitoring, memory management, thread utilities |
| **Mathematics** | Common math operations, random utilities, number formatting |
| **PermissionHandler** | Permission checks with fallbacks and group support |
| **PlayerGather** | Get online players, UUIDs, names with various filters |
| **ProxyListener** | BungeeCord plugin messaging from Bukkit side |
| **SchedulerHelper** | Simplified task scheduling (Bukkit/Folia compatible) |
| **ScoreBoards** | Easy scoreboard creation and management |
| **SoundPlayer** | Cross-version sound playing with string names |
| **TabCompleter** | Easy tab completion registration |
| **UUIDhelp** | UUID utilities and username lookups |
| **VersionDetector** | Detect Minecraft version, platform, and server type |
| **VisualCreator** | Particles, titles, action bars, boss bars |
| **WorldEditor** | Simplified world editing, block placement, area filling |

### Development Speed Multiplier

Multimedia can make plugin development **4-10x faster** depending on complexity:

- **Simple plugins** (commands, config, permissions): ~3-4x faster
- **Feature-rich plugins** (commands, config, schedulers, sounds, scoreboards): ~5-6x faster
- **Complex plugins** (cross-version, proxy, advanced features): ~8-10x faster

**Example:** A minigame plugin that would take 500-800 lines of boilerplate can be done in 100-150 lines of actual game logic.

---

## Installation & Setup

### **IMPORTANT: How to Add Multimedia**

Multimedia must be added as a **JAR file to your server**, not as a Gradle/Maven dependency. This is because it functions as a plugin library that other plugins depend on.

### Step 1: Add Multimedia JAR to Your Server

1. Download the `Multimedia.jar` file
2. Place it in your server's `plugins/` folder
3. Restart your server

### Step 2: Configure Your Plugin to Use Multimedia

#### In your `plugin.yml`:

```yaml
name: YourPlugin
version: 1.0
main: com.yourname.yourplugin.YourPlugin
depend: [Multimedia]  # Make Multimedia load before your plugin
```

#### In your main plugin class:

```java
package com.yourname.yourplugin;

import org.bukkit.plugin.java.JavaPlugin;
import kaiakk.multimedia.classes.*;

public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Initialize ConsoleLog first
        ConsoleLog.init(this);
        ConsoleLog.info("YourPlugin is starting...");
        
        // Initialize ConfigHelp
        ConfigHelp.init(this);
        
        // Set up config defaults
        java.util.Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("enabled", true);
        defaults.put("messages.welcome", "&aWelcome!");
        ConfigHelp.ensureDefaults(defaults);
        
        // Register commands
        CommandHelper.register(this, "yourcommand", (sender, label, args) -> {
            sender.sendMessage(ColorConverter.colorize("&aCommand executed!"));
            return true;
        });
        
        ConsoleLog.info("YourPlugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        ConsoleLog.info("YourPlugin disabled.");
    }
}
```

### Step 3: Add Multimedia to Your Build Path

To compile your plugin, you need to add Multimedia as a **compileOnly** dependency in your `build.gradle`:

```gradle
repositories {
    mavenCentral()
    maven { url 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }
}

dependencies {
    compileOnly 'org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT'
    
    // Add Multimedia JAR to compile against
    compileOnly files('libs/Multimedia-Bukkit-1.00.0.jar')
    // For Folia: compileOnly files('libs/Multimedia-Folia-1.00.0.jar')
}
```

**Where to put the JAR:**
- Create a `libs/` folder in your project root
- Place the Multimedia JAR file there
- Or use any path: `compileOnly files('path/to/Multimedia-Bukkit-1.00.0.jar')`

### Step 4: Build Your Plugin

1. Build your plugin: `gradle build` or `./gradlew build`
2. Your plugin JAR will be small (only your code)
3. Place **both** your plugin JAR **and** Multimedia JAR in the server's `plugins/` folder
4. The `depend: [Multimedia]` in plugin.yml ensures Multimedia loads first

### Why This Approach?

Multimedia is designed as a **server-side library plugin**, similar to how Vault or ProtocolLib works:

- ✅ Multiple plugins can share one Multimedia installation
- ✅ Updates to Multimedia don't require rebuilding dependent plugins
- ✅ Smaller plugin file sizes (no shading/relocation needed)
- ✅ Easier maintenance
- ✅ No version conflicts between plugins

---

## Quick Start Example

Here's a complete minimal plugin using Multimedia:

```java
package com.example.demo;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import kaiakk.multimedia.classes.*;

public class DemoPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Initialize logging
        ConsoleLog.init(this);
        ConsoleLog.info("Demo plugin starting...");
        
        // Initialize config with defaults
        ConfigHelp.init(this);
        java.util.Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("welcome-message", "&aWelcome to the server!");
        defaults.put("sound-enabled", true);
        ConfigHelp.ensureDefaults(defaults);
        
        // Register command
        CommandHelper.register(this, "demo", (sender, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player only command!");
                return true;
            }
            
            Player player = (Player) sender;
            
            // Send colored message
            String msg = ConfigHelp.getString("welcome-message", "Welcome!");
            player.sendMessage(ColorConverter.colorize(msg));
            
            // Play sound
            if (ConfigHelp.getBoolean("sound-enabled", true)) {
                SoundPlayer.play(player, "LEVEL_UP");
            }
            
            // Schedule task
            SchedulerHelper.runLaterSeconds(this, () -> {
                player.sendMessage("Delayed message after 3 seconds!");
            }, 3);
            
            return true;
        });
        
        ConsoleLog.info("Demo plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        ConsoleLog.info("Demo plugin disabled.");
    }
}
```

**plugin.yml:**
```yaml
name: DemoPlugin
version: 1.0
main: com.example.demo.DemoPlugin
depend: [Multimedia]
commands:
  demo:
    description: Demo command
```

---

## Supported Platforms

- **Bukkit**: 1.7.2+ (this includes Spigot, Paper, Purpur, etc.)
- **Folia**: 1.18+

---

## Additional Resources

- **Full Class Documentation**: See `multimedia usage BUKKIT.md` for detailed examples of all 21 classes
- **Support**: For Folia-specific features, check the Folia variant of classes

---

## Why Multimedia is Different

Most libraries either:
- ❌ Are too complex for beginners
- ❌ Have limited cross-version support
- ❌ Require shading/relocating dependencies
- ❌ Lack Folia compatibility

**Multimedia is:**
- ✅ Simple one-liner APIs
- ✅ 14+ Minecraft versions supported
- ✅ Zero dependencies to shade
- ✅ Folia-ready out of the box
- ✅ Comprehensive (21 classes covering everything)

---

## License

See `LICENSE` for details.
