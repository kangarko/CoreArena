package org.mineacademy.game.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.game.type.TierMode;

/**
 * Represents a class tier
 */
public interface ClassTier extends ConfigSerializable {

	/**
	 * Get the tier level
	 *
	 * @return the tier level
	 */
	int getTier();

	/**
	 * Assign this class tier a new level
	 *
	 * @param tier the new level
	 *
	 * @deprecated unsafe. You should always make a new instance with another class tier
	 * and remove the old one instead of changing it. You hold full responsibility for
	 * handling everything else if you call this method (a lot)
	 */
	@Deprecated
	void setTier(int tier);

	/**
	 * Get the costs to update to this tier
	 *
	 * @return the cost, in Nuggets
	 */
	int getLevelCost();

	/**
	 * Set a new costs to update to this tier
	 *
	 * @param cost the new cost
	 */
	void setLevelCost(int cost);

	/**
	 * Get the inventory content for this tier
	 *
	 * @return the inventory content
	 */
	ItemStack[] getContent();

	/**
	 * Get the armor content for this tier
	 *
	 * @return the armor content
	 */
	ArmorContent getArmor();

	/**
	 * Give this tier to the player
	 *
	 * @param player the player
	 * @param mode, how we should give the items
	 */
	void giveToPlayer(Player player, TierMode mode);

	/**
	 * Called when the player leaves the arena so you can reverse changes
	 * such as permissions or special effects
	 *
	 * @param player the player
	 */
	void onArenaLeave(Player player);
}
