package kaiakk.multimedia.classes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import java.util.*;

/**
 * ScoreBoards - Easy scoreboard creation and management.
 * Simple API for titles, lines, and per-player scoreboards.
 */
public final class ScoreBoards {

	private static final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
	private static final Map<UUID, Objective> playerObjectives = new HashMap<>();
	private static final Map<UUID, Map<Integer, String>> boardLines = new HashMap<>();

	private ScoreBoards() {}

	/**
	 * Create a new scoreboard for a player.
	 * @param player Target player
	 * @param title Scoreboard title
	 * @return Created objective
	 */
	public static Objective createScoreboard(Player player, String title) {
		if (player == null || title == null) return null;
		
		try {
			UUID uuid = player.getUniqueId();
			
			// Create new scoreboard
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			@SuppressWarnings("deprecation")
			Objective objective = board.registerNewObjective("multimedia", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
			
			// Store references
			playerBoards.put(uuid, board);
			playerObjectives.put(uuid, objective);
			boardLines.put(uuid, new HashMap<>());
			
			// Show to player
			player.setScoreboard(board);
			
			return objective;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to create scoreboard: " + t.getMessage());
			return null;
		}
	}

	/**
	 * Create a scoreboard with initial lines.
	 * @param player Target player
	 * @param title Scoreboard title
	 * @param lines Initial lines (highest line number = top)
	 * @return Created objective
	 */
	public static Objective createScoreboard(Player player, String title, String... lines) {
		Objective obj = createScoreboard(player, title);
		if (obj != null && lines != null) {
			for (int i = 0; i < lines.length; i++) {
				setLine(player, lines.length - i, lines[i]);
			}
		}
		return obj;
	}

	/**
	 * Set a line on a player's scoreboard.
	 * @param player Target player
	 * @param line Line number (1-15, higher = top)
	 * @param text Line text (supports color codes with &)
	 * @return true if successful
	 */
	public static boolean setLine(Player player, int line, String text) {
		if (player == null || line < 1 || line > 15) return false;
		
		try {
			UUID uuid = player.getUniqueId();
			Objective objective = playerObjectives.get(uuid);
			Map<Integer, String> lines = boardLines.get(uuid);
			
			if (objective == null || lines == null) {
				ConsoleLog.warn("Scoreboard not found for " + player.getName());
				return false;
			}
			
			// Remove old line if exists
			String oldText = lines.get(line);
			if (oldText != null) {
				objective.getScoreboard().resetScores(oldText);
			}
			
			// Add new line
			String colored = ChatColor.translateAlternateColorCodes('&', text);
			Score score = objective.getScore(colored);
			score.setScore(line);
			
			lines.put(line, colored);
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to set scoreboard line: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Update an existing line (alias for setLine).
	 * @param player Target player
	 * @param line Line number
	 * @param text New text
	 * @return true if successful
	 */
	public static boolean updateLine(Player player, int line, String text) {
		return setLine(player, line, text);
	}

	/**
	 * Remove a line from scoreboard.
	 * @param player Target player
	 * @param line Line number to remove
	 * @return true if successful
	 */
	public static boolean removeLine(Player player, int line) {
		if (player == null) return false;
		
		try {
			UUID uuid = player.getUniqueId();
			Objective objective = playerObjectives.get(uuid);
			Map<Integer, String> lines = boardLines.get(uuid);
			
			if (objective == null || lines == null) return false;
			
			String text = lines.remove(line);
			if (text != null) {
				objective.getScoreboard().resetScores(text);
				return true;
			}
			return false;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to remove scoreboard line: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Set multiple lines at once.
	 * @param player Target player
	 * @param lines Map of line number → text
	 * @return true if successful
	 */
	public static boolean setLines(Player player, Map<Integer, String> lines) {
		if (player == null || lines == null || lines.isEmpty()) return false;
		
		boolean success = true;
		for (Map.Entry<Integer, String> entry : lines.entrySet()) {
			if (!setLine(player, entry.getKey(), entry.getValue())) {
				success = false;
			}
		}
		return success;
	}

	/**
	 * Update scoreboard title.
	 * @param player Target player
	 * @param newTitle New title
	 * @return true if successful
	 */
	public static boolean setTitle(Player player, String newTitle) {
		if (player == null || newTitle == null) return false;
		
		try {
			UUID uuid = player.getUniqueId();
			Objective objective = playerObjectives.get(uuid);
			
			if (objective == null) {
				ConsoleLog.warn("Scoreboard not found for " + player.getName());
				return false;
			}
			
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', newTitle));
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to set scoreboard title: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Get current scoreboard title.
	 * @param player Target player
	 * @return Title or null
	 */
	public static String getTitle(Player player) {
		if (player == null) return null;
		
		Objective objective = playerObjectives.get(player.getUniqueId());
		return objective != null ? objective.getDisplayName() : null;
	}

	/**
	 * Show scoreboard to player.
	 * @param player Target player
	 * @return true if successful
	 */
	public static boolean showToPlayer(Player player) {
		if (player == null) return false;
		
		Scoreboard board = playerBoards.get(player.getUniqueId());
		if (board != null) {
			player.setScoreboard(board);
			return true;
		}
		return false;
	}

	/**
	 * Hide scoreboard from player (reset to main board).
	 * @param player Target player
	 * @return true if successful
	 */
	public static boolean hideFromPlayer(Player player) {
		if (player == null) return false;
		
		try {
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to hide scoreboard: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Toggle scoreboard visibility.
	 * @param player Target player
	 * @return true if now visible, false if hidden
	 */
	public static boolean toggleVisibility(Player player) {
		if (player == null) return false;
		
		Scoreboard current = player.getScoreboard();
		Scoreboard playerBoard = playerBoards.get(player.getUniqueId());
		
		if (current == playerBoard) {
			hideFromPlayer(player);
			return false;
		} else {
			showToPlayer(player);
			return true;
		}
	}

	/**
	 * Clear all lines from scoreboard (keep title).
	 * @param player Target player
	 * @return true if successful
	 */
	public static boolean clearLines(Player player) {
		if (player == null) return false;
		
		try {
			UUID uuid = player.getUniqueId();
			Objective objective = playerObjectives.get(uuid);
			Map<Integer, String> lines = boardLines.get(uuid);
			
			if (objective == null || lines == null) return false;
			
			// Remove all lines
			for (String text : lines.values()) {
				objective.getScoreboard().resetScores(text);
			}
			
			lines.clear();
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to clear scoreboard lines: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Completely remove scoreboard for a player.
	 * @param player Target player
	 * @return true if successful
	 */
	public static boolean clearScoreboard(Player player) {
		if (player == null) return false;
		
		try {
			UUID uuid = player.getUniqueId();
			
			hideFromPlayer(player);
			
			Objective objective = playerObjectives.remove(uuid);
			if (objective != null) {
				objective.unregister();
			}
			
			playerBoards.remove(uuid);
			boardLines.remove(uuid);
			
			return true;
		} catch (Throwable t) {
			ConsoleLog.warn("Failed to clear scoreboard: " + t.getMessage());
			return false;
		}
	}

	/**
	 * Remove all scoreboards (cleanup on plugin disable).
	 */
	public static void clearAllScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			clearScoreboard(player);
		}
		
		playerBoards.clear();
		playerObjectives.clear();
		boardLines.clear();
	}

	/**
	 * Check if player has a scoreboard.
	 * @param player Target player
	 * @return true if player has custom scoreboard
	 */
	public static boolean hasScoreboard(Player player) {
		if (player == null) return false;
		return playerBoards.containsKey(player.getUniqueId());
	}

	/**
	 * Get player's scoreboard.
	 * @param player Target player
	 * @return Scoreboard or null
	 */
	public static Scoreboard getScoreboard(Player player) {
		if (player == null) return null;
		return playerBoards.get(player.getUniqueId());
	}

	/**
	 * Get player's objective.
	 * @param player Target player
	 * @return Objective or null
	 */
	public static Objective getObjective(Player player) {
		if (player == null) return null;
		return playerObjectives.get(player.getUniqueId());
	}

	/**
	 * Get all lines from player's scoreboard.
	 * @param player Target player
	 * @return Map of line number → text
	 */
	public static Map<Integer, String> getLines(Player player) {
		if (player == null) return Collections.emptyMap();
		
		Map<Integer, String> lines = boardLines.get(player.getUniqueId());
		return lines != null ? new HashMap<>(lines) : Collections.emptyMap();
	}

	/**
	 * Get number of lines on scoreboard.
	 * @param player Target player
	 * @return Line count
	 */
	public static int getLineCount(Player player) {
		if (player == null) return 0;
		
		Map<Integer, String> lines = boardLines.get(player.getUniqueId());
		return lines != null ? lines.size() : 0;
	}

	/**
	 * Create an animated title that cycles through values.
	 * @param player Target player
	 * @param titles Titles to cycle through
	 * @param intervalTicks Ticks between changes
	 */
	public static void animateTitle(Player player, List<String> titles, long intervalTicks) {
		if (player == null || titles == null || titles.isEmpty()) return;
		
		final int[] index = {0};
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline() || !hasScoreboard(player)) {
					cancel();
					return;
				}
				
				setTitle(player, titles.get(index[0]));
				index[0] = (index[0] + 1) % titles.size();
			}
		}.runTaskTimer(SchedulerHelper.getPlugin(), 0L, intervalTicks);
	}

	/**
	 * Animate a specific line.
	 * @param player Target player
	 * @param lineNumber Line to animate
	 * @param texts Texts to cycle through
	 * @param intervalTicks Ticks between changes
	 */
	public static void animateLine(Player player, int lineNumber, List<String> texts, long intervalTicks) {
		if (player == null || texts == null || texts.isEmpty()) return;
		
		final int[] index = {0};
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline() || !hasScoreboard(player)) {
					cancel();
					return;
				}
				
				setLine(player, lineNumber, texts.get(index[0]));
				index[0] = (index[0] + 1) % texts.size();
			}
		}.runTaskTimer(SchedulerHelper.getPlugin(), 0L, intervalTicks);
	}
}
