package org.mineacademy.corearena.command.placeholder;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;

public final class ArenaPlaceholder extends PositionPlaceholder {

	public ArenaPlaceholder(int position) {
		super("arena", position);
	}

	@Override
	public String replace(String raw) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(raw);

		return arena != null ? arena.getName() : raw;
	}
}