package org.mineacademy.corearena.countdown;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.fo.Common;

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
		if (this.getTimeLeft() == 1)
			try {
				this.getArena().getPhase().onNextPhase();

			} catch (final Throwable t) {
				Common.error(t,
						"Arena " + this.getArena().getName() + " has failed entering phase: " + this.getArena().getPhase().getCurrent(),
						"Stopping arena for safety.",
						"Error: {error}");

				this.getArena().stopArena(StopCause.INTERRUPTED_ERROR);
			}
		else
			this.getArena().getPhase().onTimerTick();
	}

	@Override
	protected void onEnd() {
	}

	@Override
	public int getTimeLeft() {
		return this.durationSeconds - this.getSecondsSinceStart() % this.durationSeconds;
	}
}
