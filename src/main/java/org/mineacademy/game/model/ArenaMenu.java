package org.mineacademy.game.model;

import org.mineacademy.game.type.MenuType;

/**
 * Represents a menu from Menu library that is area menu
 *
 * In CoreArena/Puncher only used as a workaround to fire MenuOpenEvent
 */
public interface ArenaMenu {

	/**
	 * Get the menu type
	 *
	 * @return the menu type
	 */
	MenuType getMenuType();
}
