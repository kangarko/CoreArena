package org.mineacademy.corearena.model;

import org.bukkit.entity.Player;

import net.kyori.adventure.bossbar.BossBar;

/**
 * Represents the boss bar indicator, typicall used to display remaining phase time
 */
public interface BossBarIndicator {

	/**
	 * Display the bar to a player
	 *
	 * @param player the player
	 */
	void showTo(Player player);

	/**
	 * Hide the bar from a player
	 *
	 * @param player the player
	 */
	void hideFrom(Player player);

	/**
	 * Updates the title of this boss bar
	 *
	 * @param title the new title
	 */
	void updateTitle(String title);

	/**
	 * Updates progress of this boss bar
	 *
	 * @param progress the new progress
	 */
	void updateProgress(float progress);

	/**
	 * Updates the color of this boss bar
	 *
	 * @param color the new color
	 */
	void updateColor(BossBar.Color color);

	/**
	 * Completely hide the bar from everyone
	 */
	void hide();
}
