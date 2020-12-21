package org.mineacademy.game.command;

import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.LeaveCause;

public class LeaveCommand extends AbstractCoreSubcommand {

	public LeaveCommand() {
		super("leave|l");

		setDescription("Leave arena while playing.");
	}

	@Override
	protected final void onCommand() {
		final Arena arena = getArenas().findArena(getPlayer());
		checkNotNull(arena, Localization.Commands.Leave.NOT_PLAYING);

		final boolean left = arena.kickPlayer(getPlayer(), LeaveCause.COMMAND);

		if (left)
			tell(Localization.Commands.Leave.SUCCESS.replace("{arena}", arena.getName()));
	}
}