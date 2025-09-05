package org.mineacademy.corearena.command;

import org.mineacademy.corearena.menu.MenuItems;
import org.mineacademy.fo.menu.Menu;

final class ItemsCommand extends ItemMenuSubCommand {

	public ItemsCommand() {
		super("items|i");

		this.setDescription("Get items with special features while in game.");
	}

	@Override
	protected Menu getMenu() {
		return MenuItems.getInstance();
	}
}