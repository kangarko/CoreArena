package org.mineacademy.game.model;

import java.util.Set;

/**
 * A simple phase trigger that can be configured
 * from user config to trigger on each X phase
 *
 * Typically, an arena can for example refill chests with loots.
 * You can configure to refill them either every 3rd phase, or only on
 * the phase 10 and 20 (completely up to you).
 *
 * This class is responsible for triggering that chest refil, for example.
 */
public class ArenaTrigger {

	/**
	 * A list of phase numbers that will trigger, null if {@link #trigger} is set
	 */
	private final Set<Integer> triggers;

	/**
	 * If {@link #triggers} is null than this represents each X phase that should triggers
	 */
	private final int trigger;

	/**
	 * A phase counter
	 */
	private int count = 0;

	/**
	 * Create a new trigger that triggers only on specific phases
	 *
	 * @param triggers
	 */
	public ArenaTrigger(Set<Integer> triggers) {
		this(-1, triggers);
	}

	/**
	 * Create a new trigger that triggers every X phase
	 *
	 * @param trigger
	 */
	public ArenaTrigger(int trigger) {
		this(trigger, null);
	}

	/**
	 * Internal method, either one must be null, do not use
	 *
	 * @param trigger
	 * @param triggers
	 */
	private ArenaTrigger(int trigger, Set<Integer> triggers) {
		this.trigger = trigger;
		this.triggers = triggers;
	}

	/**
	 * Check the arena phase and returns true if we should trigger
	 *
	 * @param phase phase
	 * @return true if trigger
	 */
	public final boolean trigger(int phase) {
		if (triggers != null)
			return triggers.contains(phase);

		if (trigger != -1) {
			if (++count >= trigger) {
				count = 0;

				return true;
			}

			return false;
		}

		throw new RuntimeException("Dead end triggering " + phase + " phase");
	}
}
