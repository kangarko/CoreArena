package org.mineacademy.game.hook;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.model.Arena;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public final class ArenaPAPIPlaceholder extends PlaceholderExpansion {

	@Override
	public String getIdentifier() {
		return SimplePlugin.getNamed();
	}

	@Override
	public String getAuthor() {
		return SimplePlugin.getInstance().getDescription().getAuthors().get(0);
	}

	@Override
	public String getVersion() {
		return SimplePlugin.getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	@Nullable
	public String onPlaceholderRequest(Player player, String identifier) {
		final String[] args = identifier.split("\\_");
		final ArenaPlayer cache = player != null ? CoreArenaPlugin.getDataFor(player) : null;

		if (player != null) {
			if (identifier.equalsIgnoreCase("is_playing"))
				return cache.hasArenaCache() ? "yes" : "no";

			else if (identifier.equalsIgnoreCase("nuggets"))
				return String.valueOf(cache.getNuggets());

			else if (identifier.equalsIgnoreCase("class"))
				return cache.getClassCache().assignedClass != null ? cache.getClassCache().assignedClass.getName() : "none";

			// arena_name
			if (args.length == 1) {
				if ("arena".equals(identifier))
					return cache.hasArenaCache() ? cache.getArenaCache().getArena(player).getName() : "dead";

				return null;
			}
		}

		final String arenaName = args[0];
		Arena arena = CoreArenaPlugin.getArenaManager().findArena(arenaName);

		if (arena == null && cache != null && cache.hasArenaCache() && "player".equals(arenaName))
			arena = cache.getArenaCache().getArena(player);

		if (arena == null)
			return "no arena " + arenaName;

		// arenaname_player_{X}_{variable}
		if (args.length == 4 && "player".equals(args[1]) && Valid.isInteger(args[2])) {
			final List<Player> players = new ArrayList<>(arena.getPlayers());
			final int playerNumber = Integer.parseInt(args[2]) - 1;
			final Player selected = playerNumber < players.size() && playerNumber >= 0 ? players.get(playerNumber) : null;

			return parseVariables(arena, args[3], selected, args);
		}

		// arenaname_{player}_{variable}
		else if (args.length == 3) {
			Player selected = Bukkit.getPlayer(args[1]);

			if (selected == null && player != null && "current".equals(args[1]))
				selected = player;

			return parseVariables(arena, args[2], selected, args);
		}

		// arenaname_variable
		else if (args.length == 2) {
			final String param = args[1];

			if ("mobcount".equals(param))
				return String.valueOf(arena.getAliveMonsters());

			else if ("maxphase".equals(param))
				return String.valueOf(arena.getSettings().getMaxPhase() == -1 ? "infinite" : arena.getSettings().getMaxPhase());

			else if ("phase".equals(param))
				return String.valueOf(arena.getPhase().getCurrent());

			else if ("remaining".equals(param))
				return arena.getRemainingSeconds() > 7200 ? "never" : TimeUtil.formatTimeShort(arena.getRemainingSeconds());

			else if ("alive".equals(param))
				return String.valueOf(arena.getPlayers().size());

			else if ("lives".equals(param))
				return String.valueOf(arena.getSettings().getLifes());

		}

		return null;
	}

	private String parseVariables(Arena arena, String param, Player selected, String[] args) {
		if (selected == null)
			return "dead";

		if ("name".equals(param))
			return selected.getName();

		final ArenaPlayer cache = CoreArenaPlugin.getDataFor(selected);

		if (!cache.hasArenaCache())
			return "dead";

		if ("health".equals(param))
			return selected.isDead() ? "dead" : MathUtil.formatTwoDigits(selected.getHealth());

		else if ("location".contains(param)) {
			final Location loc = selected.getLocation();

			return selected.isDead() ? "dead" : "x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ();
		}

		else if ("exp".equals(param))
			return String.valueOf(cache.getArenaCache().getExp());

		else if ("level".equals(param))
			return String.valueOf(cache.getArenaCache().getLevel());

		else if ("livesleft".equals(param))
			return String.valueOf(cache.getArenaCache().lifesLeft);

		else if ("nearestmob".equals(param)) {
			final Location playerLocation = selected.getLocation();
			Location closest = null;

			for (final Entity entity : arena.getData().getRegion().getEntities())
				if (EntityUtil.isAggressive(entity)) {
					final Location entityLocation = entity.getLocation();

					if (closest == null || entityLocation.distance(playerLocation) < closest.distance(playerLocation))
						closest = entityLocation;
				}

			return closest == null ? "no mobs" : closest.getBlockX() + " " + closest.getBlockY() + " " + closest.getBlockZ();
		}

		else if ("nearestmobmeters".equals(param)) {
			final Location playerLocation = selected.getLocation();
			Location closest = null;

			for (final Entity entity : arena.getData().getRegion().getEntities())
				if (EntityUtil.isAggressive(entity)) {
					final Location entityLocation = entity.getLocation();

					if (closest == null || entityLocation.distance(playerLocation) < closest.distance(playerLocation))
						closest = entityLocation;
				}

			return closest == null ? "no mobs" : MathUtil.formatTwoDigits(closest.distance(playerLocation)) + "m";
		}

		return null;
	}
}
