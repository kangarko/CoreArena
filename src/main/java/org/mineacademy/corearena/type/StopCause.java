package org.mineacademy.corearena.type;

/**
 * The reason why an arena stopped.
 */
public enum StopCause {

	/**
	 * Natural, usually triggered by somebody or some that has won
	 */
	NATURAL_TRIGGER,

	/**
	 * Natural, the last phase is out
	 */
	NATURAL_LAST_PHASE,

	/**
	 * The time is up!
	 */
	NATURAL_COUNTDOWN,

	/**
	 * There are no more monsters alive.
	 */
	NATURAL_NO_MONSTERS,

	/**
	 * Stopped by a command or a server reload
	 */
	INTERRUPTED_COMMAND,

	/**
	 * Stopped due to an error
	 */
	INTERRUPTED_ERROR,

	/**
	 * Stopped due to a server or plugin reload
	 */
	INTERRUPTED_RELOAD,

	/**
	 * Stopped due to being empty
	 */
	INTERRUPTED_LAST_PLAYER_LEFT,

	/**
	 * Not enough players to start (below the required minimum)
	 */
	CANCELLED_NOT_ENOUGH_PLAYERS
}
