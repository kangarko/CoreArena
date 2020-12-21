package org.mineacademy.game.type;

import lombok.RequiredArgsConstructor;

/**
 * Represents an arena spawner type
 */
@RequiredArgsConstructor
public enum SpawnPointType {

	/**
	 * The monster spawn point
	 */
	MONSTER("Monster"),

	/**
	 * The player spawn point
	 */
	PLAYER("Player"),

	;

	/**
	 * The human readable format
	 */
	private final String key;

	@Override
	public String toString() {
		return key;
	}
}