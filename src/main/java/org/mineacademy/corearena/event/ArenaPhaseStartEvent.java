package org.mineacademy.corearena.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Triggered when a new phase starts in the arena.
 *
 * Fired for the initial phase (phase 1) and every subsequent phase transition.
 */
@Getter
@RequiredArgsConstructor
public final class ArenaPhaseStartEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The arena
	 */
	private final Arena arena;

	/**
	 * The phase that just started
	 */
	private final int phase;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
