package org.mineacademy.game.countdown;

import org.mineacademy.fo.Valid;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.StopCause;

public final class EndCountdown extends MomentedCountdown {

	public EndCountdown(Arena arena) {
		super(arena, arena.getSettings().getArenaDurationSeconds());
	}

	@Override
	protected void onInMomentTick() {
		check();

		broadcastLeft(Localization.Arena.Game.END_WARNING);
		playSoundTimeLeft();
	}

	@Override
	protected void onOutMomentTick() {
		check();
	}

	private void check() {
		Valid.checkBoolean(getArena().getState() == ArenaState.RUNNING, "Illegal state of " + getArena().getName() + " = " + getArena().getState());
	}

	@Override
	protected void onEnd() {
		getArena().stopArena(StopCause.NATURAL_COUNTDOWN);
	}
}
