package org.mineacademy.corearena.physics;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.TORCH;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.util.FallingLimitter;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.remain.CompMaterial;

/**
 * The physical engine offers two functions:
 *
 * A) It can accept the velocity of the arrow landing on a block and make it seem "explode",
 * 	  push it away.
 *
 * B) It can simulate gravity of sand/gravel, i.e. you are mining in a cave and when you mine iron ore,
 *    we make the 3-5 neighbor stones fall on you, adding realism and difficulty.
 */
public final class PhysicalEngine extends SimpleRunnable {

	/**
	 * Matches all DOUBLE or STEP block names
	 */
	private static final Pattern SLAB_PATTERN = Pattern.compile("(?!DOUBLE).*STEP");

	/**
	 * A list of active tasks where we make nearby adjacent blocks fall when the center block falls too,
	 * simulating gravity.
	 */
	private static final List<SimpleRunnable> activeTasks = new ArrayList<>();

	/**
	 * The arena to prevent blocks falling outside its border.
	 */
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

	private PhysicalEngine(Arena arena, Block block, int recursionCount, boolean forced) {
		this.arena = arena;
		this.block = block;
		this.recursionCount = recursionCount;
		this.forced = forced;

		activeTasks.add(this);
	}

	@Override
	public void run() {

		final Block under = this.block.getRelative(BlockFace.DOWN);
		boolean fall = false;

		if ((under.getType() == AIR || under.isLiquid() || under.getType() == TORCH) && canExplode(this.block.getType())) {
			this.fallBlock(this.block);

			fall = true;
		}

		if ((fall || this.forced) && this.recursionCount >= 0) {

			if (this.forced) {
				final int recursion = 6;

				for (int i = 0; i < recursion; i++)
					this.applyUp(i);

				for (int i = 0; i < recursion; i++)
					this.applyDown(i);
			}

			final BlockFace[] faces = { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
			int wait = 1;

			for (final BlockFace face : faces)
				this.applySingle(face, wait++);
		}

		activeTasks.remove(this);
	}

	private void applySingle(BlockFace face, int wait) {
		this.apply(face, 1, wait);
	}

	private void applyUp(int distance) {
		this.apply(BlockFace.UP, distance, distance);
	}

	private void applyDown(int distance) {
		this.apply(BlockFace.DOWN, distance, distance);
	}

	private void apply(BlockFace face, int distance, int wait) {
		final Block neighbor = this.block.getRelative(face, distance);

		applyGravitation(this.arena, neighbor, this.recursionCount - 1, false, wait);
	}

	private void fallBlock(Block block) {
		final Material type = block.getType();

		if (!canExplode(type))
			return;

		final Arena otherArena = CoreArenaPlugin.getArenaManager().findArena(block.getLocation());

		if (otherArena == null || !this.arena.getName().equals(otherArena.getName()))
			return;

		final String typeName = type.toString();

		if (typeName.equals("GRASS_BLOCK") || typeName.equals("GRASS") || type == CompMaterial.MYCELIUM.getMaterial())
			block.setType(Material.DIRT);

		final FallingBlock falling = block.getWorld().spawn(block.getLocation().add(0.5D, 0.0D, 0.5D), FallingBlock.class);
		falling.setBlockData(block.getBlockData());

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
					if (isBreakingFallingBlock(current.getType()))
						current.breakNaturally();

					// Will land on the block below
					break;
				}
				current = below;
				below = current.getRelative(BlockFace.DOWN);
			}
		}

		FallingLimitter.add(this.arena, falling);
	}

	/**
	 * Will a FallingBlock which lands on this Material break and drop to the
	 * ground?
	 *
	 * @param material to check
	 * @return boolean
	 */
	private static boolean isBreakingFallingBlock(final Material material) {
		return !material.isOccluding() &&
				material != CompMaterial.NETHER_PORTAL.getMaterial() &&
				material != CompMaterial.END_PORTAL.getMaterial() ||
				material == CompMaterial.COBWEB.getMaterial() ||
				material == Material.DAYLIGHT_DETECTOR ||
				CompMaterial.isTrapDoor(material) ||
				material == CompMaterial.OAK_SIGN.getMaterial() ||
				CompMaterial.isWallSign(material) ||
				// Match all slabs besides double slab
				SLAB_PATTERN.matcher(ReflectionUtil.getEnumName(material)).matches();
	}

	public static boolean canExplode(Material material) {
		if (material.toString().contains("STEP") || material.toString().contains("SLAB"))
			return true;

		return BlockUtil.isForBlockSelection(material);
	}

	public static void applyGravitation(Arena arena, Block block, int recursionCount, boolean forceCheck, int wait) {
		new PhysicalEngine(arena, block, recursionCount, forceCheck).runTaskLater(CoreArenaPlugin.getInstance(), wait);
	}

	public static void pushAway(Arena arena, BlockState block, Vector velocity) {
		final Material type = block.getType();

		if (type == Material.AIR || !canExplode(type))
			return;

		final FallingBlock falling = block.getWorld().spawn(block.getLocation(), FallingBlock.class);
		falling.setBlockData(block.getBlockData());

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
		new SimpleRunnable() {

			int ticksFalling = 0;

			@Override
			public void run() {
				if (this.ticksFalling++ > 20 * 10) {
					this.cancel();
					return;
				}

				if (block.isOnGround()) {
					final Block up = block.getLocation().getBlock().getRelative(BlockFace.UP);

					if (up.getType() == Material.AIR)
						up.setType(Material.FIRE);

					this.cancel();
				}
			}
		}.runTaskTimer(BukkitPlugin.getInstance(), 0, 1);
	}

	public static void cancelRunning() {
		for (final SimpleRunnable task : activeTasks)
			try {
				task.cancel();
			} catch (final IllegalStateException ex) {
			}
	}
}