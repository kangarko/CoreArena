package org.mineacademy.corearena.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.JoinCause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Triggered when after the player has joined an arena.
 */
@RequiredArgsConstructor
@Getter
public final class ArenaJoinEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The arena
	 */
	private final Arena arena;

	/**
	 * Why the player has joined
	 */
	private final JoinCause cause;

	/**
	 * The player
	 */
	private final Player player;

	/**
	 * Shall other players in the lobby be informed about this player joining?
	 */
	@Setter
	private boolean silent = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}