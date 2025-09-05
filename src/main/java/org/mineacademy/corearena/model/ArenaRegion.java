package org.mineacademy.corearena.model;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.mineacademy.fo.model.ConfigSerializable;

/**
 * Represents the protected arena region
 */
public interface ArenaRegion extends ConfigSerializable {

	/**
	 * Get the primary region point
	 *
	 * @return the point, or null if not set
	 */
	Location getPrimary();

	/**
	 * Get the secondary region point
	 *
	 * @return the point, or null if not set
	 */
	Location getSecondary();

	/**
	 * Get the estimated center point of this region
	 *
	 * Typically, this is used to teleport admins to the center of the arena.
	 *
	 * @return the center point, or null if region is incomplete
	 */
	default Location getCenter() {
		return this.getPrimary() != null && this.getSecondary() != null ? new Location(
				this.getPrimary().getWorld(),
				(this.getPrimary().getX() + this.getSecondary().getX()) / 2,
				(this.getPrimary().getY() + this.getSecondary().getY()) / 2,
				(this.getPrimary().getZ() + this.getSecondary().getZ()) / 2) : null;
	}

	/**
	 * Get all blocks in the arena region
	 *
	 * @return all region blocks
	 */
	List<Block> getBlocks();

	/**
	 * Get all entities (live) in the arena region
	 *
	 * @return all alive entities
	 */
	List<Entity> getEntities();

	/**
	 * Get the region world
	 *
	 * @return the world, first checks primary region, if that is null than attempts to get the secondary,
	 *         if both are null then returns null
	 */
	default World getWorld() {
		return this.getPrimary() != null ? this.getPrimary().getWorld() : this.getSecondary() != null ? this.getSecondary().getWorld() : null;
	}

	/**
	 * Return if the given location is within this arena's region
	 *
	 * @param loc the location
	 * @return true if the location is in this arena
	 */
	boolean isWithin(Location loc);

	/**
	 * Is this region having both points set?
	 *
	 * @return true if both primary and secondary points are set
	 */
	default boolean isComplete() {
		return this.getPrimary() != null && this.getSecondary() != null;
	}
}
