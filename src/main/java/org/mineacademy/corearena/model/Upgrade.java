package org.mineacademy.corearena.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * An upgrade in-game enables the players to
 * purchase resources and enhancements during their arena stay.
 *
 * The upgrade is one-time buy and you buy all the resources specified
 * below. Those that are not set, are returned null, and are ignored.
 */
public interface Upgrade {

	/**
	 * The name of this upgrade.
	 * @return
	 */
	String getName();

	/**
	 * The permission to buy this upgrade.
	 * @return
	 */
	String getPermission();

	/**
	 * From which phase is this upgrade available?
	 * @return
	 */
	int getUnlockPhase();

	/**
	 * Custom items to give to the boy.
	 * @return
	 */
	ItemStack[] getItems();

	/**
	 * Set the new items for this upgrade
	 *
	 * @param items the new items
	 */
	void setItems(ItemStack[] items);

	/**
	 * Gives the upgrade to the player
	 *
	 * @param player the player
	 */
	void giveToPlayer(Player player);

	/**
	 * Permanently removes the upgrade
	 */
	void deleteUpgrade();
}
