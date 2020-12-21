package org.mineacademy.game.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;

public class EditCommand extends AbstractCoreSubcommand {

	public EditCommand() {
		super("edit|e");

		setDescription("Begin or stop editing an arena.");
		setUsage("[arena]");

		addPlaceholder(new PlaceholderArena(0));
	}

	@Override
	protected final void onCommand() {
		final Player player = getPlayer();
		final String arenaName = args.length == 1 ? args[0] : "";

		Arena arena;

		if (arenaName.isEmpty()) {
			arena = getArenas().findArena(player.getLocation());

			if (arena == null)
				arena = getSetup().getEditedArena(player);

			checkNotNull(arena, Localization.Arena.Error.NO_ARENA_AT_LOCATION);

		} else {
			arena = getArena(arenaName);
			checkNotNull(arena, Localization.Arena.Error.NOT_FOUND);
		}

		setArg(0, arena.getName());

		if (!canEditArena(arena))
			return;

		if (getSetup().isArenaEdited(arena)) {
			getSetup().removeEditedArena(arena);

			tell(Localization.Commands.Edit.DISABLED);

		} else {
			getSetup().addEditedArena(player, arena);

			tell(Localization.Commands.Edit.ENABLED);
		}
	}

	@Override
	public final List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (args.length == 1)
			if (sender instanceof Player && getSetup().getEditedArena((Player) sender) != null)
				tab.add(getSetup().getEditedArena((Player) sender).getName());
			else
				for (final String key : CoreArenaPlugin.getArenaManager().getAvailable())
					if (key.toLowerCase().startsWith(args[0].toLowerCase()))
						tab.add(key);

		return tab;
	}
}