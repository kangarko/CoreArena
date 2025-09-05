package org.mineacademy.corearena.visualize;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.platform.BukkitPlugin;

/**
 * Visualize a single block by either replacing its type with for example
 * Glowstone or invoking setGlowing method in later MC versions
 *
 * @deprecated use classes in the new "visual" package
 */
@Deprecated
public abstract class BlockVisualizer {

	/**
	 * A map of locations and their visualized blocks
	 */
	private final Map<Location, VisualizedBlock> stored = new HashMap<>();

	/**
	 * The mask that is shown when the block is visualized
	 */
	private final ToolVisualizer tool;

	public BlockVisualizer(final ToolVisualizer tool) {
		this.tool = tool;

		VisualizerListener.register(this);
	}

	/**
	 * Visualize the block.
	 * @param location
	 * @param mode
	 */
	public final void show(final Location location, final VisualizeMode mode) {
		Valid.checkNotNull(location, "Location == null");
		Valid.checkNotNull(location.getWorld(), "Location.World == null");

		this.tool.setCalledLocation(location);

		final VisualizedBlock v = new VisualizedBlock(location.getBlock(), this.tool.getMask()) {

			@Override
			public String getBlockName(final Block block) {
				BlockVisualizer.this.tool.setCalledLocation(block.getLocation());

				return BlockVisualizer.this.getBlockName(block);
			}
		};

		v.visualize(mode);
		this.stored.put(location, v);
	}

	/**
	 * Stop visualizing of the block.
	 *
	 * @param location the block
	 */
	public final void hide(final Location location) {
		if (!this.stored.containsKey(location))
			return;

		final VisualizedBlock v = this.stored.remove(location);

		// Workaround for shutdown of plugins:
		if (!BukkitPlugin.getInstance().isEnabled())
			v.hide();

		else
			Platform.runTask(() -> v.hide());
	}

	/**
	 * Get if the block that this tool holds, is stored (it has been put
	 * to the map by the show method?)
	 *
	 * @param block the block
	 * @return whether or not the block is visualized
	 */
	public final boolean isStored(final Block block) {
		Valid.checkNotNull(block, "Null block!");

		return this.stored.containsKey(block.getLocation());
	}

	/**
	 * Update all stored blocks to a new state.
	 *
	 * @param mode the new mode
	 */
	public final void updateStored(final VisualizeMode mode) {
		for (final VisualizedBlock v : this.stored.values())
			v.visualize(mode);
	}

	/**
	 * Get the blocks's name above it for the specified block.
	 *
	 * @param block the block
	 * @return the block name
	 */
	protected abstract String getBlockName(Block block);

	/**
	 * A method called when the block is removed
	 *
	 * @param player the player
	 * @param block the block
	 */
	protected abstract void onRemove(Player player, Block block);

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.tool.getMask() + "}";
	}
}
