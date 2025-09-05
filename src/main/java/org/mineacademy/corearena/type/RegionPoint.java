package org.mineacademy.corearena.type;

import lombok.RequiredArgsConstructor;

/**
 * Represents a region point for a cuboid arena region
 */
@RequiredArgsConstructor
public enum RegionPoint {

	/**
	 * The first, primary region point
	 */
	PRIMARY("Primary"),

	/**
	 * The second, secondary region point
	 */
	SECONDARY("Secondary");

	/**
	 * The human readable representation
	 */
	private final String key;

	@Override
	public String toString() {
		return this.key;
	}

	/**
	 * Convert a right/left {@link BlockClick} to a primary/secondary region point automatically
	 * Left click = primary, right click = secondary
	 *
	 * @param click the click
	 * @return the region point
	 */
	public static RegionPoint fromClick(BlockClick click) {
		if (click == BlockClick.LEFT_CLICK)
			return PRIMARY;

		else if (click == BlockClick.RIGHT_CLICK)
			return RegionPoint.SECONDARY;

		throw new RuntimeException("Unhandled region point from click " + click);
	}
}