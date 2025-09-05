package org.mineacademy.corearena.visualize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.menu.MenuMonsterSpawn;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.tool.SpawnpointMonsterTool;
import org.mineacademy.corearena.type.BlockClick;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *  @deprecated use classes in the new "visual" package
 */
@Deprecated
public abstract class ToolVisualizer extends Tool {

	@Getter
	private final BlockVisualizer visualizer;

	@Deprecated // unsafe
	@Getter
	@Setter
	private Location calledLocation;

	@Getter
	@Setter
	private VisualizeMode defaultMode = VisualizeMode.GLOW;

	protected ToolVisualizer() {
		this.visualizer = new BlockVisualizer(this) {

			@Override
			public String getBlockName(final Block block) {
				return ToolVisualizer.this.getBlockTitle(block);
			}

			@Override
			public void onRemove(final Player player, final Block block) {
				ToolVisualizer.this.onRemove(player, block);
			}
		};
	}

	private boolean canSetup(final Block clicked, final Action action) {
		return (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) && BlockUtil.isForBlockSelection(clicked.getType());
	}

	// --------------------------------------------------------------------------------
	// Standard methods
	// --------------------------------------------------------------------------------

	@Override
	public final void onBlockClick(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!event.hasBlock() || event.getAction().toString().contains("AIR")) {
			this.handleAirClick(player, event.getItem(), event.getAction() == Action.LEFT_CLICK_AIR ? ClickType.LEFT : ClickType.RIGHT);
			return;
		}

		if (event.isCancelled())
			return;

		if (!this.canSetup(event.getClickedBlock(), event.getAction()) && event.getClickedBlock().getType() != CompMaterial.BARRIER.getMaterial()) {
			Common.tell(player, "&c" + ChatUtil.capitalizeFully(event.getClickedBlock().getType()) + " is not allowed. Select a solid block.");

			return;
		}

		final Block block = event.getClickedBlock();

		if (!this.canVisualize(block, player) || VisualizerListener.isBlockTakenByOthers(block, this.visualizer))
			return;

		this.handleDataLoad(player, block);

		if (!this.visualizer.isStored(block)) {
			this.handleBlockSelect(player, block, BlockClick.fromAction(event.getAction()));

			this.visualizer.show(block.getLocation(), this.getDefaultMode());
			Common.tell(player, this.makeActionMessage("&2set"));

		} else {

			if (this instanceof SpawnpointMonsterTool && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				final ArenaPlayer cache = CoreArenaPlugin.getDataFor(player);

				if (cache.hasSetupCache()) {
					final Arena arena = cache.getSetupCache().arena;
					final SpawnPoint point = arena.getData().findSpawnPoint(block.getLocation());

					if (point instanceof SimpleSpawnPointMonster)
						new MenuMonsterSpawn((SimpleSpawnPointMonster) point, arena).displayTo(player);
				}

				return;
			}

			this.onRemove(player, block);
		}
	}

	public final void onRemove(final Player player, final Block block) {
		this.handleDataLoad(player, block);

		this.visualizer.hide(block.getLocation());
		this.handleBlockBreak(player, block);

		Common.tell(player, this.makeRemoveActionMessage());
	}

	// Workaround for concurrency issues
	private final List<Player> cache = new ArrayList<>();

	@Override
	public void onHotbarFocused(@NonNull final Player pl) {
		if (this.cache.contains(pl))
			return;

		this.handleDataLoad(pl, null);

		this.cache.add(pl);
		this.visualizer.updateStored(VisualizeMode.GLOW);
		this.cache.remove(pl);
	}

	@Override
	public void onHotbarDefocused(@NonNull final Player pl) {
		if (this.cache.contains(pl))
			return;

		this.handleDataLoad(pl, null);

		this.cache.add(pl);
		this.visualizer.updateStored(VisualizeMode.MASK);
		this.cache.remove(pl);
	}

	@Override
	public final ItemStack getItem() {
		final CompMaterial mat = this.getMenuItem();
		Valid.checkNotNull(mat);

		this.setCalledLocation(null);

		return ItemCreator

				.fromMaterial(mat)
				.name("&8> " + this.getColor() + "&l" + this.getName() + " &8<")

				.lore("&r")
				.lore(this.getDescription())
				.lore("&r")
				.lore("&7Break it to remove.")
				.unbreakable(true)
				.tag("Game", "Edit Item")
				.make();
	}

	// --------------------------------------------------------------------------------
	// Protected methods
	// --------------------------------------------------------------------------------

	/**
	 * Can the player visualize the clicked block?
	 */
	protected abstract boolean canVisualize(Block block, Player player);

	protected abstract CompMaterial getMenuItem();

	public abstract CompMaterial getMask();

	protected abstract String getName();

	protected abstract CompChatColor getColor();

	protected List<String> getDescription() {
		return Arrays.asList(
				this.getColor() + "&l<< &r" + this.getColor() + "Left click &7a block to set ",
				"&7" + this.getName().toLowerCase() + "&7");
	}

	protected String makeRemoveActionMessage() {
		return this.makeActionMessage("&4removed");
	}

	protected String makeActionMessage(final String action) {
		return "&9Setup > &7" + ChatUtil.capitalizeFully(this.getName()) + " &7has been " + action + "&7.";
	}

	protected String getBlockTitle(final Block block) {
		return this.getColoredName();
	}

	protected final void visualize(final Location loc) {
		this.visualizer.show(loc, this.getDefaultMode());
	}

	protected final void visualizeMask(final Location loc) {
		this.visualizer.show(loc, VisualizeMode.MASK);
	}

	protected final void visualizeGlow(final Location loc) {
		this.visualizer.show(loc, VisualizeMode.MASK);
	}

	protected final void hide(final Location loc) {
		this.visualizer.hide(loc);
	}

	// --------------------------------------------------------------------------------
	// Handle
	// --------------------------------------------------------------------------------

	protected void handleAirClick(final Player pl, final ItemStack item, final ClickType click) {
	}

	protected abstract void handleDataLoad(Player pl, Block block);

	protected abstract void handleBlockSelect(Player pl, Block block, BlockClick click);

	protected abstract void handleBlockBreak(Player pl, Block block);

	// --------------------------------------------------------------------------------
	// Final
	// --------------------------------------------------------------------------------

	protected final String getColoredName() {
		return this.getColor() + this.getName();
	}

	@Override
	public final boolean autoCancel() {
		return true;
	}

	@Override
	public final boolean ignoreCancelled() {
		return false;
	}

	// --------------------------------------------------------------------------------
	// Block manipulation
	// --------------------------------------------------------------------------------

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.getMask() + "}";
	}
}
