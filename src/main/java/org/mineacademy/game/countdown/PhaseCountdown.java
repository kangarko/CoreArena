package org.mineacademy.game.countdown;

import org.mineacademy.fo.Common;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.type.StopCause;

import lombok.Getter;

public final class PhaseCountdown extends ArenaCountdown {

	@Getter
	private final int durationSeconds;

	public PhaseCountdown(Arena arena, int durationSeconds) {
		super(arena, arena.getSettings().getArenaDurationSeconds());

		this.durationSeconds = durationSeconds;
	}

	@Override
	protected void onTick() {
		if (getTimeLeft() == 1)
			try {
				getArena().getPhase().onNextPhase();

			} catch (final Throwable t) {
				Common.error(t,
						"Arena " + getArena().getName() + " has failed entering phase: " + getArena().getPhase().getCurrent(),
						"Stopping arena for safety.",
						"%error");

				getArena().stopArena(StopCause.INTERRUPTED_ERROR);
			}
		else
			getArena().getPhase().onTimerTick();
	}

	@Override
	protected void onEnd() {
	}

	@Override
	public int getTimeLeft() {
		return durationSeconds - getSecondsSinceStart() % durationSeconds;
	}
}
