package org.mineacademy.corearena.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Triggered when a phase ends in the arena.
 *
 * Fired when transitioning to the next phase and when the arena stops mid-phase.
 */
@Getter
@RequiredArgsConstructor
public final class ArenaPhaseEndEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The arena
	 */
	private final Arena arena;

	/**
	 * The phase that just ended
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
