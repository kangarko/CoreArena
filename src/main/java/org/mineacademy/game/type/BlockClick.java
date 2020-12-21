package org.mineacademy.game.type;

import java.util.Objects;

import org.bukkit.event.block.Action;

/**
 * Used when player has clicked on a block.
 */
public enum BlockClick {

	/**
	 * The right block click
	 */
	RIGHT_CLICK,

	/**
	 * The left block click
	 */
	LEFT_CLICK;

	/**
	 * Parses Bukkit action into our block click, failing if it is not a block-related click
	 *
	 * @param action
	 * @return
	 */
	public static BlockClick fromAction(Action action) {
		final BlockClick click = BlockClick.valueOf(action.toString().replace("_BLOCK", ""));
		Objects.requireNonNull(click, "Report / Unsupported click type from " + action);

		return click;
	}
}
