package org.mineacademy.game.model;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.game.type.RewardType;

/**
 * Represents a material reward that can be purchased for Nuggets
 * after the game is finished.
 */
public interface Reward {

	/**
	 * The reward type
	 */
	RewardType getType();

	/**
	 * The Nugget costs to obtain it
	 *
	 * @return the cost
	 */
	int getCost();

	/**
	 * Set a new Nugget cost to obtain it
	 *
	 * @param cost the new cost
	 */
	void setCost(int cost);

	/**
	 * Get the item for this reward
	 *
	 * @return the itemstack
	 */
	ItemStack getItem();

	/**
	 * Set the item for this reward
	 *
	 * @param item new item
	 */
	void setItem(ItemStack item);
}
