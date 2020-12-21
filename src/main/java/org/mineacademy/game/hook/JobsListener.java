package org.mineacademy.game.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.corearena.CoreArenaPlugin;

import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gamingmesh.jobs.container.JobsPlayer;

public class JobsListener implements Listener {

	@EventHandler
	public final void onLevelUp(JobsLevelUpEvent e) {
		final JobsPlayer player = e.getPlayer();

		if (player != null && CoreArenaPlugin.getArenaManager().isPlaying(player.getPlayer()))
			e.setCancelled(true);
	}
}
