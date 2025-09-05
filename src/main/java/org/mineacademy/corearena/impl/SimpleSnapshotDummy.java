package org.mineacademy.corearena.impl;

import org.bukkit.block.Block;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural;
import org.mineacademy.corearena.model.ArenaSnapshotStage;

public class SimpleSnapshotDummy extends ArenaSnapshotProcedural {

	public SimpleSnapshotDummy(Arena arena) {
		super(arena);
	}

	@Override
	public final void onTake(ArenaSnapshotStage stage) {
	}

	@Override
	public final boolean isSaved(ArenaSnapshotStage stage) {
		return false;
	}

	@Override
	public Block restoreBlock(Block block, ArenaSnapshotStage stage) {
		return block;
	}

	@Override
	public final void onRestore(ArenaSnapshotStage stage) {
	}
}
