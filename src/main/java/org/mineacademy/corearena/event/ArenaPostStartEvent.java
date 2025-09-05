package org.mineacademy.corearena.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Triggered when the arena starts after the lobby wait time has finished.
 */
@Getter
@RequiredArgsConstructor
public final class ArenaPostStartEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The arena
	 */
	private final Arena arena;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}