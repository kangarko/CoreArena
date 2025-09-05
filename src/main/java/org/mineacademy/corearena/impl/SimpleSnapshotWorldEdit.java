package org.mineacademy.corearena.impl;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural;
import org.mineacademy.corearena.model.ArenaSnapshotStage;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.ChunkedTask;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.platform.Platform;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;

/**
 * Represents a simple way of loading/saving WorldEdit schematics files
 * used for damaged snapshots.
 *
 * CoreArena takes two arena schematics files:
 *
 * 1. Initial
 * 2. Damaged
 *
 * Damage snapshot is then used when player tries to damage the
 * arena, whereas the initial one is used when arena is stopping to
 * restore it.
 */
public final class SimpleSnapshotWorldEdit extends ArenaSnapshotProcedural {

	/**
	 * The two stages for arena - initial and damaged
	 */
	private final Clipboard[] stages = new Clipboard[2];

	/**
	 * The file cache to optimize performance and invoke less calls
	 */
	private final Map<ArenaSnapshotStage, File> cache = new HashMap<>();

	/**
	 * Create a new instance for the given arena
	 *
	 * @param arena
	 */
	public SimpleSnapshotWorldEdit(Arena arena) {
		super(arena);

		// Load automatically when created
		this.loadAll();
	}

	/*
	 * Load the two snapshots
	 */
	private void loadAll() {
		this.stages[0] = this.load(DamagedStage.INITIAL);
		this.stages[1] = this.load(DamagedStage.DAMAGED);
	}

	/*
	 * Load a snapshot in the given state, returning clipboard object
	 */
	private Clipboard load(DamagedStage stage) {
		final File file = this.getSchematicFile(stage);

		// Ignore if not yet taken
		if (!file.exists())
			return null;

		// Warn for broken files
		if (file.length() == 0) {
			Common.warning(stage + " schematic file for arena " + this.getArena().getName() + " had 0 size and was removed!");

			file.delete();
			return null;
		}

		// Load from file and return
		try {
			final ClipboardFormat format = ClipboardFormats.findByFile(file);
			Valid.checkNotNull(format, "Unknown " + stage + " schematic file for arena " + this.getArena().getName() + "!");

			final ClipboardReader reader = format.getReader(new FileInputStream(file));
			final Clipboard copy = reader.read();

			return copy;

		} catch (final Throwable t) {

			// Do not crash if broken, just rename
			if (t instanceof EOFException)
				if (t.getMessage() != null && t.getMessage().startsWith("Unexpected end")) {
					Common.log("&cSchematics in '" + file.getPath() + "' is broken, ignoring and renaming file..");

					file.renameTo(new File(file.getPath() + ".broken_" + TimeUtil.getCurrentTimeSeconds()));
					return null;
				}

			throw new FoException(t, "Failed to load " + stage + " template for " + this.getArena().getName() + ", see below and report (make sure you have the latest WorldEdit)");
		}
	}

	@Override
	public void onTake(ArenaSnapshotStage stage) {
		final org.mineacademy.corearena.model.ArenaRegion region = this.getArena().getData().getRegion();

		// Ignore if region not set
		if (region.getPrimary() == null || region.getSecondary() == null)
			return;

		final File schematicFile = this.getSchematicFile(stage);

		if (!schematicFile.exists()) {
			schematicFile.getParentFile().mkdirs();

			try {
				schematicFile.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		final EditSession session = this.createEditSession();

		try (Closer closer = Closer.create()) {
			final Region weRegion = this.makeRegion();
			final Clipboard clipboard = new BlockArrayClipboard(weRegion);
			final ForwardExtentCopy copy = new ForwardExtentCopy(session, weRegion, clipboard, weRegion.getMinimumPoint());

			Operations.completeLegacy(copy);

			final FileOutputStream fos = new FileOutputStream(schematicFile);
			final FileOutputStream out = closer.register(fos);
			final ClipboardWriter writer = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(out));

			writer.write(clipboard);

			if (Settings.WorldEdit.CLOSE_STREAM && !HookManager.isFAWELoaded()) {
				fos.close();
				out.close();
				writer.close();
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// Reload
		this.loadAll();
	}

	@Override
	public void onRestore(ArenaSnapshotStage newStage) {
		if (!this.isSaved(newStage))
			return;

		// Run as much async as possible to put pressure off of the main thread
		Platform.runTaskAsync(() -> {

			final Clipboard clip = this.getClipboard(newStage);

			if (clip == null)
				return;

			final EditSession session = this.createEditSession();
			session.setFastMode(true);

			// Count all arena blocks
			final List<BlockVector3> vectors = new ArrayList<>();
			final List<Block> blocks = this.getArena().getData().getRegion().getBlocks();

			for (final Block block : blocks)
				vectors.add(this.makeVector(block.getLocation()));

			// Restore each block individually, in chunks
			final ChunkedTask task = new ChunkedTask(Settings.WorldEdit.BLOCK_BULK_RESTORE_AMOUNT) {

				@Override
				protected void onProcess(int index) {
					final BlockVector3 vector = vectors.get(index);

					SimpleSnapshotWorldEdit.this.update(session, newStage, vector);
				}

				@Override
				protected boolean canContinue(int index) {
					return index < vectors.size();
				}

				@Override
				protected void onFinish(boolean gracefully) {
					session.flushSession();

					// FAWE bug: Fix lightning
					// https://github.com/IntellectualSites/FastAsyncWorldEdit/issues/605#issuecomment-708596438
					if (HookManager.isFAWELoaded())
						FaweAPI.fixLighting(SimpleSnapshotWorldEdit.this.makeWorld(), SimpleSnapshotWorldEdit.this.makeRegion(), null, RelightMode.ALL);
				}
			};

			task.setWaitPeriodTicks(Settings.WorldEdit.WAIT_PERIOD.getTimeTicks());

			// Run the chain on the main thread for safety
			Platform.runTask(() -> task.startChain());
		});
	}

	/**
	 * @see org.mineacademy.corearena.model.ArenaSnapshotProcedural#restoreBlock(org.bukkit.block.Block, org.mineacademy.corearena.model.ArenaSnapshotStage)
	 */
	@Override
	public Block restoreBlock(Block block, ArenaSnapshotStage stage) {
		if (this.isSaved(stage) && this.getClipboard(stage) != null) {
			final EditSession session = this.createEditSession();
			final BlockType changed = this.update(session, stage, this.makeVector(block.getLocation()));

			block = changed != null && changed.getName().equals("Air") ? null : block;
			session.flushSession();
		}

		return block;
	}

	/**
	 * @see org.mineacademy.corearena.model.ArenaSnapshot#isSaved(org.mineacademy.corearena.model.ArenaSnapshotStage)
	 */
	@Override
	public boolean isSaved(ArenaSnapshotStage stage) {
		return this.getSchematicFile(stage).exists();
	}

	/*
	 * Sets the given vector to its state from the given stage
	 */
	private BlockType update(EditSession session, ArenaSnapshotStage stage, BlockVector3 vec) {
		final BaseBlock copy = this.getBaseBlock(stage, vec);

		if (copy != null)
			try {
				session.setBlock(BlockVector3.at(vec.getX(), vec.getY(), vec.getZ()), copy);

				return copy.getBlockType();

			} catch (final MaxChangedBlocksException ex) {
				ex.printStackTrace();
			}

		return null;
	}

	/*
	 * Return the schematic file, if exists
	 */
	private File getSchematicFile(ArenaSnapshotStage stage) {
		if (!this.cache.containsKey(stage)) {
			final String folder = (HookManager.isFAWELoaded() ? "FastAsyncWorldEdit/schematics/" : "WorldEdit/schematics/")
					+ stage.getFileName().replace("{arena}", this.getArena().getName()) + (HookManager.isFAWELoaded() ? ".schem" : ".schematic");

			this.cache.put(stage, new File(CoreArenaPlugin.getInstance().getDataFolder().getParent(), folder));
		}

		return this.cache.get(stage);
	}

	/*
	 * Create edit session from the arena world automatically
	 */
	private EditSession createEditSession() {
		return WorldEdit.getInstance().getEditSessionFactory().getEditSession(this.makeWorld(), -1);
	}

	/*
	 * Create world from our current arena region
	 */
	private com.sk89q.worldedit.world.World makeWorld() {
		return new BukkitWorld(this.getArena().getData().getRegion().getPrimary().getWorld());
	}

	/*
	 * Converts the given location into WorldEdit vector
	 */
	private BlockVector3 makeVector(Location location) {
		Valid.checkNotNull(location, "Location in WorldEdit snapshot cannot be null!");

		return BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/*
	 * Create a WorldEdit region from ours
	 */
	private Region makeRegion() {
		final BlockVector3 min = this.makeVector(this.getArena().getData().getRegion().getPrimary());
		final BlockVector3 max = this.makeVector(this.getArena().getData().getRegion().getSecondary());

		return new CuboidRegion(min, max);
	}

	/*
	 * Return WorldEdit block from the given vector
	 */
	private BaseBlock getBaseBlock(ArenaSnapshotStage stage, BlockVector3 vector) {
		final Clipboard clip = this.getClipboard(stage);

		Valid.checkNotNull(clip, "Selected stage (" + stage + ") is not loaded");
		return clip.getFullBlock(BlockVector3.at(vector.getX(), vector.getY(), vector.getZ()));
	}

	/*
	 * Return the clipboard in a given stage
	 */
	private Clipboard getClipboard(ArenaSnapshotStage stage) {
		return this.stages[stage.getId()];
	}
}
