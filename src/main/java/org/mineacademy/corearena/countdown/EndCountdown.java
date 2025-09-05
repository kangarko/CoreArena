package org.mineacademy.corearena.countdown;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.settings.Lang;

public final class EndCountdown extends MomentedCountdown {

	public EndCountdown(Arena arena) {
		super(arena, arena.getSettings().getArenaDurationSeconds());
	}

	@Override
	protected void onInMomentTick() {
		this.check();

		this.broadcastLeft(Lang.component("arena-game-end-warning"));
		this.playSoundTimeLeft();
	}

	@Override
	protected void onOutMomentTick() {
		this.check();
	}

	private void check() {
		Valid.checkBoolean(this.getArena().getState() == ArenaState.RUNNING, "Illegal state of " + this.getArena().getName() + " = " + this.getArena().getState());
	}

	@Override
	protected void onEnd() {
		this.getArena().stopArena(StopCause.NATURAL_COUNTDOWN);
	}
}
