package org.mineacademy.game.model;

import org.bukkit.block.Block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a procedural snapshot, see {@link ArenaSnapshot}
 *
 */
public abstract class ArenaSnapshotProcedural extends ArenaSnapshot {

	/**
	 * Create a new empty procedural snapshot for an arena
	 *
	 * @param arena
	 */
	public ArenaSnapshotProcedural(Arena arena) {
		super(arena);
	}

	/**
	 * Sets the block to its new {@link ArenaSnapshotStage}
	 *
	 * @param block the block
	 * @param stage the stage from which we should retrieve the block
	 *
	 * @return The restored block, null if air
	 */
	public abstract Block restoreBlock(Block block, ArenaSnapshotStage stage);

	/**
	 * Represents the two states CoreArena/Puncher support
	 */
	@RequiredArgsConstructor
	@Getter
	public enum DamagedStage implements ArenaSnapshotStage {
		/**
		 * Launch virgin stage.
		 */
		INITIAL(0, "initial", "{arena}"),

		/**
		 * Final damaged stage.
		 */
		DAMAGED(1, "damaged", "{arena}_damaged");

		/**
		 * The internal id for the states
		 */
		private final int id;

		/**
		 * The immutable string identificator
		 */
		private final String formattedName;

		/**
		 * The file name template
		 */
		private final String fileName;
	}
}
