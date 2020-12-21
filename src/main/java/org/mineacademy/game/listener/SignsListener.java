package org.mineacademy.game.listener;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.command.ClassCommand;
import org.mineacademy.game.exception.EventHandledException;
import org.mineacademy.game.impl.SimpleSignClass;
import org.mineacademy.game.impl.SimpleSignJoin;
import org.mineacademy.game.impl.SimpleSignLeave;
import org.mineacademy.game.impl.SimpleSignPower;
import org.mineacademy.game.impl.SimpleSignPower.PowerType;
import org.mineacademy.game.impl.SimpleSignUpgrade;
import org.mineacademy.game.manager.SimpleArenaManager;
import org.mineacademy.game.menu.MenuRewards;
import org.mineacademy.game.menu.MenuRewards.MenuMode;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ArenaManager;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSign.SignType;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.ArenaState;

public class SignsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockBreak(BlockBreakEvent e) {
		final SimpleArenaManager manager = CoreArenaPlugin.getArenaManager();
		final BlockState state = e.getBlock().getState();

		// Handle clicking arena sign
		if (state instanceof Sign) {
			final ArenaSign sign = manager.findSign((Sign) state);

			if (sign != null) {
				sign.removeSign();

				Common.tell(e.getPlayer(), Localization.Signs.REMOVED.replace("{arena}", sign.getArena().getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		final Player player = e.getPlayer();
		final String[] lines = Common.replaceNullWithEmpty(e.getLines());

		final String firstLine = lines[0];
		final String secondLine = lines[1];

		final Block block = e.getBlock();

		if (!(block.getState() instanceof Sign))
			return;

		final Sign sign = (Sign) block.getState();

		// A helper class to reduce duplicate code, see below
		// Most methods here do multiple things at once
		class $ {

			private void checkSecondLine(String... emptyMessage) {
				if (secondLine.isEmpty()) {
					Common.tellNoPrefix(player, emptyMessage);

					breakReturn();
				}
			}

			private Arena getArena(Block block) {
				final Arena arena = CoreArenaPlugin.getArenaManager().findArena(block.getLocation());

				if (arena == null) {
					Common.tell(player, Localization.Signs.OUTSIDE_ARENA);

					breakReturn();
				}

				return arena;
			}

			private Arena getArena(String name) {
				final Arena arena = CoreArenaPlugin.getArenaManager().findArena(name);

				if (arena == null) {
					Common.tell(player, Localization.Arena.Error.NOT_FOUND.replace("{arena}", name));

					breakReturn();
				}

				return arena;
			}

			private void update(SignType type, Arena arena) {
				Common.runLater(2, new BukkitRunnable() {
					@Override
					public void run() {
						arena.getData().getSigns().updateSigns(type, arena);
					}
				});
			}

			private int getNumber(String raw, String... falseMessage) {
				try {
					return Integer.parseInt(raw);

				} catch (final NumberFormatException ex) {
					Common.tell(player, falseMessage);

					breakReturn();
					return 0;
				}
			}

			private void checkNull(Object obj, String... falseMessage) {
				checkBoolean(obj != null, falseMessage);
			}

			private void checkBoolean(boolean bol, String... falseMessage) {
				if (!bol) {
					Common.tellNoPrefix(player, falseMessage);

					breakReturn();
				}
			}

			private void addSign(ArenaSign sign) {
				final Arena arena = sign.getArena();

				arena.getData().addSign(sign);
				arena.getMessenger().tell(player, Localization.Signs.CREATED.replace("{arena}", arena.getName()));

				update(sign.getType(), arena);
			}

			private void breakReturn() {
				block.breakNaturally();

				throw new EventHandledException();
			}
		}

		final $ $ = new $();

		try {
			// ** Join sign **
			// e.g. [ma] on first sign
			if (("[" + CoreArenaPlugin.getInstance().getMainCommand().getLabel().toLowerCase() + "]").equalsIgnoreCase(firstLine)) {
				$.checkSecondLine(Localization.Signs.Arena.HELP);
				final Arena arena = $.getArena(secondLine);

				$.addSign(new SimpleSignJoin(arena.getName(), sign));

			} else if (("[" + Settings.Signs.Label.UPGRADES + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = $.getArena(block);
				final String costLine = lines[2];
				$.checkBoolean(!costLine.isEmpty(), Localization.Signs.Upgrade.COST_MISSING);
				$.checkBoolean(!secondLine.isEmpty() && !costLine.isEmpty(), Localization.Signs.Upgrade.HELP);

				final Upgrade u = CoreArenaPlugin.getUpgradesManager().findUpgrade(secondLine);

				$.checkNull(u, Localization.Signs.OBJECT_NOT_FOUND.replace("{type}", "upgrade").replace("{line}", secondLine));

				final int cost = $.getNumber(costLine, Localization.Signs.Upgrade.COST_INVALID);

				$.addSign(new SimpleSignUpgrade(arena.getName(), u, cost, sign));
			} else if (("[" + Settings.Signs.Label.CLASS + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = $.getArena(block);
				final String classLine = lines[1];
				$.checkBoolean(!classLine.isEmpty(), Localization.Signs.Class.HELP);

				final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(classLine);
				$.checkNull(clazz, Localization.Signs.OBJECT_NOT_FOUND.replace("{type}", "class").replace("{line}", secondLine));

				$.addSign(new SimpleSignClass(arena.getName(), clazz, sign));
			} else if (("[" + Settings.Signs.Label.POWER + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = $.getArena(block);
				final String powerType = lines[1];
				$.checkBoolean(!powerType.isEmpty(), Localization.Signs.Power.HELP);

				final PowerType power = PowerType.fromLine(powerType);
				$.checkNull(power, Localization.Signs.Power.TYPE_INVALID.replace("{type}", powerType));

				$.addSign(new SimpleSignPower(power, arena.getName(), sign));
			} else if (("[" + Settings.Signs.Label.LEAVE + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = $.getArena(block);

				$.addSign(new SimpleSignLeave(arena.getName(), sign));
			} else if (Settings.Signs.ALLOW_CLASSES_SIGN && ("[" + Settings.Signs.Label.CLASSES + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = $.getArena(block);

				if (arena.getState() == ArenaState.LOBBY)
					ClassCommand.tryOpenMenu(player);
			}

		} catch (final EventHandledException ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onPlayerInteract(PlayerInteractEvent e) {
		if (!Remain.isInteractEventPrimaryHand(e))
			return;

		final Player pl = e.getPlayer();
		final ArenaManager manager = CoreArenaPlugin.getArenaManager();

		// Handle clicking arena sign
		if (e.hasBlock() && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign) {
			final Sign sign = (Sign) e.getClickedBlock().getState();

			final ArenaSign arenaSign = manager.findSign(sign);

			if (arenaSign != null) {
				final Arena edited = CoreArenaPlugin.getArenaManager().findEditedArena(pl);

				if (edited != null && edited.equals(arenaSign.getArena()))
					arenaSign.onSignSetupClick(pl);

				else if (arenaSign.getArena().getPlayers().contains(pl))
					arenaSign.onSignInGameClick(pl);

				else
					arenaSign.onSignOutGameClick(pl);

				e.setCancelled(true);
				return;
			}

			// We do not register these two internally
			else {
				final String title = sign.getLine(0).toLowerCase();

				if (("[" + Settings.Signs.Label.CLASSES + "]").equalsIgnoreCase(title))
					ClassCommand.tryOpenMenu(pl);

				else if (("[" + Settings.Signs.Label.REWARDS + "]").equalsIgnoreCase(title))
					if (!manager.isPlaying(pl))
						MenuRewards.showRewardsMenu(pl, MenuMode.PURCHASE);

			}
		}
	}
}