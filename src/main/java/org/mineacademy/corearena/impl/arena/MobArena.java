package org.mineacademy.corearena.impl.arena;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.mineacademy.corearena.exception.EventHandledException;
import org.mineacademy.corearena.type.DeathCause;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.fo.settings.Lang;

/**
 * An arena where the last dick standing wins.
 */
public final class MobArena extends FeatureArena {

	public MobArena(String name) {
		super(name);
	}

	@Override
	public void onPlayerDeath(Player player, Player killer) throws EventHandledException {
		super.onPlayerDeath(player, killer);

		this.kickPlayer(player, LeaveCause.KILLED);

		this.getMessenger().broadcastExcept(player, player, Lang.component("arena-game-kick-kill-broadcast", "killer", killer.getName()));
		this.getMessenger().tell(player, Lang.component("arena-game-kick-kill-to-victim", "killer", killer.getName()));
	}

	@Override
	public void onPlayerDeath(Player pl, DeathCause cause) throws EventHandledException {
		super.onPlayerDeath(pl, cause);

		this.kickPlayer(pl, LeaveCause.KILLED);
	}

	@Override
	public void onPlayerDamage(EntityDamageByEntityEvent e, Player player, Entity source, double damage) {
	}

	@Override
	public void onPlayerBlockDamage(EntityDamageByBlockEvent e, Player player, double damage) {
	}

	@Override
	public void onProjectileHit(ProjectileHitEvent e) {
	}

	@Override
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
	}

	@Override
	protected void handleArenaPostStop(StopCause cause) {
	}
}
