package org.mineacademy.corearena.listener;

import java.util.Arrays;
import java.util.List;

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
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.ClassCommand;
import org.mineacademy.corearena.exception.EventHandledException;
import org.mineacademy.corearena.impl.SimpleSignClass;
import org.mineacademy.corearena.impl.SimpleSignJoin;
import org.mineacademy.corearena.impl.SimpleSignLeave;
import org.mineacademy.corearena.impl.SimpleSignPower;
import org.mineacademy.corearena.impl.SimpleSignPower.PowerType;
import org.mineacademy.corearena.impl.SimpleSignUpgrade;
import org.mineacademy.corearena.manager.SimpleArenaManager;
import org.mineacademy.corearena.menu.MenuRewards;
import org.mineacademy.corearena.menu.MenuRewards.MenuMode;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ArenaManager;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSign.SignType;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

public class SignsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockBreak(BlockBreakEvent event) {
		final SimpleArenaManager manager = CoreArenaPlugin.getArenaManager();
		final BlockState state = event.getBlock().getState();

		// Handle clicking arena sign
		if (state instanceof Sign) {
			final ArenaSign sign = manager.findSign((Sign) state);

			if (sign != null) {
				sign.removeSign();

				Common.tell(event.getPlayer(), Lang.component("sign-removed", "arena", sign.getArena().getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		final Player player = event.getPlayer();
		final String[] lines = Common.replaceNullWithEmpty(event.getLines());

		final String firstLine = lines[0];
		final String secondLine = lines[1];

		final Block block = event.getBlock();

		if (!(block.getState() instanceof Sign))
			return;

		final Sign sign = (Sign) block.getState();

		// A helper class to reduce duplicate code, see below
		// Most methods here do multiple things at once
		class Helper {

			private void checkSecondLine(String... emptyMessage) {
				this.checkSecondLine(Arrays.asList(emptyMessage));
			}

			private void checkSecondLine(List<String> emptyMessage) {
				if (secondLine.isEmpty()) {
					for (final String messageLine : emptyMessage)
						Common.tell(player, messageLine);

					this.breakReturn();
				}
			}

			private Arena getArena(Block block) {
				final Arena arena = CoreArenaPlugin.getArenaManager().findArena(block.getLocation());

				if (arena == null) {
					Common.tell(player, Lang.component("sign-outside-arena"));

					this.breakReturn();
				}

				return arena;
			}

			private Arena getArena(String name) {
				final Arena arena = CoreArenaPlugin.getArenaManager().findArena(name);

				if (arena == null) {
					Common.tell(player, Lang.component("arena-error-not-found", "arena", name));

					this.breakReturn();
				}

				return arena;
			}

			private void update(SignType type, Arena arena) {
				Platform.runTask(2, new SimpleRunnable() {
					@Override
					public void run() {
						arena.getData().getSigns().updateSigns(type, arena);
					}
				});
			}

			private int getNumber(String raw, String... falseMessage) {
				try {
					final int level = Integer.parseInt(raw);

					if (level < 1)
						throw new NumberFormatException();

					return level;

				} catch (final NumberFormatException ex) {
					for (final String line : falseMessage)
						Messenger.error(player, line);

					this.breakReturn();
					return 0;
				}
			}

			private void checkNull(Object obj, String... falseMessage) {
				this.checkNull(obj, Arrays.asList(falseMessage));
			}

			private void checkNull(Object obj, List<String> falseMessage) {
				this.checkBoolean(obj != null, falseMessage);
			}

			private void checkBoolean(boolean bol, String... falseMessage) {
				this.checkBoolean(bol, Arrays.asList(falseMessage));
			}

			private void checkBoolean(boolean bol, List<String> falseMessage) {
				if (!bol) {
					for (final String line : falseMessage)
						Messenger.error(player, line);

					this.breakReturn();
				}
			}

			private void addSign(ArenaSign sign) {
				final Arena arena = sign.getArena();

				arena.getData().addSign(sign);
				arena.getMessenger().tell(player, Lang.legacy("sign-created", "arena", arena.getName()));

				this.update(sign.getType(), arena);
			}

			private void breakReturn() {
				block.breakNaturally();

				throw new EventHandledException();
			}
		}

		final Helper helper = new Helper();

		try {
			// ** Join sign **
			// e.g. [ma] on first sign
			if (("[" + CoreArenaPlugin.getInstance().getDefaultCommandGroup().getLabel().toLowerCase() + "]").equalsIgnoreCase(firstLine)) {
				helper.checkSecondLine(Lang.legacy("sign-arena-help").split("\n"));
				final Arena arena = helper.getArena(secondLine);

				helper.addSign(new SimpleSignJoin(arena.getName(), sign));

			} else if (("[" + Settings.Signs.Label.UPGRADES + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = helper.getArena(block);
				final String costLine = lines[2];
				helper.checkBoolean(!costLine.isEmpty(), Lang.legacy("sign-upgrade-cost-missing"));
				helper.checkBoolean(!secondLine.isEmpty() && !costLine.isEmpty(), Lang.legacy("sign-upgrade-help"));

				final Upgrade u = CoreArenaPlugin.getUpgradesManager().findUpgrade(secondLine);

				helper.checkNull(u, Lang.legacy("sign-object-not-found", "type", "upgrade", "line", secondLine));

				final int cost = helper.getNumber(costLine, Lang.legacy("sign-upgrade-cost-invalid"));

				helper.addSign(new SimpleSignUpgrade(arena.getName(), u, cost, sign));
			} else if (("[" + Settings.Signs.Label.CLASS + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = helper.getArena(block);
				final String classLine = lines[1];
				helper.checkBoolean(!classLine.isEmpty(), Lang.legacy("sign-class-help"));

				final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(classLine);
				helper.checkNull(clazz, Lang.legacy("sign-object-not-found", "type", "class", "line", secondLine));

				helper.addSign(new SimpleSignClass(arena.getName(), clazz, sign));
			} else if (("[" + Settings.Signs.Label.POWER + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = helper.getArena(block);
				final String powerType = lines[1];
				helper.checkBoolean(!powerType.isEmpty(), Lang.legacy("sign-power-help"));

				final PowerType power = PowerType.fromLine(powerType);
				helper.checkNull(power, Lang.legacy("sign-power-type-invalid", "type", powerType));

				helper.addSign(new SimpleSignPower(power, arena.getName(), sign));
			} else if (("[" + Settings.Signs.Label.LEAVE + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = helper.getArena(block);

				helper.addSign(new SimpleSignLeave(arena.getName(), sign));
			} else if (Settings.Signs.ALLOW_CLASSES_SIGN && ("[" + Settings.Signs.Label.CLASSES + "]").equalsIgnoreCase(firstLine)) {
				final Arena arena = helper.getArena(block);

				if (arena.getState() == ArenaState.LOBBY)
					ClassCommand.tryOpenMenu(player);
			}
		} catch (final EventHandledException ex) {
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onPlayerInteract(PlayerInteractEvent event) {
		if (!Remain.isInteractEventPrimaryHand(event))
			return;

		final Player player = event.getPlayer();

		// Fix bug in older Spigot versions where the event is called while browsing GUI
		if (player.hasMetadata(Menu.TAG_MENU_CURRENT))
			return;

		final ArenaManager manager = CoreArenaPlugin.getArenaManager();

		// Handle clicking arena sign
		if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
			final Sign sign = (Sign) event.getClickedBlock().getState();

			final ArenaSign arenaSign = manager.findSign(sign);

			if (arenaSign != null) {
				final Arena edited = CoreArenaPlugin.getArenaManager().findEditedArena(player);

				if (edited != null && edited.equals(arenaSign.getArena()))
					arenaSign.onSignSetupClick(player);

				else if (arenaSign.getArena().getPlayers().contains(player))
					arenaSign.onSignInGameClick(player);

				else
					arenaSign.onSignOutGameClick(player);

				event.setCancelled(true);
				return;
			}

			// We do not register these two internally
			else {
				final String title = sign.getLine(0).toLowerCase();

				if (("[" + Settings.Signs.Label.CLASSES + "]").equalsIgnoreCase(title))
					ClassCommand.tryOpenMenu(player);

				else if (("[" + Settings.Signs.Label.REWARDS + "]").equalsIgnoreCase(title))
					if (!manager.isPlaying(player))
						MenuRewards.showRewardsMenu(player, MenuMode.PURCHASE);

			}
		}
	}
}