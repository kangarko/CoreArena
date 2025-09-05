package org.mineacademy.corearena.command;

import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.InArenaCache;
import org.mineacademy.corearena.type.ArenaState;

final class LevelCommand extends AbstractCoreSubcommand {

	public LevelCommand() {
		super("level");

		this.setValidArguments(0, 5);
		this.setDescription("Manipulate levels of in-arena players.");
		this.setUsage("[player] or <player> <give/set/take> <level>");
	}

	@Override
	protected void onCommand() {
		final Player target = this.findPlayerOrSelf(0);
		final ArenaPlayer cache = this.getCache(target);

		this.checkBoolean(cache.hasArenaCache(), target.getName() + " is not playing in any arena.");

		final InArenaCache arenaCache = cache.getArenaCache();
		this.checkBoolean(arenaCache.getArena(target).getState() == ArenaState.RUNNING, target.getName() + "'s arena must be played.");

		final int level = arenaCache.getLevel();

		if (this.args.length == 1) {
			this.tellInfo("&f" + target.getName() + " &7has level " + level);

			return;
		}

		this.checkArgs(3, "Usage: /{label} {sublabel} <player> <give/set/take> <level>");

		final String param = this.args[1].toLowerCase();
		final int amount = this.findInt(2, 0, Integer.MAX_VALUE, "Levels must be a whole number.");

		if ("give".equals(param)) {
			arenaCache.setLevel(target, level + amount);

			this.tellSuccess("Gave &f" + target.getName() + " &7" + amount + " levels. New level: " + arenaCache.getLevel() + ".");

		} else if ("take".equals(param)) {
			this.checkBoolean(level - amount >= 0, "Cannot take more levels than the player has (" + level + ").");
			arenaCache.setLevel(target, level - amount);

			this.tellSuccess("Took &f" + target.getName() + " &7" + amount + " levels. New level: " + arenaCache.getLevel() + ".");

		} else if ("set".equals(param)) {
			arenaCache.setLevel(target, amount);

			this.tellSuccess("Set &f" + target.getName() + " &7level to " + amount + ".");

		} else
			this.returnInvalidArgs(param);
	}

	@Override
	public List<String> tabComplete() {
		switch (this.args.length) {
			case 1:
				return this.completeLastWordPlayerNames();
			case 2:
				return this.completeLastWord("give", "set", "take");
			case 3:
				return this.completeLastWord("1", "10", "100");
		}

		return NO_COMPLETE;
	}
}