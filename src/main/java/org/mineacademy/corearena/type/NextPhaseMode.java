package org.mineacademy.corearena.type;

/**
 * When we should enter a new arena phase?
 */
public enum NextPhaseMode {

	/**
	 * Next phase turns when time for the current phase is up
	 */
	TIMER,

	/**
	 * Next phase turns when monsters are killed
	 */
	MONSTERS
}
