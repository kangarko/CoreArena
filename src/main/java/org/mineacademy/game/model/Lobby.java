package org.mineacademy.game.model;

import org.bukkit.Location;

/**
 * Represents the arena lobby
 */
public interface Lobby {

	/**
	 * Get the lobby location
	 *
	 * @return the location, or null if not exists
	 */
	Location getLocation();
}
