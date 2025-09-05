package org.mineacademy.corearena.model;

/**
 * Represents a "wave" or a phase in an arena, divided by time.
 *
 * Typically, we spawn new round of monsters each time the arena enters a new phase
 *
 */
public interface ArenaPhase {

	/**
	 * Get the current phase
	 *
	 * @return the current phase
	 */
	int getCurrent();

	/**
	 * Starts the timer that will enter next phases automatically
	 */
	void startTimer();

	/**
	 * Enter next phase manually
	 */
	void onNextPhase();

	/**
	 * Called when the timer that enter next phases ticks but does not enter next phase yet.
	 *
	 * Example: Your timer is set to tick every second but a phase is 20 seconds long.
	 *          This method will get calld 19 times, and {@link #onNextPhase()} will get called 1 during the phase.
	 *
	 * This is useful if you want to update BossBar to decrease the time available.
	 *
	 * Typically, this is called automatically from your time
	 */
	void onTimerTick();

	/**
	 * Stops and resets the phase timer.
	 *
	 * Typically, this only resets boss bar. Monsters are not removed, this is handled within the arena on its stop.
	 */
	void stopAndReset();

	/**
	 * How many seconds we passed till the next phase?
	 *
	 * @return
	 */
	int getWaitedBetweenNextPhase();
}
