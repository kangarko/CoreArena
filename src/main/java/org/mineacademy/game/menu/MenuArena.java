package org.mineacademy.game.menu;

import java.util.Arrays;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.game.conversation.AddNewConvo.Created;
import org.mineacademy.game.model.Arena;

public final class MenuArena extends AbstractListMenu {

	protected MenuArena() {
		super("Arena", CoreArenaPlugin.getArenaManager().getAvailable());
	}

	@Override
	protected Button getListButton(String listName, int listIndex) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(listName);

		return new ButtonMenu(new IndividualArenaMenu(arena),

				ItemCreator.of(arena.getData().getIcon())
						.name("&f" + listName)
						.lores(Arrays.asList("", "&7Click to open arena menu.")));
	}

	@Override
	protected Created getCreatedObject() {
		return Created.ARENA;
	}
}
