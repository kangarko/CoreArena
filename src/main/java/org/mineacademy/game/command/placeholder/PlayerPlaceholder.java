package org.mineacademy.game.command.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Fixed placeholder replacing {player} with the player name
 */
public final class PlayerPlaceholder extends PositionPlaceholder {

	/**
	 * Create a new player placeholder getting player name
	 * at a certain args position, eg. args[1]
	 *
	 * @param position
	 */
	public PlayerPlaceholder(int position) {
		super("player", position);
	}

	@Override
	public String replace(String raw) {
		final Player player = Bukkit.getPlayer(raw);

		return player != null && player.isOnline() ? player.getName() : raw;
	}
}
