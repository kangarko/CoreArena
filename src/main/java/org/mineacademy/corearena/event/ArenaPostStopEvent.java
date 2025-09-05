package org.mineacademy.corearena.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.StopCause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Triggered after the arena stopped for any reason (stop command, reload, error, or naturally)
 */
@Getter
@RequiredArgsConstructor
public final class ArenaPostStopEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * The arena
	 */
	private final Arena arena;

	/**
	 * Why is the arena stopping
	 */
	private final StopCause cause;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}