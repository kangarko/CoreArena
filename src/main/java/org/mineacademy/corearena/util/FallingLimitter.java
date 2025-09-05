package org.mineacademy.corearena.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.FallingBlock;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.model.SimpleRunnable;

import lombok.val;

/**
 * Removes flying blocks when they cross an arena's border.
 */
public final class FallingLimitter extends SimpleRunnable {

	// ----------------------------------------------------------------------------------------

	private static final Map<Arena, List<FallingBlock>> watched = new HashMap<>();

	public static void add(Arena arena, FallingBlock block) {
		final val list = watched.getOrDefault(arena, new ArrayList<>());

		list.add(block);
		watched.put(arena, list);
	}

	// ----------------------------------------------------------------------------------------

	@Override
	public void run() {
		final List<Arena> pendingRemove = new ArrayList<>();

		for (final Map.Entry<Arena, List<FallingBlock>> e : watched.entrySet()) {
			final Arena arena = e.getKey();

			for (final Iterator<FallingBlock> it = e.getValue().iterator(); it.hasNext();) {
				final FallingBlock block = it.next();

				if (!this.isWithin(block, arena)) {
					block.remove();

					it.remove();
				}
			}

			if (e.getValue().isEmpty())
				pendingRemove.add(arena);
		}

		for (final Iterator<Arena> it = watched.keySet().iterator(); it.hasNext();) {
			if (pendingRemove.contains(it.next()))
				it.remove();
		}
	}

	private boolean isWithin(FallingBlock block, Arena arena) {
		return arena.getData().getRegion().isWithin(block.getLocation());
	}

	public void onReload() {
		watched.clear();
	}

	public void onArenaStop(Arena arena) {
		if (watched.containsKey(arena)) {
			for (final FallingBlock block : watched.get(arena))
				block.remove();

			watched.remove(arena);
		}
	}
}
