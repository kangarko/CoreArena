package org.mineacademy.game.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.game.command.placeholder.ForwardingPlaceholder;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.menu.CoreMenu;
import org.mineacademy.game.menu.IndividualArenaMenu;
import org.mineacademy.game.menu.IndividualClassMenu;
import org.mineacademy.game.menu.IndividualUpgradeMenu;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.settings.Localization;

public class MenuCommand extends AbstractCoreSubcommand {

	public MenuCommand() {
		super("menu|m");

		setDescription("The main menu of this plugin.");
		setUsage("[object]");

		addPlaceholder(new ForwardingPlaceholder("object", 0));
	}

	@Override
	protected final void onCommand() {
		if (!canAccessMenu())
			return;

		if (args.length == 0) {
			new CoreMenu().displayTo(getPlayer());

			return;
		}

		checkBoolean(args.length == 1, Localization.Parts.USAGE + "/" + getLabel() + " " + getSublabel() + " [object]");

		final String object = args[0];

		// Check arena
		final Arena arena = getArena(object);

		if (arena != null) {
			new IndividualArenaMenu(object).displayTo(getPlayer());
			return;
		}

		// Check class
		final ArenaClass cl = getClass(object);

		if (cl != null) {
			new IndividualClassMenu(cl, true).displayTo(getPlayer());
			return;
		}

		// Check upgrade
		final Upgrade u = getUpgrade(object);

		if (u != null) {
			new IndividualUpgradeMenu(u, true).displayTo(getPlayer());

			return;
		}

		tell(Localization.Commands.Menu.LOOKUP_FAILED.replace("{object}", object));
	}

	private final boolean canAccessMenu() throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(sender);

		if (data.hasArenaCache())
			returnTell(Localization.Menu.CANNOT_OPEN_IN_ARENA);

		if (sender instanceof Player) {
			final Arena editedArena = getSetup().getEditedArena(getPlayer());

			if (editedArena != null)
				returnTell(Localization.Arena.CANNOT_DO_WHILE_EDITING.replace("{arena}", editedArena.getName()));
		}

		return true;
	}

	@Override
	public final List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (args.length == 1) {
			final List<String> all = new ArrayList<>();

			all.addAll(getArenas().getAvailable());
			all.addAll(getClasses().getAvailable().getSource());
			all.addAll(getUpgrades().getAvailable().getSource());

			for (final String key : all)
				if (key.toLowerCase().startsWith(args[0].toLowerCase()))
					tab.add(key);
		}

		return tab;
	}
}