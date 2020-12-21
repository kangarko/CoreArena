package org.mineacademy.game.physics;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.TORCH;
import static org.mineacademy.game.physics.BlockPhysics.applyGravitation;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.util.FallingLimitter;

public class BlockPhysicsCheckTask extends BukkitRunnable {

	private static volatile StrictList<BukkitRunnable> activeTasks = new StrictList<>();

	public static final void cancelRunning() {
		for (final BukkitRunnable r : activeTasks)
			try {
				r.cancel();
			} catch (final IllegalStateException ex) {
			}
	}

	private final Arena arena;

	/**
	 * Target block.
	 */
	private final Block block;

	/**
	 * Recursion count.
	 */
	private final int recursionCount;

	/**
	 * Will the the adjacent blocks be checked no matter if the center falls or not?
	 */
	private final boolean forced;

	protected BlockPhysicsCheckTask(Arena arena, Block block, int recursionCount, boolean forced) {
		this.arena = arena;
		this.block = block;
		this.recursionCount = recursionCount;
		this.forced = forced;

		activeTasks.add(this);
	}

	@Override
	public void run() {
		boolean fall = false;

		final Block under = block.getRelative(BlockFace.DOWN);

		if ((under.getType() == AIR || under.isLiquid() || under.getType() == TORCH) && BlockPhysics.canExplode(block.getType())) {
			fallBlock(block);
			fall = true;
		}

		if ((fall || forced) && recursionCount >= 0) {
			final GravitationHelper force = new GravitationHelper();

			if (forced) {
				final int recursion = 6;

				for (int i = 0; i < recursion; i++)
					force.applyUp(i);

				for (int i = 0; i < recursion; i++)
					force.applyDown(i);
			}

			final BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
			int wait = 1;

			for (final BlockFace face : faces)
				force.applySingle(face, wait++);
		}

		activeTasks.remove(this);
	}

	private final class GravitationHelper {

		private void applySingle(BlockFace face, int wait) {
			apply(face, 1, wait);
		}

		private void applyUp(int distance) {
			apply(BlockFace.UP, distance, distance);
		}

		private void applyDown(int distance) {
			apply(BlockFace.DOWN, distance, distance);
		}

		private void apply(BlockFace face, int distance, int wait) {
			final Block neighbor = block.getRelative(face, distance);

			applyGravitation(arena, neighbor, recursionCount - 1, false, wait);
		}
	}

	private final void fallBlock(Block block) {
		final Material type = block.getType();

		if (!BlockPhysics.canExplode(type))
			return;

		final Arena otherArena = CoreArenaPlugin.getArenaManager().findArena(block.getLocation());

		if (otherArena == null || !arena.getName().equals(otherArena.getName()))
			return;

		if (type == Material.GRASS || type == CompMaterial.MYCELIUM.getMaterial())
			block.setType(Material.DIRT);

		final FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5D, 0.0D, 0.5D), block.getType(), block.getData());

		falling.setDropItem(false);

		// Remove original block
		block.setType(Material.AIR);

		// Break torches
		{
			Block current = block;
			Block below = block.getRelative(BlockFace.DOWN);

			// Check downwards and break any obstructing blocks
			for (int y = current.getY(); y > 0; y--) {

				// Only breaks if the block below is solid and the block on top is transparent
				if (below.getType().isSolid()) {
					if (BlockUtil.isBreakingFallingBlock(current.getType()))
						current.breakNaturally();

					//Will land on the block below
					break;
				}
				current = below;
				below = current.getRelative(BlockFace.DOWN);
			}
		}

		FallingLimitter.add(arena, falling);
	}
}