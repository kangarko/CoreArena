package org.mineacademy.game.tool;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.ArenaRegion;
import org.mineacademy.game.type.BlockClick;
import org.mineacademy.game.type.RegionPoint;
import org.mineacademy.game.visualize.RegionVisualized;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegionSelector extends Selector {

	@Getter
	private static final RegionSelector instance = new RegionSelector();

	public static final ItemStack getTool() {
		return instance.getItem();
	}

	private RegionVisualized regionVisualizer;

	/**
	 * Last points are stored here, because only one point is allowed and the arena-lobby does not allow tracking of individual points.
	 */
	private Location lastPrimaryPoint = new Location(null, 0, 0, 0);
	private Location lastSecondaryPoint = new Location(null, 0, 0, 0);

	@Override
	protected final String getName() {
		return "Arena Region Point";
	}

	@Override
	protected final List<String> getDescription() {
		return Arrays.asList(
				getColor() + "Left click &7a block to set",
				"&7the primary location&7. ",
				"",
				getColor() + "Right click &7a block to set",
				"&7the secondary location&7. ");
	}

	@Override
	protected final CompMaterial getMenuItem() {
		return CompMaterial.EMERALD;
	}

	@Override
	public final CompMaterial getMask() {
		return CompMaterial.EMERALD_BLOCK;
	}

	@Override
	protected final ChatColor getColor() {
		return ChatColor.GREEN;
	}

	@Override
	protected boolean requiresRegionInArena() {
		return false;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		final Location loc = block.getLocation();
		final org.mineacademy.game.type.RegionPoint point = org.mineacademy.game.type.RegionPoint.fromClick(org.mineacademy.game.type.BlockClick.valueOf(click.toString()));

		// Only allow one point of its kind to be set.
		{
			if (point == org.mineacademy.game.type.RegionPoint.PRIMARY) {
				if (lastPrimaryPoint != null)
					if (!lastPrimaryPoint.equals(loc))
						hide(lastPrimaryPoint);

				lastPrimaryPoint = loc;

			} else {
				if (lastSecondaryPoint != null)
					if (!lastSecondaryPoint.equals(loc))
						hide(lastSecondaryPoint);

				lastSecondaryPoint = loc;
			}
		}

		getArena().getData().setRegion(loc, point);

		final RegionVisualized visualized = craftVisualizerRegion();

		if (visualized != null)
			visualized.restart(30 * 20);
	}

	@Override
	protected final void handleBlockBreak(Player player, Block block) {
		final org.mineacademy.game.type.RegionPoint point = lookupPoint(block);

		if (point != null)
			getArena().getData().removeRegion(point);

		final RegionVisualized visualized = craftVisualizerRegion();

		if (visualized != null)
			visualized.restart(30 * 20);
	}

	@Override
	protected String getBlockTitle(@NonNull Block block) {
		final RegionPoint rp = lookupPoint(block);

		return rp == null ? ChatColor.WHITE + "Unrecognized region block" : getColor().toString() + rp + " " + getColoredName();
	}

	private org.mineacademy.game.type.RegionPoint lookupPoint(Block block) {
		final Location loc = block.getLocation();

		if (lastPrimaryPoint != null && loc.equals(lastPrimaryPoint))
			return org.mineacademy.game.type.RegionPoint.PRIMARY;

		if (lastSecondaryPoint != null && loc.equals(lastSecondaryPoint))
			return org.mineacademy.game.type.RegionPoint.SECONDARY;

		return null;
	}

	@Override
	public final void onHotbarFocused(Player pl) {
		if (getArena() != null) {
			super.onHotbarFocused(pl);

			if (getArena() != null) {
				final RegionVisualized visualized = craftVisualizerRegion();

				if (visualized != null)
					visualized.show(30 * 20);
			}
		}
	}

	@Override
	public final void onHotbarDefocused(Player pl) {
		if (getArena() != null) {
			super.onHotbarDefocused(pl);

			if (getArena() != null) {
				final RegionVisualized visualized = craftVisualizerRegion();

				if (visualized != null)
					visualized.stop();
			}
		}
	}

	@Override
	protected void renderExistingBlocks() {
		final ArenaRegion region = getArena().getData().getRegion();

		if (region != null) {
			if (region.getPrimary() != null)
				visualizeMask(region.getPrimary());

			if (region.getSecondary() != null)
				visualizeMask(region.getSecondary());
		}
	}

	@Override
	protected void unrenderExistingBlocks() {
		final ArenaRegion region = getArena().getData().getRegion();

		if (region != null) {
			if (region.getPrimary() != null)
				hide(region.getPrimary());

			if (region.getSecondary() != null)
				hide(region.getSecondary());
		}

		if (getArena() != null) {
			final RegionVisualized visualized = craftVisualizerRegion();

			if (visualized != null)
				visualized.stop();
		}
	}

	private final RegionVisualized craftVisualizerRegion() {
		if (regionVisualizer != null)
			regionVisualizer.stop();

		final ArenaRegion region = getData().getRegion();

		if (region.getPrimary() == null || region.getSecondary() == null)
			return null;

		return getArena() != null ? (regionVisualizer = new RegionVisualized(null, region.getPrimary(), region.getSecondary())) : null;
	}
}