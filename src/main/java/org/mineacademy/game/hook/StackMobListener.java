package org.mineacademy.game.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaManager;

import uk.antiperson.stackmob.entity.StackEntity;
import uk.antiperson.stackmob.events.StackMergeEvent;

public class StackMobListener implements Listener {

	@EventHandler
	public final void onEntityStack(StackMergeEvent event) {
		final StackEntity stacked = event.getStackEntity();
		final StackEntity nearby = event.getNearbyStackEntity();

		if (stacked != null) {
			final ArenaManager manager = CoreArenaPlugin.getArenaManager();

			final Arena arena = manager.findArena(stacked.getEntity().getLocation());
			final Arena arenaSecond = manager.findArena(nearby.getEntity().getLocation());

			if (arena != null || arenaSecond != null)
				event.setCancelled(true);
		}
	}
}
