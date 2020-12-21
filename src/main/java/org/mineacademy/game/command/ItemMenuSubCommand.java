package org.mineacademy.game.command;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.Permissions;

abstract class ItemMenuSubCommand extends AbstractCoreSubcommand {

	public ItemMenuSubCommand(String aliases) {
		super(aliases);
	}

	@Override
	protected final void onCommand() {
		checkConsole();

		if (!CoreArenaPlugin.DEBUG_EDITING_MODE && !hasPerm(Permissions.Bypass.ARENA_COMMANDS) && CoreArenaPlugin.getDataFor(getPlayer()).hasArenaCache())
			returnTell(Localization.Commands.DISALLOWED_WHILE_PLAYING);

		final Menu menu = getMenu();

		if (menu != null)
			menu.displayTo(getPlayer());
	}

	protected abstract Menu getMenu();
}