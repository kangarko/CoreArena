package org.mineacademy.game.model;

import java.util.List;

import org.bukkit.Location;
import org.mineacademy.game.model.ArenaSign.SignType;

/**
 * Represents signs manager storing all signs
 */
public interface ArenaSigns {

	/**
	 * Find a sign at a location
	 *
	 * @param loc the location
	 * @return the sign, or null
	 */
	ArenaSign getSignAt(Location loc);

	/**
	 * Get all stored signs of a certain type
	 *
	 * @param type the sign type
	 * @return the signs
	 */
	List<ArenaSign> getSigns(SignType type);

	/**
	 * Calls {@link ArenaSign#updateState()} for all signs of a certain type in an arena
	 *
	 * @param type the sign type
	 * @param arena the arena
	 */
	void updateSigns(SignType type, Arena arena);
}