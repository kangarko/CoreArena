package org.mineacademy.game.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.BoxedMessage;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaMessenger;
import org.mineacademy.game.type.MessengerTarget;
import org.mineacademy.game.util.CoreUtil;

import lombok.Getter;
import lombok.Setter;

public final class SimpleMessenger implements ArenaMessenger {

	@Getter
	@Setter
	private MessengerTarget target = MessengerTarget.ARENA;

	private final Arena arena;

	public SimpleMessenger(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Only tells the directed players, with the player and other variables
	 */
	@Override
	public void tell(Player player, String message) {
		Common.tell(player, replaceVariables(message.replace("{player}", player.getName())));
	}

	public void broadcastAndLog(String message) {
		broadcast0(getRecipients(), message, true, false);
	}

	public void broadcastAndLogFramed(String message) {
		broadcast0(getRecipients(), message, true, true);
	}

	/**
	 * Tells all players in the arena, with the player and other variables
	 */
	/*public final void broadcast(Player player, String message) {
		broadcast(message.replace("{player}", player.getName()));
	}*/

	public void broadcastExcept(Player player, Player exception, String message) {
		final List<Player> receivers = new ArrayList<>(getRecipients());
		receivers.remove(exception);

		broadcast(receivers, message.replace("{player}", player.getName()));
	}

	/**
	 * Tells all players in the arena, replaces variables
	 */
	@Override
	public void broadcast(String message) {
		broadcast0(getRecipients(), message, false, false);
	}

	public void broadcastFramed(String message) {
		broadcast0(getRecipients(), message, false, true);
	}

	public void broadcast(Iterable<? extends CommandSender> toWhom, String message) {
		broadcast0(toWhom, message, false, false);
	}

	private void broadcast0(Iterable<? extends CommandSender> toWhom, String message, boolean log, boolean frame) {
		message = replaceVariables(message);

		if (frame)
			BoxedMessage.tell(toWhom, message);

		else
			for (final CommandSender sender : toWhom)
				Common.tell(sender, message);

		if (log)
			Common.log(message);
	}

	@Override
	public void broadcastBar(String message) {
		message = replaceVariables(message);

		for (final Player player : getRecipients())
			Remain.sendActionBar(player, message);
	}

	@Override
	public void playSound(CompSound sound, float pitch) {
		for (final Player player : getRecipients())
			playSound(player, sound, pitch);
	}

	@Override
	public void playSound(Player player, CompSound sound, float pitch) {
		player.playSound(player.getLocation(), sound.getSound(), 1, pitch);
	}

	public void playSound(SimpleSound sound) {
		for (final Player player : getRecipients())
			playSound(player, sound);
	}

	public void playSound(Player player, SimpleSound sound) {
		sound.play(player);
	}

	@Override
	public String replaceVariables(String message) {
		return message
				.replace("{arena}", arena.getName())
				.replace("{remainingTime}", CoreUtil.formatTime(arena.getRemainingSeconds()))
				.replace("{state}", CoreUtil.getStateName(arena.getState()))
				.replace("{phase}", arena.getPhase().getCurrent() + "")
				.replace("{players}", getRecipients().size() + "")
				.replace("{maxPlayers}", arena.getSettings().getMaximumPlayers() + "")
				.replace("{minPlayers}", arena.getSettings().getMinimumPlayers() + "");
	}

	private Collection<? extends Player> getRecipients() {
		switch (target) {
			case ARENA:
				return arena.getPlayers();

			case WORLD:
				return arena.getData().getRegion().getCenter().getWorld().getPlayers();

			case SERVER:
				return Remain.getOnlinePlayers();

			default:
				throw new FoException("Unhandled target " + target);
		}
	}
}
