package org.mineacademy.corearena.countdown;

import java.util.Set;

import org.mineacademy.corearena.model.Arena;

import com.google.common.collect.Sets;

public abstract class MomentedCountdown extends ArenaCountdown {

	private final Set<Integer> moments = Sets.newHashSet(this.getCountdownSeconds() / 2, 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1);

	protected MomentedCountdown(Arena arena, int seconds) {
		super(arena, seconds);
	}

	@Override
	protected final void onTick() {
		if (this.moments.contains(this.getTimeLeft()))
			this.onInMomentTick();
		else
			this.onOutMomentTick();
	}

	protected abstract void onInMomentTick();

	protected abstract void onOutMomentTick();

}
