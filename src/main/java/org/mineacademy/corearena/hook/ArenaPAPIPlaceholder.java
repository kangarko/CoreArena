package org.mineacademy.corearena.hook;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.model.SimpleExpansion;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.remain.Remain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AutoRegister
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArenaPAPIPlaceholder extends SimpleExpansion {

	@Getter
	private static final ArenaPAPIPlaceholder instance = new ArenaPAPIPlaceholder();

	@Override
	protected String onReplace(FoundationPlayer audience, String identifier) {
		final Player player = audience != null && audience.isPlayer() ? audience.getPlayer() : null;
		final String[] args = identifier.split("\\_");
		final ArenaPlayer cache = player != null ? CoreArenaPlugin.getDataFor(player) : null;

		if (player != null) {
			if (identifier.equalsIgnoreCase("is_playing"))
				return cache.hasArenaCache() ? "yes" : "no";

			if (identifier.equalsIgnoreCase("is_in_lobby")) {
				if (!cache.hasArenaCache())
					return "no";

				final Arena arena = cache.getArenaCache().getArena(player);
				return arena.getState() == ArenaState.LOBBY ? "yes" : "no";
			}

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
		Arena arena = null;

		if ("player".equals(arenaName)) {
			if (cache != null) {
				if (!cache.hasArenaCache())
					return "not playing";

				arena = cache.getArenaCache().getArena(player);
			}

		} else if ("location".equals(arenaName)) {
			arena = CoreArenaPlugin.getArenaManager().findArena(player.getLocation());

		} else
			arena = CoreArenaPlugin.getArenaManager().findArena(arenaName);

		// arenaname_player_{X}_{variable}
		if (args.length == 4 && "player".equals(args[1]) && Valid.isInteger(args[2])) {
			if (arena == null)
				return "no arena " + arenaName;

			final List<Player> players = new ArrayList<>(arena.getPlayers());
			final int playerNumber = Integer.parseInt(args[2]) - 1;
			final Player selected = playerNumber < players.size() && playerNumber >= 0 ? players.get(playerNumber) : null;

			return this.parseVariables(arena, args[3], selected, args);
		}

		// arenaname_{player}_{variable}
		else if (args.length == 3) {
			if (arena == null)
				return "no arena " + arenaName;

			Player selected = Bukkit.getPlayer(args[1]);

			if (selected == null && player != null && "current".equals(args[1]))
				selected = player;

			return this.parseVariables(arena, args[2], selected, args);
		}

		// arenaname_variable
		else if (args.length == 2) {
			final String param = args[1];

			if ("mobcount".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(arena.getAliveMonsters());
			}

			else if ("maxphase".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(arena.getSettings().getMaxPhase() == -1 ? "infinite" : arena.getSettings().getMaxPhase());
			}

			else if ("phase".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(arena.getPhase().getCurrent());
			}

			else if ("state".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(CoreUtil.getStateName(arena.getState()));
			}

			else if ("remaining".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				final ArenaState state = arena.getState();

				return state == ArenaState.LOBBY ? "lobby" : state == ArenaState.STOPPED ? "stopped" : arena.getRemainingSeconds() > 3600 ? "never" : TimeUtil.formatTimeShort(arena.getRemainingSeconds());
			}

			else if ("remaininglobby".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				final ArenaState state = arena.getState();

				return state == ArenaState.RUNNING ? "played" : state == ArenaState.STOPPED ? "stopped" : arena.getRemainingLobbySeconds() > 3600 ? "never" : TimeUtil.formatTimeShort(arena.getRemainingLobbySeconds());
			}

			else if ("alive".equals(param) || "player".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(arena.getPlayers().size());
			}

			else if ("lives".equals(param)) {
				if (arena == null)
					return "no arena " + arenaName;

				return String.valueOf(arena.getSettings().getLifes());
			}
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
			return selected.isDead() ? "dead" : MathUtil.formatTwoDigits(Remain.getHealth(selected));

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
