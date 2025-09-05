package org.mineacademy.corearena.model;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.type.TierMode;

/**
 * Represents a player class.
 *
 * Typically, a class includes player inventory
 * with armor, and some special effects.
 */
public interface ArenaClass extends Iconable {

	/**
	 * The name of this class
	 *
	 * @return the name of this class
	 */
	String getName();

	/**
	 * The permission to access this class
	 *
	 * @return the permission, null if none
	 */
	String getPermission();

	/**
	 * Check if the player may obtain this class. Typically we check for {@link #getPermission()} here.
	 *
	 * @param player the player
	 * @return true if player is eligible for this class
	 */
	boolean mayObtain(Player player);

	/**
	 * Give this class (and all items) to the player, depending on his tier mode
	 *
	 * @param player the player
	 * @param mode the mode
	 * @param playEffects if true, play sound effects
	 */
	void giveToPlayer(Player player, TierMode mode, boolean playEffects);

	/**
	 * Get all tiers in this class. A tier represents how advanced or good the items/armor should be.
	 * A class can have multiple tiers each giving the player more advanced items, motivating him
	 * to purchase higher tiers for Nuggets.
	 *
	 * @return the tiers installed for this class
	 */
	int getTiers();

	/**
	 * Get the minimum valid tier. Since you can add/remove tiers in the game, it is easy to mess up
	 * and this method will safely return the lower tier in case you give it one that's too high
	 *
	 * @param level the tier to get, or it will return lower in case it does not exist
	 * @return the given tier, or the closes minimum that exists
	 */
	ClassTier getMinimumTier(int level);

	/**
	 * Get a class tier, or null if not present
	 *
	 * @param level the tier level
	 * @return the tier or null
	 */
	ClassTier getTier(int level);

	/**
	 * Add or update class tier. The tier level is found in the wrapper
	 *
	 * @param tier the tier
	 */
	void addOrUpdateTier(ClassTier tier);

	/**
	 * Remove a class tier
	 *
	 * @param tier the tier
	 */
	void removeTier(ClassTier tier);

	/**
	 * Since we store tier settings in a separate file, return them here for clarity
	 *
	 * @param level the tier level
	 * @return the settings of this tier level, or null
	 */
	TierSettings getTierSettings(int level);

	/**
	 * Is this class valid?
	 *
	 * @return true if the first tier is valid
	 */
	boolean isValid();

	/**
	 * Remove the class permanently
	 */
	void deleteClass();
}
