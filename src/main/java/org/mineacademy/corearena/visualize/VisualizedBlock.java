package org.mineacademy.corearena.visualize;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompProperty;
import org.mineacademy.fo.remain.Remain;

/**
 *  @deprecated use classes in the new "visual" package
 */
@Deprecated
public abstract class VisualizedBlock {

	private final static boolean CAN_VISUALIZE = MinecraftVersion.atLeast(V.v1_9);

	/**
	 * The block this container originally holds
	 */
	private final BlockState state;

	/**
	 * The representation when selected
	 */
	private final CompMaterial mask;

	// Internal

	/** The visualized entity for this container */
	private FallingBlock fallingBlock = null;

	/** Is glowing? */
	private boolean glow = false;

	/** Keeping this block visualized */
	private Task keepAliveTask = null;

	/** The block under the falling block to prevent it from falling */
	private BlockState underground = null;

	public VisualizedBlock(final Block block, final CompMaterial mask) {
		this.state = block.getState();
		this.mask = mask;
	}

	public final void visualize(final VisualizeMode mode) {
		this.removeGlowIf();

		switch (mode) {
			case MASK:
				this.setMask();
				break;

			case GLOW:
				this.glow();
				break;

			default:
				throw new FoException("Unhandled visual mode: " + mode);
		}
	}

	public final void hide() {
		this.removeGlowIf();

		this.state.update(true, false);
	}

	public abstract String getBlockName(Block block);

	// ---------------------------------------------------------------------------
	// Mask
	// ---------------------------------------------------------------------------

	private final void setMask() {
		Remain.setTypeAndData(this.state.getBlock(), this.mask.getMaterial(), this.mask.getData());
	}

	// ---------------------------------------------------------------------------
	// Glow
	// ---------------------------------------------------------------------------

	private final void glow() {
		Valid.checkBoolean(!this.glow, "Block " + this.state.getBlock().getType() + " already glows!");

		this.changeRealBlock();

		if (CAN_VISUALIZE) {
			this.spawnFallingBlock();
			this.startKeepingAlive();
		}

		this.glow = true;
	}

	private final void changeRealBlock() {
		this.state.getBlock().setType(CAN_VISUALIZE ? Remain.getMaterial("BARRIER", CompMaterial.GLASS).getMaterial() : Material.BEACON);
	}

	private final void spawnFallingBlock() {
		Valid.checkBoolean(this.fallingBlock == null, "Report / Already visualized!");

		final Location spawnLoc = this.state.getLocation().clone().add(0.5, 0, 0.5);

		final Block under = spawnLoc.getBlock().getRelative(BlockFace.DOWN);
		if (under.getType() == Material.AIR) {
			this.underground = under.getState();
			under.setType(CompMaterial.BARRIER.getMaterial());
		}

		final FallingBlock falling = spawnLoc.getWorld().spawnFallingBlock(spawnLoc, this.mask.getMaterial(), this.mask.getData());

		this.paintFallingBlock(falling);
		this.fallingBlock = falling;
	}

	private final void paintFallingBlock(final FallingBlock falling) {
		try {
			Remain.setCustomName(falling, CompChatColor.translateColorCodes("&8[" + this.getBlockName(falling.getLocation().getBlock()) + "&r&8]"));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		falling.setVelocity(new Vector(0, 0, 0));
		falling.setDropItem(false);

		CompProperty.GLOWING.apply(falling, true);
		CompProperty.GRAVITY.apply(falling, false);
	}

	private final void startKeepingAlive() {
		Valid.checkBoolean(this.keepAliveTask == null, "Report / Task already running for " + this);

		this.keepAliveTask = new SimpleRunnable() {

			@Override
			public void run() {
				if (!VisualizedBlock.this.glow) {
					this.cancel();
					return;
				}

				Valid.checkNotNull(VisualizedBlock.this.fallingBlock, "Report / Falling block is null!");
				VisualizedBlock.this.fallingBlock.setTicksLived(1);
			}
		}.runTaskTimer(BukkitPlugin.getInstance(), 0, 580 /* Falling sand holds for 600 ticks, but let's be safe */);
	}

	// ---------------------------------------------------------------------------
	// Hide
	// ---------------------------------------------------------------------------

	private final void removeGlowIf() {
		if (this.glow) {

			if (CAN_VISUALIZE) {
				this.removeFallingBlock();
				this.stopKeepingAlive();
			}

			this.glow = false;
		}
	}

	private final void removeFallingBlock() {
		Valid.checkNotNull(this.fallingBlock, "Report / Visualized, but visualized block is null!");

		if (this.underground != null) {
			this.underground.update(true);
			this.underground = null;
		}

		this.fallingBlock.remove();
		this.fallingBlock = null;
	}

	private final void stopKeepingAlive() {
		Valid.checkNotNull(this.keepAliveTask, "Report / Task not running for " + this);

		this.keepAliveTask.cancel();
		this.keepAliveTask = null;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.state.getBlock().getType() + "}";
	}
}