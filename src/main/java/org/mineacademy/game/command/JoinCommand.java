package org.mineacademy.game.command;

import java.util.List;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.JoinCause;
import org.mineacademy.game.util.CoreUtil;

public class JoinCommand extends AbstractCoreSubcommand {

	public JoinCommand() {
		super("join|j");

		setDescription("Join an existing arena.");
		setUsage("[arena]");

		addPlaceholder(new PlaceholderArena(0));
	}

	@Override
	protected final void onCommand() {
		final String arenaName = args.length > 0 ? args[0] : null;
		Arena arena = arenaName != null ? getArena(arenaName) : getArenas().findArena(getPlayer().getLocation());

		if (arena == null && getArenas().getArenas().size() == 1)
			arena = getArenas().getArenas().iterator().next();

		if (arena == null) {

			if (args.length > 0)
				tell(Localization.Arena.Error.NOT_FOUND);

			CoreArenaPlugin.getArenaManager().tellAvailableArenas(sender);
			return;
		}

		arena.joinPlayer(getPlayer(), JoinCause.COMMAND);
	}

	@Override
	public List<String> tabComplete() {
		return CoreUtil.tabCompleteArenaNames(args);
	}
}