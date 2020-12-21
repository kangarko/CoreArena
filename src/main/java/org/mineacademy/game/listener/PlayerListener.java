package org.mineacademy.game.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.game.data.GeneralDataSection;
import org.mineacademy.game.event.PluginShouldFireMenuEvent;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.MySQLDatabase;
import org.mineacademy.game.menu.MenuInArenaClasses;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.LeaveCause;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent e) {
		final Player pl = e.getPlayer();
		final ArenaPlayer cache = CoreArenaPlugin.getDataFor(pl);
		final GeneralDataSection generalData = GeneralDataSection.getInstance();

		// Load MySQL
		Common.runLater(Settings.MySQL.DELAY_TICKS, () -> MySQLDatabase.load(cache));

		// Check pending location
		final Location prevLocation = generalData.getPendingLocation(pl.getUniqueId());

		if (prevLocation != null)
			Common.runLater(2, () -> {
				generalData.setPendingLocation(pl.getUniqueId(), null);

				pl.teleport(prevLocation);
			});

		// If we don't teleport them to a specific location, check if we can teleport to spawn
		else {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl.getLocation());

			if (arena != null && Settings.Arena.MOVE_FORGOTTEN_PLAYERS && !CoreArenaPlugin.DEBUG_EDITING_MODE)
				arena.teleportPlayerBack(pl);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(PlayerInteractEvent e) {
		final Player player = e.getPlayer();

		if (player.getItemInHand() != null) {
			final ItemStack hand = player.getItemInHand();
			final String entity = CompMetadata.getMetadata(hand, "Game_Egg");

			if (entity != null)
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onMenuShouldFire(PluginShouldFireMenuEvent e) {
		if (!e.getPlugin().getName().equals(SimplePlugin.getNamed()))
			return;

		final Player pl = e.getPlayer();

		switch (e.getType()) {

			case CLASSES: {
				Valid.checkBoolean(e.getData() != null && e.getData().length == 1 && e.getData()[0] instanceof Arena, "To open class menu specify to which arena to open");

				new MenuInArenaClasses((Arena) e.getData()[0], pl).displayTo(pl);
				break;
			}

			default:
				break;
		}
	}

	@EventHandler
	public final void onQuit(PlayerQuitEvent e) {
		final Player pl = e.getPlayer();
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);

		if (data.hasArenaCache()) {
			if (data.getArenaCache().prevLocation != null)
				GeneralDataSection.getInstance().setPendingLocation(pl.getUniqueId(), data.getArenaCache().prevLocation);

			data.getArenaCache().getArena(pl).kickPlayer(pl, LeaveCause.DISCONNECT);
		}

		if (data.hasSetupCache())
			CoreArenaPlugin.getSetupManager().removeEditedArena(data.getSetupCache().arena);

		MySQLDatabase.save(data, true);
		CoreArenaPlugin.trashDataFor(pl);
	}
}
