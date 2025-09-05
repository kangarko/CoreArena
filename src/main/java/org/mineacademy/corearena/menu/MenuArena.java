package org.mineacademy.corearena.menu;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.conversation.AddNewConvo.Created;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;

public final class MenuArena extends AbstractListMenu {

	protected MenuArena() {
		super("Arena", CoreArenaPlugin.getArenaManager().getArenasNames());
	}

	@Override
	protected Button getListButton(String listName, int listIndex) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(listName);

		return new ButtonMenu(new IndividualArenaMenu(arena),

				ItemCreator.fromItemStack(arena.getData().getIcon())
						.name("&f" + listName)
						.lore(
								"",
								"&7Click to open arena menu."));
	}

	@Override
	protected Created getCreatedObject() {
		return Created.ARENA;
	}
}
