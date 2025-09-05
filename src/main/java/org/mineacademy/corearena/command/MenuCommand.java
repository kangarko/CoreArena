package org.mineacademy.corearena.command;

import java.util.ArrayList;
import java.util.List;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.ForwardingPlaceholder;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.menu.CoreMenu;
import org.mineacademy.corearena.menu.IndividualArenaMenu;
import org.mineacademy.corearena.menu.IndividualClassMenu;
import org.mineacademy.corearena.menu.IndividualUpgradeMenu;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.settings.Lang;

final class MenuCommand extends AbstractCoreSubcommand {

	public MenuCommand() {
		super("menu|m");

		this.setValidArguments(0, 1);
		this.setDescription("The main menu of this plugin.");
		this.setUsage("[object]");

		this.addPlaceholder(new ForwardingPlaceholder("object", 0));
	}

	@Override
	protected void onCommand() {
		if (!this.canAccessMenu())
			return;

		if (this.args.length == 0) {
			new CoreMenu().displayTo(this.getPlayer());

			return;
		}

		final String object = this.args[0];

		// Check arena
		final Arena arena = this.getArena(object);

		if (arena != null) {
			new IndividualArenaMenu(object).displayTo(this.getPlayer());

			return;
		}

		// Check class
		final ArenaClass arenaClass = this.getClass(object);

		if (arenaClass != null) {
			new IndividualClassMenu(arenaClass, true).displayTo(this.getPlayer());

			return;
		}

		// Check upgrade
		final Upgrade arenaUpgrade = this.getUpgrade(object);

		if (arenaUpgrade != null) {
			new IndividualUpgradeMenu(arenaUpgrade, true).displayTo(this.getPlayer());

			return;
		}

		this.tell(Lang.component("command-menu-lookup-failed", "object", object));
	}

	private boolean canAccessMenu() throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(this.getSender());

		if (data.hasArenaCache())
			this.returnTell(Lang.component("menu-cannot-open-in-arena"));

		if (this.isPlayer()) {
			final Arena editedArena = this.getSetup().getEditedArena(this.getPlayer());

			if (editedArena != null)
				this.returnTell(Lang.component("arena-cannot-do-while-editing", "arena", editedArena.getName()));
		}

		return true;
	}

	@Override
	public List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (this.args.length == 1) {
			final List<String> all = new ArrayList<>();

			all.addAll(this.getArenas().getArenasNames());
			all.addAll(this.getClasses().getClassNames());
			all.addAll(this.getUpgrades().getAvailable());

			for (final String key : all)
				if (key.toLowerCase().startsWith(this.args[0].toLowerCase()))
					tab.add(key);
		}

		return tab;
	}
}