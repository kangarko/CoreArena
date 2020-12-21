package org.mineacademy.game.model;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a menu item that can have an icon
 */
public interface Iconable {

	/**
	 * Get the icon
	 *
	 * @return the icon, or null if not set
	 */
	ItemStack getIcon();

	/**
	 * Is the icon set?
	 *
	 * @return true if the icon has been set
	 */
	boolean hasIcon();

	/**
	 * Set a new icon for this menu item
	 *
	 * @param icon the new icon, set to null to remove
	 */
	void setIcon(ItemStack icon);
}
