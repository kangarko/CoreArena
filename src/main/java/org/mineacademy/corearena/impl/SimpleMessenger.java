package org.mineacademy.corearena.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaMessenger;
import org.mineacademy.corearena.type.MessengerTarget;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

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
		Common.tell(player, this.replaceVariables(message.replace("{player}", player.getName())));
	}

	@Override
	public void tell(Player player, SimpleComponent message) {
		Common.tell(player, this.replaceVariables(message.replaceBracket("player", player.getName())));
	}

	public void broadcastAndLog(String message) {
		this.broadcast0(this.getRecipients(), message, true, false);
	}

	public void broadcastAndLogFramed(SimpleComponent message) {
		this.broadcast0(this.getRecipients(), message, true, true);
	}

	public void broadcastAndLogFramed(String message) {
		this.broadcast0(this.getRecipients(), message, true, true);
	}

	/**
	 * Tells all players in the arena, with the player and other variables
	 * @param player
	 * @param exception
	 * @param message
	 */
	public void broadcastExcept(Player player, Player exception, String message) {
		final List<Player> receivers = new ArrayList<>(this.getRecipients());
		receivers.remove(exception);

		this.broadcast(receivers, message.replace("{player}", player.getName()));
	}

	/**
	 * Tells all players in the arena, with the player and other variables
	 * @param player
	 * @param exception
	 * @param message
	 */
	public void broadcastExcept(Player player, Player exception, SimpleComponent message) {
		final List<Player> receivers = new ArrayList<>(this.getRecipients());
		receivers.remove(exception);

		this.broadcast(receivers, message.replaceBracket("player", player.getName()));
	}

	/**
	 * Tells all players in the arena, replaces variables
	 */
	@Override
	public void broadcast(String message) {
		this.broadcast0(this.getRecipients(), message, false, false);
	}

	@Override
	public void broadcast(SimpleComponent message) {
		this.broadcast0(this.getRecipients(), message, false, false);
	}

	public void broadcastFramed(String message) {
		this.broadcast0(this.getRecipients(), message, false, true);
	}

	public void broadcast(Iterable<? extends CommandSender> toWhom, String message) {
		this.broadcast0(toWhom, message, false, false);
	}

	public void broadcast(Iterable<? extends CommandSender> toWhom, SimpleComponent message) {
		this.broadcast0(toWhom, message, false, false);
	}

	private void broadcast0(Iterable<? extends CommandSender> toWhom, String message, boolean log, boolean frame) {
		message = this.replaceVariables(message);

		if (frame)
			for (final CommandSender sender : toWhom)
				Common.tellBoxed(sender, message);

		else
			for (final CommandSender sender : toWhom)
				Common.tell(sender, message);

		if (log)
			Common.log(message);
	}

	private void broadcast0(Iterable<? extends CommandSender> toWhom, SimpleComponent message, boolean log, boolean frame) {
		message = this.replaceVariables(message);

		if (frame)
			for (final CommandSender sender : toWhom)
				Common.tellBoxed(sender, message);

		else
			for (final CommandSender sender : toWhom)
				Common.tell(sender, message);

		if (log)
			Common.log(message.toLegacySection());
	}

	@Override
	public void broadcastBar(String message) {
		message = this.replaceVariables(message);

		for (final Player player : this.getRecipients())
			Platform.toPlayer(player).sendActionBar(message);
	}

	@Override
	public void playSound(CompSound sound, float pitch) {
		for (final Player player : this.getRecipients())
			this.playSound(player, sound, pitch);
	}

	@Override
	public void playSound(Player player, CompSound sound, float pitch) {
		player.playSound(player.getLocation(), sound.getSound(), 1, pitch);
	}

	public void playSound(SimpleSound sound) {
		for (final Player player : this.getRecipients())
			this.playSound(player, sound);
	}

	public void playSound(Player player, SimpleSound sound) {
		sound.play(player);
	}

	@Override
	public String replaceVariables(String message) {
		return message
				.replace("{arena}", this.arena.getName())
				.replace("{remainingTime}", TimeUtil.formatTimeGeneric(this.arena.getRemainingSeconds()))
				.replace("{state}", CoreUtil.getStateName(this.arena.getState()))
				.replace("{phase}", this.arena.getPhase().getCurrent() + "")
				.replace("{players}", this.getRecipients().size() + "")
				.replace("{maxPlayers}", this.arena.getSettings().getMaximumPlayers() + "")
				.replace("{minPlayers}", this.arena.getSettings().getMinimumPlayers() + "");
	}

	private SimpleComponent replaceVariables(SimpleComponent message) {
		return message
				.replaceBracket("arena", this.arena.getName())
				.replaceBracket("remainingTime", TimeUtil.formatTimeGeneric(this.arena.getRemainingSeconds()))
				.replaceBracket("state", CoreUtil.getStateName(this.arena.getState()))
				.replaceBracket("phase", this.arena.getPhase().getCurrent() + "")
				.replaceBracket("players", this.getRecipients().size() + "")
				.replaceBracket("maxPlayers", this.arena.getSettings().getMaximumPlayers() + "")
				.replaceBracket("minPlayers", this.arena.getSettings().getMinimumPlayers() + "");
	}

	private Collection<? extends Player> getRecipients() {
		switch (this.target) {
			case ARENA:
				return this.arena.getPlayers();

			case WORLD:
				return this.arena.getData().getRegion().getCenter().getWorld().getPlayers();

			case SERVER:
				return Remain.getOnlinePlayers();

			default:
				throw new FoException("Unhandled target " + this.target);
		}
	}
}
