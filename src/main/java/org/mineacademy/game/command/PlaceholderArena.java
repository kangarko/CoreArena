package org.mineacademy.game.command;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.command.placeholder.PositionPlaceholder;
import org.mineacademy.game.model.Arena;

final class PlaceholderArena extends PositionPlaceholder {

	public PlaceholderArena(int position) {
		super("arena", position);
	}

	@Override
	public String replace(String raw) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(raw);

		return arena != null ? arena.getName() : raw;
	}
}