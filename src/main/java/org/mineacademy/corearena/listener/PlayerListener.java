package org.mineacademy.corearena.listener;

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
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.event.PluginShouldFireMenuEvent;
import org.mineacademy.corearena.impl.MySQLDatabase;
import org.mineacademy.corearena.menu.MenuInArenaClasses;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.corearena.util.InventoryStorageUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMetadata;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final ArenaPlayer cache = CoreArenaPlugin.getDataFor(player);
		final AllData generalData = AllData.getInstance();

		// Load MySQL
		MySQLDatabase.load(cache);

		// Check pending location
		final Location prevLocation = generalData.getPendingLocation(player.getUniqueId());

		if (prevLocation != null)
			Platform.runTask(1, () -> {
				generalData.setPendingLocation(player.getUniqueId(), null);

				player.teleport(prevLocation);
			});

		// If we don't teleport them to a specific location, check if we can teleport to spawn
		else {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player.getLocation());

			if (arena != null && Settings.Arena.MOVE_FORGOTTEN_PLAYERS && !CoreArenaPlugin.DEBUG_EDITING_MODE)
				arena.teleportPlayerBack(player);
		}

		Platform.runTask(4, () -> {
			if (player.isOnline())
				InventoryStorageUtil.getInstance().restoreIfStored(player);
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		// Fix bug in older Spigot versions where the event is called while browsing GUI
		if (player.hasMetadata(Menu.TAG_MENU_CURRENT))
			return;

		if (player.getItemInHand() != null) {
			final ItemStack hand = player.getItemInHand();
			final String entity = CompMetadata.getMetadata(hand, "Game_Egg");

			if (entity != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onMenuShouldFire(PluginShouldFireMenuEvent e) {
		if (!e.getPlugin().getName().equals(Platform.getPlugin().getName()))
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
	public final void onQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

		if (data.hasArenaCache()) {
			if (data.getArenaCache().prevLocation != null)
				AllData.getInstance().setPendingLocation(player.getUniqueId(), data.getArenaCache().prevLocation);

			data.getArenaCache().getArena(player).kickPlayer(player, LeaveCause.DISCONNECT);
		}

		if (data.hasSetupCache())
			CoreArenaPlugin.getSetupManager().removeEditedArena(data.getSetupCache().arena);

		MySQLDatabase.save(data, true);

		CoreArenaPlugin.trashDataFor(player);
	}
}
