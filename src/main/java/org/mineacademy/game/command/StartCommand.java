package org.mineacademy.game.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.util.CoreUtil;

public class StartCommand extends AbstractCoreSubcommand {

	public StartCommand() {
		super("start");

		setDescription("Start a pending arena.");
		setUsage("[arena] [-f]");

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

		if (arena.getState() == ArenaState.STOPPED) {
			arena.startLobby();
			tell(Localization.Commands.Start.SUCCESS);

		} else if (arena.getState() == ArenaState.LOBBY)
			tell(arena.startArena(true) ? Localization.Commands.Start.SUCCESS : Localization.Commands.Start.FAIL);

		else
			tell(Localization.Arena.Error.ALREADY_RUNNING);
	}

	final static Arena findArenaWhenUnspecified(Player player) {
		Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena == null)
			arena = CoreArenaPlugin.getArenaManager().findArena(player.getLocation());

		return arena;
	}

	final static void returnTellAvailable(CommandSender sender) throws CommandException {
		CoreArenaPlugin.getArenaManager().tellAvailableArenas(sender);

		throw new CommandException();
	}

	@Override
	public List<String> tabComplete() {
		return CoreUtil.tabCompleteArenaNames(args);
	}
}