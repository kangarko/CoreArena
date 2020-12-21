package org.mineacademy.game.command;

import org.bukkit.entity.Player;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;

public class ToggleCommand extends AbstractCoreSubcommand {

	public ToggleCommand() {
		super("toggle|tg");

		setDescription("Enable or disable an arena");
		setUsage("[arena]");
	}

	@Override
	protected void onCommand() {
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

		setArg(0, getLabel());

		if (!canEditArena(arena))
			return;

		arena.setEnabled(!arena.isEnabled());

		tell((arena.isEnabled() ? Localization.Commands.Change_State.ENABLED : Localization.Commands.Change_State.DISABLED)
				.find("{name}")
				.replace(arena.getName())
				.getReplacedMessageJoined());
	}
}
