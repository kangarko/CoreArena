package org.mineacademy.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mineacademy.game.model.ArenaPlugin;
import org.mineacademy.game.type.MenuType;

import lombok.Getter;
import lombok.Setter;

/**
 * An event indicating to a third party plugin that they should
 * open a class menu.
 */
@Getter
public final class PluginShouldFireMenuEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * What plugin
	 */
	private final ArenaPlugin plugin;

	/**
	 * What menu to open
	 */
	private final MenuType type;

	/**
	 * The player to whom to open
	 */
	private final Player player;

	/**
	 * The data to be put into the menu class when initializing
	 */
	private final Object[] data;

	public PluginShouldFireMenuEvent(ArenaPlugin plugin, MenuType type, Player player, Object... data) {
		this.plugin = plugin;
		this.type = type;
		this.player = player;
		this.data = data;
	}

	/**
	 * Should we prevent to display this menu?
	 */
	@Setter
	private boolean cancelled;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}