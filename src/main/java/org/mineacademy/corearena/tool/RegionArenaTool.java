package org.mineacademy.corearena.tool;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.model.ArenaRegion;
import org.mineacademy.corearena.type.BlockClick;
import org.mineacademy.corearena.type.RegionPoint;
import org.mineacademy.corearena.visualize.RegionVisualized;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegionArenaTool extends SelectorTool {

	@Getter
	private static final RegionArenaTool instance = new RegionArenaTool();

	public static ItemStack getTool() {
		return instance.getItem();
	}

	private RegionVisualized regionVisualizer;

	/**
	 * Last points are stored here, because only one point is allowed and the arena-lobby does not allow tracking of individual points.
	 */
	private Location lastPrimaryPoint = new Location(null, 0, 0, 0);
	private Location lastSecondaryPoint = new Location(null, 0, 0, 0);

	@Override
	protected String getName() {
		return "Arena Region Point";
	}

	@Override
	protected List<String> getDescription() {
		return Arrays.asList(
				"&2&l<< " + this.getColor() + "Left click &7a block to set",
				"&7the primary location&7.",
				"",
				this.getColor() + "Right click &7a block to set",
				"&7the secondary location&7 &2&l>>");
	}

	@Override
	protected CompMaterial getMenuItem() {
		return CompMaterial.EMERALD;
	}

	@Override
	public CompMaterial getMask() {
		return CompMaterial.EMERALD_BLOCK;
	}

	@Override
	protected CompChatColor getColor() {
		return CompChatColor.GREEN;
	}

	@Override
	protected boolean requiresRegionInArena() {
		return false;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		final Location loc = block.getLocation();
		final org.mineacademy.corearena.type.RegionPoint point = org.mineacademy.corearena.type.RegionPoint.fromClick(org.mineacademy.corearena.type.BlockClick.valueOf(click.toString()));

		// Only allow one point of its kind to be set.
		{
			if (point == org.mineacademy.corearena.type.RegionPoint.PRIMARY) {
				if (this.lastPrimaryPoint != null)
					if (!this.lastPrimaryPoint.equals(loc))
						this.hide(this.lastPrimaryPoint);

				this.lastPrimaryPoint = loc;

			} else {
				if (this.lastSecondaryPoint != null)
					if (!this.lastSecondaryPoint.equals(loc))
						this.hide(this.lastSecondaryPoint);

				this.lastSecondaryPoint = loc;
			}
		}

		this.getArena().getData().setRegion(loc, point);

		final RegionVisualized visualized = this.craftVisualizerRegion();

		if (visualized != null)
			visualized.restart(30 * 20);
	}

	@Override
	protected void handleBlockBreak(Player player, Block block) {
		final org.mineacademy.corearena.type.RegionPoint point = this.lookupPoint(block);

		if (point != null)
			this.getArena().getData().removeRegion(point);

		final RegionVisualized visualized = this.craftVisualizerRegion();

		if (visualized != null)
			visualized.restart(30 * 20);
	}

	@Override
	protected String getBlockTitle(@NonNull Block block) {
		final RegionPoint rp = this.lookupPoint(block);

		return rp == null ? CompChatColor.WHITE + "Unrecognized region block" : this.getColor().toString() + rp + " " + this.getColoredName();
	}

	private org.mineacademy.corearena.type.RegionPoint lookupPoint(Block block) {
		final Location loc = block.getLocation();

		if (this.lastPrimaryPoint != null && loc.equals(this.lastPrimaryPoint))
			return org.mineacademy.corearena.type.RegionPoint.PRIMARY;

		if (this.lastSecondaryPoint != null && loc.equals(this.lastSecondaryPoint))
			return org.mineacademy.corearena.type.RegionPoint.SECONDARY;

		return null;
	}

	@Override
	public void onHotbarFocused(Player pl) {
		if (this.getArena() != null) {
			super.onHotbarFocused(pl);

			if (this.getArena() != null) {
				final RegionVisualized visualized = this.craftVisualizerRegion();

				if (visualized != null)
					visualized.show(30 * 20);
			}
		}
	}

	@Override
	public void onHotbarDefocused(Player pl) {
		if (this.getArena() != null) {
			super.onHotbarDefocused(pl);

			if (this.getArena() != null) {
				final RegionVisualized visualized = this.craftVisualizerRegion();

				if (visualized != null)
					visualized.stop();
			}
		}
	}

	@Override
	protected void renderExistingBlocks() {
		final ArenaRegion region = this.getArena().getData().getRegion();

		if (region != null) {
			if (region.getPrimary() != null)
				this.visualizeMask(region.getPrimary());

			if (region.getSecondary() != null)
				this.visualizeMask(region.getSecondary());
		}
	}

	@Override
	protected void unrenderExistingBlocks() {
		final ArenaRegion region = this.getArena().getData().getRegion();

		if (region != null) {
			if (region.getPrimary() != null)
				this.hide(region.getPrimary());

			if (region.getSecondary() != null)
				this.hide(region.getSecondary());
		}

		if (this.getArena() != null) {
			final RegionVisualized visualized = this.craftVisualizerRegion();

			if (visualized != null)
				visualized.stop();
		}
	}

	private RegionVisualized craftVisualizerRegion() {
		if (this.regionVisualizer != null)
			this.regionVisualizer.stop();

		final ArenaRegion region = this.getData().getRegion();

		if (region.getPrimary() == null || region.getSecondary() == null)
			return null;

		return this.getArena() != null ? (this.regionVisualizer = new RegionVisualized(null, region.getPrimary(), region.getSecondary())) : null;
	}
}