package org.mineacademy.corearena.model;

import org.bukkit.Location;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.model.ConfigSerializable;

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
