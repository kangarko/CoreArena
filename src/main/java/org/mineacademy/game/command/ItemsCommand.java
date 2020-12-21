package org.mineacademy.game.command;

import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.menu.MenuItems;

public final class ItemsCommand extends ItemMenuSubCommand {

	public ItemsCommand() {
		super("items|i");

		setDescription("Get items with special features while in game.");
	}

	@Override
	protected Menu getMenu() {
		return MenuItems.getInstance();
	}
}