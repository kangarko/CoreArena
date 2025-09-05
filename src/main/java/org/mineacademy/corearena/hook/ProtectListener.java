package org.mineacademy.corearena.hook;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.protect.api.InventoryScanEvent;
import org.mineacademy.protect.api.ItemScanEvent;
import org.mineacademy.protect.api.PlayerScanEvent;

public final class ProtectListener implements Listener {

	@EventHandler
	public void onItemScan(ItemScanEvent event) {
		final Entity entity = event.getItem();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(entity.getLocation());

		if (arena != null)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerScan(PlayerScanEvent event) {
		this.ignoreIfInArena(event.getPlayer(), event);
	}

	@EventHandler
	public void onItemScan(InventoryScanEvent event) {
		this.ignoreIfInArena(event.getPlayer(), event);
	}

	private void ignoreIfInArena(Player player, Cancellable event) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null)
			event.setCancelled(true);
	}
}
