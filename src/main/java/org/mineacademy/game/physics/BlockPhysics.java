package org.mineacademy.game.physics;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.util.FallingLimitter;

public class BlockPhysics {

	public static boolean canExplode(Material material) {
		if (material.toString().contains("STEP") || material.toString().contains("SLAB"))
			return true;

		return BlockUtil.isForBlockSelection(material);
	}

	public static void pushAway(Arena arena, BlockState block, Vector velocity) {
		final Material type = block.getType();

		if (type == Material.AIR || !canExplode(type))
			return;

		final FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), block.getData().getItemType(), block.getData().getData());

		final double x = MathUtil.range(velocity.getX(), -2, 2) * 0.5D; //0.15D + (b.getY() - startingPoint.getY()) * 0.03D;
		final double y = Math.random(); //(block.getX() /*- startingPoint.getX()*/) * 0.1D;
		final double z = MathUtil.range(velocity.getZ(), -2, 2) * 0.5D;

		falling.setVelocity(new Vector(x, y, z));
		falling.setDropItem(false);

		if (RandomUtil.chance(12) && type.isBurnable())
			scheduleBurnOnFall(falling);

		FallingLimitter.add(arena, falling);

		block.setType(Material.AIR);
	}

	private static void scheduleBurnOnFall(FallingBlock block) {
		new BukkitRunnable() {

			int uplynulo = 0;

			@Override
			public void run() {
				if (uplynulo++ > 20 * 10) {
					cancel();
					return;
				}

				if (block.isOnGround()) {
					final Block up = block.getLocation().getBlock().getRelative(BlockFace.UP);

					if (up.getType() == Material.AIR)
						up.setType(Material.FIRE);

					cancel();
				}
			}
		}.runTaskTimer(SimplePlugin.getInstance(), 0, 1);
	}

	public static final void applyGravitation(Arena arena, Block block, int recursionCount, boolean forceCheck, int wait) {
		new BlockPhysicsCheckTask(arena, block, recursionCount, forceCheck).runTaskLater(CoreArenaPlugin.getInstance(), wait);
	}
}
