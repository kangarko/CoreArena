package org.mineacademy.corearena.task;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.remain.Remain;

public final class ArenaEscapeTask extends SimpleRunnable {

	@Override
	public void run() {
		for (final Player player : Remain.getOnlinePlayers()) {
			final Arena playerArena = CoreArenaPlugin.getArenaManager().findArena(player);

			if (playerArena != null && playerArena.getState() != ArenaState.STOPPED) {
				final Arena locationArena = CoreArenaPlugin.getArenaManager().findArena(player.getLocation());
				final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

				if (locationArena == null || !locationArena.equals(playerArena))
					if (!data.getArenaCache().pendingRemoval && !data.getArenaCache().pendingJoining) {

						if (playerArena.getState() == ArenaState.LOBBY)
							Common.warning("Kicking player in lobby of " + playerArena.getName() + " for being outside of its region - check if lobby players can't escape easily!");

						playerArena.kickPlayer(player, LeaveCause.ESCAPED);
					}
			}
		}
	}
}
