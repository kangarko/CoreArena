package org.mineacademy.game.type;

/**
 * Represents tier mode.
 *
 * Typically used when giving classes to
 * players, to determine whether the class is
 * given as preview in the lobby (without permissions, etc.)
 * or as full class ready for the game.
 */
public enum TierMode {

	/**
	 * The preview shown in the lobby
	 */
	PREVIEW,

	/**
	 * The full version of the class
	 */
	PLAY;
}
