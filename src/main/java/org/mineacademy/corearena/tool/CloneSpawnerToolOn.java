package org.mineacademy.corearena.tool;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.impl.SpawnedEntity;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.util.CoreNBTUtil;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.corearena.visualize.VisualizeMode;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;

public final class CloneSpawnerToolOn extends Tool {

	@Getter
	private static final CloneSpawnerToolOn instance = new CloneSpawnerToolOn();

	@Getter
	private final ItemStack item;

	private CloneSpawnerToolOn() {
		this.item = ItemCreator.from(
				CompMaterial.IRON_HOE,
				"&8> &f&lCloned Mob Spawnpoint &8<",
				"",
				"This item helds a cloned",
				"monster spawner point.",
				"",
				"&2&l<< &7Left click to &bclear",
				"&7Right click block to &6place &2&l>>",
				"&7Right click air for &finfo &2&l>>")
				.enchant(CompEnchantment.DURABILITY)
				.tag("CloneSpawnerToolOn", "N/A")
				.hideTags(true)
				.make();
	}

	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		if (!CoreUtil.checkPerm(event.getPlayer(), Permissions.Tools.TOOLS))
			return;

		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final Block clicked = event.getClickedBlock();

		if (action.toString().contains("LEFT_CLICK")) {
			final ItemStack off_tool = CloneSpawnerToolOff.getInstance().getItem();

			PlayerUtil.updateInvSlot(event.getPlayer().getInventory(), event.getItem(), off_tool);
			Messenger.info(player, "Cleared the cloned spawnpoint on your tool.");

			event.setCancelled(true);
			return;
		}

		final Location clonedLocation = CoreNBTUtil.readSpawnPointLocation(event.getItem());
		final SimpleSpawnPointMonster originalSpawnpoint = CoreNBTUtil.findSpawnPoint(clonedLocation);

		if (originalSpawnpoint == null) {
			Messenger.error(player, "The original spawnpoint" + (clonedLocation != null ? " at " + SerializeUtil.serializeLocation(clonedLocation) : "") + " was destroyed, cannot clone.");

			event.setCancelled(true);
			return;
		}

		final Arena arena = event.hasBlock() ? CoreArenaPlugin.getArenaManager().findArena(clicked.getLocation()) : null;
		final SpawnPoint existing = arena != null ? arena.getData().findSpawnPoint(clicked.getLocation()) : null;

		if (existing != null)
			return;

		if (action == Action.RIGHT_CLICK_BLOCK)
			if (arena != null && CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				this.simulateSpawnToolClick(event);

				final SimpleSpawnPointMonster cloned = originalSpawnpoint.clone();

				cloned.setLocation(clicked.getLocation());
				arena.getData().updateSpawnPoint(cloned);
			}

		if (action == Action.RIGHT_CLICK_AIR)
			this.tellHeldInfo(player, originalSpawnpoint);

		event.setCancelled(true);
	}

	private void simulateSpawnToolClick(PlayerInteractEvent event) {
		final SpawnpointMonsterTool selector = SpawnpointMonsterTool.getInstance();

		selector.setDefaultMode(VisualizeMode.MASK);

		selector.onBlockClick(event);

		selector.setDefaultMode(VisualizeMode.GLOW);
	}

	private void tellHeldInfo(Player player, SimpleSpawnPointMonster point) {
		final int total = point.getSpawnedTypes() != null ? point.getSpawnedTypes().length : 0;

		int count = 0;
		String from = "";

		if (total > 0)
			for (final SpawnedEntity spawned : point.getSpawnedTypes()) {
				count++;

				if (spawned != null)
					from += spawned.getCount() + " " + (spawned.isCustom() ? "custom monster" : ChatUtil.capitalizeFully(spawned.getType())) + (spawned.getCount() > 1 ? "s" : "") + (count + 1 == total ? " &7or &6" : count == total ? "" : "&7, &6");
			}
		else
			from = Lang.legacy("part-none");

		Common.tellBoxed(player,
				"<center>&n&l" + ChatUtil.capitalizeFully(point.getType()) + " Spawnpoint&r",
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
	public boolean compareByNbt() {
		return true;
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
