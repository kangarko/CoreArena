package org.mineacademy.corearena.countdown;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.settings.Lang;

public final class LaunchCountdown extends MomentedCountdown {

	public LaunchCountdown(Arena arena) {
		super(arena, arena.getSettings().getLobbyDurationSeconds());
	}

	@Override
	protected void onInMomentTick() {
		this.broadcastLeft(Lang.component("arena-lobby-start-countdown"));

		this.playSoundTimeLeft();
	}

	@Override
	protected void onOutMomentTick() {
	}

	@Override
	protected void onEnd() {
		this.getArena().startArena(false);
	}
}