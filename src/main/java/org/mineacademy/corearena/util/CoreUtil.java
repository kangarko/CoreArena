package org.mineacademy.corearena.util;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.Lang;

public class CoreUtil {

	public static final boolean checkMenuAccessInArena(Player player, CommandSender sender) throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
		final Arena arena = data.hasArenaCache() ? data.getArenaCache().getArena(player) : null;

		if (!data.hasArenaCache() || arena == null) {
			Messenger.error(sender != null ? sender : player, Lang.component("menu-cannot-open-outside-arena"));

			return false;
		}

		if (arena.getState() != ArenaState.LOBBY) {
			Messenger.error(sender != null ? sender : player, Lang.component("menu-cannot-open-outside-lobby"));

			return false;
		}

		return true;
	}

	public static final String getStateName(ArenaState state) {
		switch (state) {
			case LOBBY:
				return Lang.legacy("arena-state-lobby");

			case RUNNING:
				return Lang.legacy("arena-state-running");

			case STOPPED:
				return Lang.legacy("arena-state-stopped");

			default:
				throw new FoException("Neznámý stav arény - " + state);
		}
	}

	public static final boolean checkPerm(CommandSender sender, String permission) {
		return checkPerm(sender, permission, true);
	}

	public static final boolean checkPerm(CommandSender sender, String permission, boolean notify) {
		Valid.checkBoolean(!permission.contains("{plugin_name}"), "Found {plugin_name} while checking for " + permission + " - report this please!");
		final boolean has = sender.hasPermission(permission);

		if (!has && notify)
			Messenger.error(sender, Lang.component("no-permission", "permission", permission));

		return has;
	}

	public static final boolean isWithinArena(Player player, Location loc) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		return arena != null && arena.getData().getRegion().isWithin(loc);
	}
}
