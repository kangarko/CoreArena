package org.mineacademy.game.countdown;

import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;

public final class LaunchCountdown extends MomentedCountdown {

	public LaunchCountdown(Arena arena) {
		super(arena, arena.getSettings().getLobbyDurationSeconds());
	}

	@Override
	protected void onInMomentTick() {
		broadcastLeft(Localization.Arena.Lobby.START_COUNTDOWN);

		playSoundTimeLeft();
	}

	@Override
	protected void onOutMomentTick() {
	}

	@Override
	protected void onEnd() {
		getArena().startArena(false);
	}
}