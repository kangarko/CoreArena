package org.mineacademy.game.command;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.game.impl.ArenaPlayer.InArenaCache;
import org.mineacademy.game.menu.MenuInArenaClasses;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.CoreUtil;

public class ClassCommand extends AbstractCoreSubcommand {

	public ClassCommand() {
		super("class|cl");

		setDescription("Select your class in an arena.");
	}

	@Override
	protected final void onCommand() {
		checkConsole();

		tryOpenMenu(getPlayer());
	}

	public static final void tryOpenMenu(Player player) {
		try {
			if (!CoreUtil.checkMenuAccessInArena(player))
				return;

		} catch (final CommandException ex) {
			return;
		}

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();
		final Arena arena = cache.getArena(player);

		if (arena.getSettings().allowOwnEquipment()) {
			Common.tell(player, Localization.Class.NOT_AVAILABLE.replace("{arena}", arena.getName()));

			return;
		}

		new MenuInArenaClasses(arena, player).displayTo(player);
	}
}