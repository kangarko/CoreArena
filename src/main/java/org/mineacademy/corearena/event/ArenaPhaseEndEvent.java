package org.mineacademy.corearena.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Triggered when a phase ends in the arena.
 */
@Getter
@RequiredArgsConstructor
public final class ArenaPhaseEndEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Arena arena;
	private final int phase;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
