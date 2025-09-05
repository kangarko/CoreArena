package org.mineacademy.corearena.type;

import lombok.RequiredArgsConstructor;

/**
 * Represents a type of reward, typically a selection of items using menu
 */
@RequiredArgsConstructor
public enum RewardType {

	/**
	 * The standard items as rewards
	 */
	ITEM("Items"),

	/**
	 * Rewards that can be categorized as blocks
	 */
	BLOCK("Blocks"),

	/**
	 * Rewards that can be defined as "packs", meaning they offer collectibles, etc.
	 */
	PACK("Packs"),

	/**
	 * Rewards that upgrade class tiers
	 */
	TIERS("Tiers");

	/**
	 * The human readable key identifier
	 */
	private final String key;

	@Override
	public String toString() {
		return this.key;
	}
}
