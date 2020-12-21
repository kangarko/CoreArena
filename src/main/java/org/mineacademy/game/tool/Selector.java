package org.mineacademy.game.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaRegion;
import org.mineacademy.game.util.CoreUtil;
import org.mineacademy.game.util.Permissions;
import org.mineacademy.game.visualize.ToolVisualizer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class Selector extends ToolVisualizer {

	@Getter(AccessLevel.PROTECTED)
	@NonNull
	@Deprecated // can be null
	private Arena arena;

	@Getter(AccessLevel.PROTECTED)
	@NonNull
	@Deprecated // can be null
	private Player player;

	@Deprecated // Localization
	protected final void tellNoArena(Player pl) {
		Common.tell(pl, "&cFirst select arena to edit via /" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " edit command.");
	}

	@Override
	protected final boolean canVisualize(Block block, Player player) {
		if (!CoreUtil.checkPerm(player, Permissions.Tools.TOOLS))
			return false;

		if (CoreArenaPlugin.getSetupManager().getEditedArena(player) == null) {
			tellNoArena(player);

			return false;
		}

		if (requiresRegionInArena() && getArena() != null) {
			final ArenaRegion region = getArena().getData().getRegion();

			if (region != null && region.getPrimary() != null && region.getSecondary() != null && !region.isWithin(block.getLocation())) {
				Common.tell(player, "&cThe selected point is outside arena's region.");

				return false;
			}
		}

		return true;
	}

	protected boolean requiresRegionInArena() {
		return true;
	}

	@Override
	protected void handleDataLoad(Player player, Block block) {
		this.player = player;
		this.arena = findArena(player, block);
	}

	private final Arena findArena(Player player, Block block) {
		Arena arena = null;

		if (player != null)
			arena = CoreArenaPlugin.getSetupManager().getEditedArena(player);

		else if (arena == null && block != null)
			arena = CoreArenaPlugin.getArenaManager().findArena(block.getLocation());

		return arena;
	}

	public final void onArenaEnterEditMode(Player pl, Arena arena) {
		this.arena = arena;

		renderExistingBlocks();
	}

	public final void onArenaLeaveEditMode(Arena arena) {
		this.arena = arena;

		unrenderExistingBlocks();
	}

	protected abstract void renderExistingBlocks();

	protected abstract void unrenderExistingBlocks();

	protected final ArenaData getData() {
		return getArena().getData();
	}
}
