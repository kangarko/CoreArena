package org.mineacademy.game.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.impl.SimpleSpawnPointMonster;
import org.mineacademy.game.impl.SpawnedEntity;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.CoreNBTUtil;
import org.mineacademy.game.util.CoreUtil;
import org.mineacademy.game.util.Permissions;
import org.mineacademy.game.visualize.VisualizeMode;

import lombok.Getter;

public class CloneSpawnerToolOn extends Tool {

	@Getter
	private static final CloneSpawnerToolOn instance = new CloneSpawnerToolOn();

	@Getter
	private final ItemStack item;

	private CloneSpawnerToolOn() {
		this.item = ItemCreator.of(
				CompMaterial.IRON_HOE,
				"&8> &f&lCloned Mob Spawnpoint &8<",
				"",
				"This item helds a cloned",
				"monster spawner point.",
				"",
				"&2&l< &7Left click to &bclear",
				"&2&l> &7Right click block to &6place",
				"&2&l> &7Right click air for &finfo")
				.enchant(new SimpleEnchant(Enchantment.DURABILITY))
				.hideTags(true)
				.build().make();
	}

	@Override
	public void onBlockClick(PlayerInteractEvent e) {
		if (!CoreUtil.checkPerm(e.getPlayer(), Permissions.Tools.TOOLS))
			return;

		final Action action = e.getAction();
		final Player player = e.getPlayer();
		final Block clicked = e.getClickedBlock();

		final SimpleSpawnPointMonster cloned = CoreNBTUtil.readSpawner(e.getItem());
		Valid.checkNotNull(cloned, "Clone Tool for " + player.getName() + " is ON but lacks a spawner point");

		final Arena arena = e.hasBlock() ? CoreArenaPlugin.getArenaManager().findArena(clicked.getLocation()) : null;
		final SpawnPoint existing = arena != null ? arena.getData().findSpawnPoint(clicked.getLocation()) : null;

		if (existing != null)
			return;

		if (action == Action.RIGHT_CLICK_BLOCK)
			if (arena != null && CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				simulateSpawnToolClick(e);

				cloned.setLocation(clicked.getLocation());
				arena.getData().updateSpawnPoint(cloned);
			}

		if (action == Action.RIGHT_CLICK_AIR)
			tellHeldInfo(player, cloned);

		if (action.toString().contains("LEFT_CLICK")) {
			final ItemStack off_tool = CloneSpawnerToolOff.getInstance().getItem();

			PlayerUtil.updateInvSlot(e.getPlayer().getInventory(), e.getItem(), off_tool);
		}

		e.setCancelled(true);
	}

	private final void simulateSpawnToolClick(PlayerInteractEvent e) {
		final SpawnSelectorMonster selector = SpawnSelectorMonster.getInstance();

		selector.setDefaultMode(VisualizeMode.MASK);

		selector.onBlockClick(e);

		selector.setDefaultMode(VisualizeMode.GLOW);
	}

	private final void tellHeldInfo(Player pl, SimpleSpawnPointMonster point) {
		final int total = point.getSpawnedTypes() != null ? point.getSpawnedTypes().length : 0;

		int count = 0;
		String from = "";

		if (total > 0)
			for (final SpawnedEntity s : point.getSpawnedTypes()) {
				count++;

				if (s != null)
					from += s.getCount() + " " + (s.isCustom() ? "custom monster" : ItemUtil.bountifyCapitalized(s.getType())) + (s.getCount() > 1 ? "s" : "") + (count + 1 == total ? " &7or &6" : count == total ? "" : "&7, &6");
			}
		else
			from = Localization.Parts.NONE;

		BoxedMessage.tell(pl,
				"<center>&n&l" + ItemUtil.bountifyCapitalized(point.getType()) + " Spawnpoint&r",
				" ",
				"&8* &7Spawns" + (count > 1 ? " either:" : "") + " &6" + from,
				"&8* &7Chance: &6" + point.getChance() + "%",
				"&8* &7Minimum players: &6" + point.getMinimumPlayers(),
				"&8* &7Active period: &6" + point.getActivePeriod().formatPeriod() + ". phase");
	}

	@Override
	public void onHotbarFocused(Player pl) {
	}

	@Override
	public void onHotbarDefocused(Player pl) {
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
