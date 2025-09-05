package org.mineacademy.corearena.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents an arena snapshot.
 *
 * This is typically the map (all blocks and mobs) of your arena.
 *
 * CoreArena and Puncher have two snapshots: initial and damaged,
 * dynamically retrieving blocks from the damaged snapshot to cause
 * procedural damage.
 *
 * Snapshots are typically stored as WorldEdit schematic files.
 */
@RequiredArgsConstructor
public abstract class ArenaSnapshot {

	/**
	 * Represents the arena associated with this snapshot.
	 */
	@Getter(value = AccessLevel.PROTECTED)
	private final Arena arena;

	/**
	 * Save the whole arena as a new {@link ArenaSnapshotStage}}.
	 *
	 * @param stage the stage the snapshot should be saved as
	 */
	public final void take(ArenaSnapshotStage stage) {
		this.onTake(stage);

		this.arena.onSnapshotUpdate(stage);
	}

	// Implemetable method for convenience of calling onSnapshotUpdate automatically above
	protected abstract void onTake(ArenaSnapshotStage stage);

	/**
	 * Sets the whole arena to a new {@link ArenaSnapshotStage}
	 *
	 * @param stage the state to restore the arena snapshot to
	 */
	public final void restore(ArenaSnapshotStage stage) {
		this.onRestore(stage);

		this.arena.onSnapshotUpdate(stage);
	}

	// Implemetable method for convenience of calling onSnapshotUpdate automatically above
	protected abstract void onRestore(ArenaSnapshotStage stage);

	/**
	 * Is a {@link ArenaSnapshotStage} saved already?
	 *
	 * @param stage the stage
	 * @return true if the snapshot is saved
	 */
	public abstract boolean isSaved(ArenaSnapshotStage stage);
}
