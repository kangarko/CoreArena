package org.mineacademy.game.impl.arena;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.mineacademy.game.exception.EventHandledException;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.DeathCause;
import org.mineacademy.game.type.LeaveCause;
import org.mineacademy.game.type.StopCause;

/**
 * An arena where the last dick standing wins.
 */
public final class MobArena extends FeatureArena {

	public MobArena(String name) {
		super(name);
	}

	@Override
	public void onPlayerDeath(Player pl, Player killer) throws EventHandledException {
		super.onPlayerDeath(pl, killer);

		kickPlayer(pl, LeaveCause.KILLED);

		getMessenger().broadcastExcept(pl, pl, Localization.Arena.Game.KICK_KILL_BROADCAST.replace("{killer}", killer.getName()));
		getMessenger().tell(pl, Localization.Arena.Game.KICK_KILL_TO_VICTIM.replace("{killer}", killer.getName()));
	}

	@Override
	public void onPlayerDeath(Player pl, DeathCause cause) throws EventHandledException {
		super.onPlayerDeath(pl, cause);

		kickPlayer(pl, LeaveCause.KILLED);
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
