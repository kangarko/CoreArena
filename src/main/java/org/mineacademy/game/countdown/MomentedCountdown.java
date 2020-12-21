package org.mineacademy.game.countdown;

import java.util.Set;

import org.mineacademy.game.model.Arena;

import com.google.common.collect.Sets;

public abstract class MomentedCountdown extends ArenaCountdown {

	private final Set<Integer> moments = Sets.newHashSet(getCountdownSeconds() / 2, 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1);

	protected MomentedCountdown(Arena arena, int seconds) {
		super(arena, seconds);
	}

	@Override
	protected final void onTick() {
		if (moments.contains(getTimeLeft()))
			onInMomentTick();
		else
			onOutMomentTick();
	}

	protected abstract void onInMomentTick();

	protected abstract void onOutMomentTick();

}
