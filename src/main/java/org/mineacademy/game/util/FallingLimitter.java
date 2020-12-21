package org.mineacademy.game.util;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.game.model.Arena;

import lombok.val;

/**
 * Removes flying blocks when they cross an arena's border.
 */
public final class FallingLimitter extends BukkitRunnable {

	// ----------------------------------------------------------------------------------------

	private static final StrictMap<Arena, StrictList<FallingBlock>> watched = new StrictMap<>();

	public static void add(Arena arena, FallingBlock block) {
		final val list = watched.getOrDefault(arena, new StrictList<>());

		list.add(block);
		watched.override(arena, list);
	}

	// ----------------------------------------------------------------------------------------

	@Override
	public void run() {
		final StrictList<Arena> pendingRemove = new StrictList<>();

		for (final Map.Entry<Arena, StrictList<FallingBlock>> e : watched.entrySet()) {
			final Arena arena = e.getKey();

			for (final Iterator<FallingBlock> it = e.getValue().iterator(); it.hasNext();) {
				final FallingBlock block = it.next();

				if (!isWithin(block, arena)) {
					block.remove();

					it.remove();
				}
			}

			if (e.getValue().isEmpty())
				pendingRemove.add(arena);
		}

		watched.removeAll(pendingRemove.getSource());
	}

	private boolean isWithin(FallingBlock block, Arena arena) {
		return arena.getData().getRegion().isWithin(block.getLocation());
	}

	public void onReload() {
		watched.clear();
	}

	public void onArenaStop(Arena arena) {
		if (watched.contains(arena)) {
			for (final FallingBlock block : watched.get(arena))
				block.remove();

			watched.remove(arena);
		}
	}
}
