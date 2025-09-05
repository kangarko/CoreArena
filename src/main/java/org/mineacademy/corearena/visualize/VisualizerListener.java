package org.mineacademy.corearena.visualize;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.mineacademy.fo.BlockUtil;

/**
 *  @deprecated use classes in the new "visual" package
 */
@Deprecated
public final class VisualizerListener implements Listener {

	// -------------------------------------------------------------------------------
	// Static registration of different visualizers
	// -------------------------------------------------------------------------------

	private static final List<BlockVisualizer> registered = new ArrayList<>();

	public static void register(final BlockVisualizer v) {
		registered.add(v);
	}

	public static boolean isBlockTakenByOthers(final Block block, final BlockVisualizer whoAsks) {
		for (final BlockVisualizer other : registered)
			if (other != whoAsks && other.isStored(block))
				return true;

		return false;
	}

	// -------------------------------------------------------------------------------
	// Automatic listeners
	// -------------------------------------------------------------------------------

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent e) {
		final BlockVisualizer visualizer = this.findVisualizer(e.getBlock());

		if (visualizer != null) {
			visualizer.onRemove(e.getPlayer(), e.getBlock());

			e.setCancelled(true);
		}
	}

	// -------------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------------

	private BlockVisualizer findVisualizer(final Block block) {
		return block != null && this.canVisualize(block) ? this.findVisualizer0(block) : null;
	}

	private BlockVisualizer findVisualizer0(final Block block) {
		for (final BlockVisualizer visualizer : registered)
			if (visualizer.isStored(block))
				return visualizer;

		return null;
	}

	private boolean canVisualize(final Block block) {
		return BlockUtil.isForBlockSelection(block.getType());
	}
}
