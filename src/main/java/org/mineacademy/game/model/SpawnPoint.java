package org.mineacademy.game.model;

import org.bukkit.Location;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.game.type.SpawnPointType;

/**
 * Represents a simple spawn point
 */
public interface SpawnPoint extends ConfigSerializable {

	/**
	 * Get the location of this spawn point
	 *
	 * @return the location
	 */
	Location getLocation();

	/**
	 * Get this spawn point type
	 *
	 * @return the type
	 */
	SpawnPointType getType();
}
