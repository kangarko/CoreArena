package org.mineacademy.game.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.SimpleLocalization;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;

public class CoreUtil {

	public static final boolean checkMenuAccessInArena(Player player) throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
		final Arena arena = data.hasArenaCache() ? data.getArenaCache().getArena(player) : null;

		if (!data.hasArenaCache() || arena == null) {
			Common.tell(player, Localization.Menu.CANNOT_OPEN_OUTSIDE_ARENA);

			return false;
		}

		if (arena.getState() != ArenaState.LOBBY) {
			Common.tell(player, Localization.Menu.CANNOT_OPEN_OUTSIDE_LOBBY);

			return false;
		}

		return true;
	}

	public static final String getStateName(ArenaState state) {
		switch (state) {
			case LOBBY:
				return Localization.Arena.State.LOBBY;

			case RUNNING:
				return Localization.Arena.State.RUNNING;

			case STOPPED:
				return Localization.Arena.State.STOPPED;

			default:
				throw new FoException("Neznamy stav areny - " + state);
		}
	}

	public static final String formatTime(int seconds) {
		final int second = seconds % 60;
		int minute = seconds / 60;
		String hourMsg = "";

		if (minute >= 60) {
			final int hour = seconds / 60;
			minute %= 60;

			hourMsg = Localization.Cases.HOUR.formatWithCount(hour) + " ";
		}

		return hourMsg + (minute > 0 ? Localization.Cases.MINUTE.formatWithCount(minute) + " " : "") + Localization.Cases.SECOND.formatWithCount(second);
	}

	public static final boolean checkPerm(CommandSender sender, String permission) {
		return checkPerm(sender, permission, true);
	}

	public static final boolean checkPerm(CommandSender sender, String permission, boolean notify) {
		Valid.checkBoolean(!permission.contains("{plugin_name}"), "Found {plugin_name} while checking for " + permission + " - report this please!");
		final boolean has = PlayerUtil.hasPerm(sender, permission);

		if (!has && notify)
			Common.tell(sender, SimpleLocalization.NO_PERMISSION.replace("{permission}", permission));

		return has;
	}

	public static final boolean isWithinArena(Player player, Location loc) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		return arena != null && arena.getData().getRegion().isWithin(loc);
	}

	public static final List<String> tabCompleteArenaNames(String[] args) {
		final List<String> tab = new ArrayList<>();

		if (args.length == 1)
			for (final String key : CoreArenaPlugin.getArenaManager().getAvailable())
				if (key.toLowerCase().startsWith(args[0].toLowerCase()))
					tab.add(key);

		return tab;
	}
}
