package org.mineacademy.corearena.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.util.CoreNBTUtil;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;

public final class CloneSpawnerToolOff extends Tool {

	@Getter
	private static final CloneSpawnerToolOff instance = new CloneSpawnerToolOff();

	@Getter
	private final ItemStack item;

	private CloneSpawnerToolOff() {
		this.item = ItemCreator.from(
				CompMaterial.IRON_HOE,
				"&8> &f&lClone Mob Spawnpoint &8<",
				"",
				"&fRight click &7a monster spawner",
				"to save its copy into this tool &2&l>>")
				.make();
	}

	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		if (!CoreUtil.checkPerm(event.getPlayer(), Permissions.Tools.TOOLS))
			return;

		final Block clicked = event.getClickedBlock();
		final Action action = event.getAction();

		// Save a copy
		if (action == Action.RIGHT_CLICK_BLOCK) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(clicked.getLocation());

			if (arena != null && CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				final SpawnPoint point = arena.getData().findSpawnPoint(clicked.getLocation());

				if (point instanceof SimpleSpawnPointMonster) {
					ItemStack clone = CloneSpawnerToolOn.getInstance().getItem().clone();
					clone = CoreNBTUtil.writeSpawner((SimpleSpawnPointMonster) point, clone);

					Messenger.info(event.getPlayer(), "&7Spawner copied. Don't remove the original spawner until you are done copying. Hover on tool to see usage.");
					PlayerUtil.updateInvSlot(event.getPlayer().getInventory(), event.getItem(), clone);

				} else
					Common.tell(event.getPlayer(), Lang.component("arena-setup-clone-mob-unknown"));
			}
		}

		event.setCancelled(true);
	}

	@Override
	public void onHotbarFocused(Player player) {
	}

	@Override
	public void onHotbarDefocused(Player player) {
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
