package org.mineacademy.corearena.command;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.settings.Lang;

abstract class ItemMenuSubCommand extends AbstractCoreSubcommand {

	public ItemMenuSubCommand(String aliases) {
		super(aliases);

		this.setValidArguments(0, 0);
	}

	@Override
	protected final void onCommand() {
		this.checkConsole();

		if (!CoreArenaPlugin.DEBUG_EDITING_MODE && !this.hasPerm(Permissions.Bypass.ARENA_COMMANDS) && CoreArenaPlugin.getDataFor(this.getPlayer()).hasArenaCache())
			this.returnTell(Lang.component("command-disallowed-while-playing"));

		final Menu menu = this.getMenu();

		if (menu != null)
			menu.displayTo(this.getPlayer());
	}

	protected abstract Menu getMenu();
}