package org.mineacademy.corearena.exp;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.MathUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpFormula {

	private final String formula;

	public int calculate(final int phase) {
		return (int) MathUtil.calculate(this.formula.replace("{phase}", phase + ""));
	}

	public int calculate(final int phase, final int reward, final int players, final int maxPlayers) {
		return (int) MathUtil.ceiling(MathUtil.calculate(this.formula
				.replace("{reward}", reward + "")
				.replace("{phase}", phase + "")
				.replace("{players}", players + "")
				.replace("{maxPlayers}", maxPlayers + "")));
	}

	public int calculate(final int reward, final Arena arena) {
		return (int) MathUtil.ceiling(MathUtil.calculate(this.formula
				.replace("{reward}", reward + "")
				.replace("{phase}", arena.getPhase().getCurrent() + "")
				.replace("{players}", arena.getPlayers().size() + "")
				.replace("{maxPlayers}", arena.getSettings().getMaximumPlayers() + "")));
	}
}