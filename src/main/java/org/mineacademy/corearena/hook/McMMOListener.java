package org.mineacademy.corearena.hook;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.corearena.CoreArenaPlugin;

import com.gmail.nossr50.events.experience.McMMOPlayerLevelDownEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.hardcore.McMMOPlayerDeathPenaltyEvent;
import com.gmail.nossr50.events.items.McMMOItemSpawnEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.alchemy.McMMOPlayerBrewEvent;

public class McMMOListener implements Listener {

	@EventHandler
	public final void mcmmo(McMMOPlayerAbilityActivateEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	@EventHandler
	public final void mcmmo(McMMOPlayerDeathPenaltyEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	@EventHandler
	public final void mcmmo(McMMOPlayerXpGainEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	@EventHandler
	public final void mcmmo(McMMOPlayerLevelUpEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	@EventHandler
	public final void mcmmo(McMMOPlayerLevelDownEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	@EventHandler
	public final void mcmmo(McMMOItemSpawnEvent e) {
		if (CoreArenaPlugin.getArenaManager().findArena(e.getLocation()) != null)
			e.setCancelled(true);
	}

	@EventHandler
	public final void mcmmo(McMMOPlayerBrewEvent e) {
		this.cancelIfPlaying(e.getPlayer(), e);
	}

	private final boolean cancelIfPlaying(Player pl, Cancellable e) {
		if (CoreArenaPlugin.getArenaManager().isPlaying(pl)) {
			e.setCancelled(true);

			return true;
		}

		return false;
	}
}
