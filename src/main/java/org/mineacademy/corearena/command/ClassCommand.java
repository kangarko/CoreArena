package org.mineacademy.corearena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.InArenaCache;
import org.mineacademy.corearena.menu.MenuInArenaClasses;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.settings.Lang;

public final class ClassCommand extends AbstractCoreSubcommand {

	public ClassCommand() {
		super("class|cl");

		this.setValidArguments(0, 0);
		this.setDescription("Select your class in an arena.");
	}

	@Override
	protected void onCommand() {
		this.checkConsole();

		tryOpenMenu(this.getPlayer());
	}

	public static boolean canOpenMenu(Player player, CommandSender sender) {
		if (!CoreUtil.checkMenuAccessInArena(player, sender))
			return false;

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();
		final Arena arena = cache.getArena(player);

		if (arena.getSettings().allowOwnEquipment()) {
			Messenger.error(sender != null ? sender : player, Lang.component("arena-doesnt-support-classes", "arena", arena.getName()));

			return false;
		}

		return true;
	}

	public static void tryOpenMenu(Player player) {
		if (canOpenMenu(player, null)) {
			final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();
			final Arena arena = cache.getArena(player);

			new MenuInArenaClasses(arena, player).displayTo(player);
		}
	}
}