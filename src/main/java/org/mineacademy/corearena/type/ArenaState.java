package org.mineacademy.corearena.type;

import lombok.RequiredArgsConstructor;

/**
 * Represents in which state the arena is found.
 */
@RequiredArgsConstructor
public enum ArenaState {

	/**
	 * Represents a stopped state.
	 */
	STOPPED,

	/**
	 * Represents when the lobby has started and it is counting down.
	 * When this is done, the arena enters a {@link #RUNNING} state
	 */
	LOBBY,

	/**
	 * Represents a arena that is being played.
	 */
	RUNNING;
}
