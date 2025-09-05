package org.mineacademy.corearena.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.Setter;

/**
 * Triggered when the player is teleported back to spawn.
 */
@Getter
@Setter
public final class SpawnTeleportEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The player
	 */
	private final Player player;

	/**
	 * The location
	 */
	private Location location;

	/**
	 * Should we cancel the teleport?
	 */
	private boolean cancelled;

	public SpawnTeleportEvent(Player player, Location location) {
		this.player = player;
		this.location = location;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}