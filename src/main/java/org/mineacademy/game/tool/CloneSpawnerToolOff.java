package org.mineacademy.game.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.impl.SimpleSpawnPointMonster;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.CoreNBTUtil;
import org.mineacademy.game.util.CoreUtil;
import org.mineacademy.game.util.Permissions;

import lombok.Getter;

public class CloneSpawnerToolOff extends Tool {

	@Getter
	private static final CloneSpawnerToolOff instance = new CloneSpawnerToolOff();

	@Getter
	private final ItemStack item;

	private CloneSpawnerToolOff() {
		this.item = ItemCreator.of(
				CompMaterial.IRON_HOE,
				"&8> &f&lClone Mob Spawnpoint &8<",
				"",
				"&fRight click &7a monster spawner",
				"to save its copy into this tool.")
				.build().make();
	}

	@Override
	public void onBlockClick(PlayerInteractEvent e) {
		if (!CoreUtil.checkPerm(e.getPlayer(), Permissions.Tools.TOOLS))
			return;

		final Block clicked = e.getClickedBlock();
		final Action action = e.getAction();

		// Save a copy
		if (action == Action.RIGHT_CLICK_BLOCK) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(clicked.getLocation());

			if (arena != null && CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				final SpawnPoint point = arena.getData().findSpawnPoint(clicked.getLocation());

				if (point != null && point instanceof SimpleSpawnPointMonster) {
					ItemStack clone = CloneSpawnerToolOn.getInstance().getItem().clone();
					clone = CoreNBTUtil.writeSpawner((SimpleSpawnPointMonster) point, clone);

					Common.tell(e.getPlayer(), "&7Spawner copied. Don't remove the original spawner until you are done copying. Hover on tool to see usage.");

					PlayerUtil.updateInvSlot(e.getPlayer().getInventory(), e.getItem(), clone);

				} else
					Common.tell(e.getPlayer(), Localization.Arena.Setup.CLONE_MOB_UNKNOWN);
			}
		}

		e.setCancelled(true);
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
