package org.mineacademy.game.command;

import static org.mineacademy.game.command.StartCommand.findArenaWhenUnspecified;
import static org.mineacademy.game.command.StartCommand.returnTellAvailable;

import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.StopCause;
import org.mineacademy.game.util.CoreUtil;

public class StopCommand extends AbstractCoreSubcommand {

	public StopCommand() {
		super("stop");

		setDescription("Stop a running arena.");
		setUsage("[arena]");

		addPlaceholder(new PlaceholderArena(0));
	}

	@Override
	protected final void onCommand() {
		final String arenaName = args.length > 0 ? args[0] : "";
		Arena arena = null;

		if (arenaName.isEmpty()) {
			if (sender instanceof Player)
				arena = findArenaWhenUnspecified(getPlayer());

			if (arena == null) {
				tell(Localization.Arena.Error.NO_ARENA_AT_LOCATION);

				returnTellAvailable(sender);
			}

		} else
			arena = getArena(arenaName);

		if (arena == null) {
			tell(Localization.Arena.Error.NOT_FOUND);
			returnTellAvailable(sender);
		}

		setArg(0, arena.getName());

		checkBoolean(arena.getState() != ArenaState.STOPPED, Localization.Commands.Stop.ALREADY_STOPPED);
		arena.stopArena(StopCause.INTERRUPTED_COMMAND);
		tell(Localization.Commands.Stop.SUCCESS);
	}

	@Override
	public List<String> tabComplete() {
		return CoreUtil.tabCompleteArenaNames(args);
	}
}