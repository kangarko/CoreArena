package org.mineacademy.game.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.model.ConfigSerializable;

/**
 * Represents armor content in a class tier
 */
public interface ArmorContent extends ConfigSerializable {

	/**
	 * Get the helmet
	 *
	 * @return the item or null if not set
	 */
	ItemStack getHelmet();

	/**
	 * Get the chestplate
	 *
	 * @return the item or null if not set
	 */
	ItemStack getChestplate();

	/**
	 * Get the leggings
	 *
	 * @return the item or null if not set
	 */
	ItemStack getLeggings();

	/**
	 * Get the boots
	 *
	 * @return the item or null if not set
	 */
	ItemStack getBoots();

	/**
	 * Get the item at a certain position of the array
	 *
	 * Typically we store helmet, chestplate, leggings and boots in an array
	 * so calling getByOrder(0) would call items[0] from the array to get
	 * the helmet, and so on.
	 *
	 * @deprecated confusing
	 * @throws IllegalArgumentException if out of range of the array
	 * @return the item or null if not set
	 */
	@Deprecated
	ItemStack getByOrder(int order);

	/**
	 * Give the armor content to a player
	 *
	 * @param player the player to give to
	 */
	void giveTo(Player player);
}