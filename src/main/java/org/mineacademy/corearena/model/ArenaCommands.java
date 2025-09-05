package org.mineacademy.corearena.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.fo.ProxyUtil;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.platform.Platform;

/**
 * A collections of commands that are run during a course of an arena.
 */
public final class ArenaCommands {

	/**
	 * Commands to be run per player, as a player.
	 */
	private final List<String> playerCommands;

	/**
	 * Commands to be run per player, as the console.
	 */
	private final List<String> playerConsoleCommands;

	/**
	 * Commands to be run once, as the console.
	 */
	private final List<String> consoleCommands;

	/**
	 * Create new arena commands
	 *
	 * @param playerCommands
	 * @param consoleCommands
	 * @param playerConsoleCommands
	 */
	public ArenaCommands(List<String> playerCommands, List<String> playerConsoleCommands, List<String> consoleCommands) {
		this.playerCommands = this.removeFirstSlash(playerCommands);
		this.playerConsoleCommands = this.removeFirstSlash(playerConsoleCommands);
		this.consoleCommands = this.removeFirstSlash(consoleCommands);
	}

	// Remove first / slash from the command list.
	private List<String> removeFirstSlash(List<String> commands) {
		final List<String> copy = new ArrayList<>();

		if (commands != null)
			commands.forEach(cmd -> {
				if (cmd.startsWith("/"))
					cmd = cmd.substring(1);

				copy.add(cmd);
			});

		return copy;
	}

	/**
	 * Run both {player commands and console commands
	 * for each online player in said arena.
	 *
	 * @param arena
	 * @param players the players to run commands for
	 * @param consoleForEach run console commands for each player as the console or from the console, once?
	 */
	public void run(Arena arena, Collection<Player> players, boolean consoleForEach) {
		this.runConsole(arena, players, consoleForEach);

		for (final Player player : players)
			this.runAsPlayer(arena, player);
	}

	/*
	 * Run {@link #consoleCommands} as the server operator.
	 */
	private void runConsole(Arena arena, Collection<Player> players, boolean consoleForEach) {
		for (final String command : this.consoleCommands) {
			final String coloredCommand = CompChatColor.translateColorCodes(arena.getMessenger().replaceVariables(command));

			if (consoleForEach)
				for (final Player player : players)
					Platform.dispatchConsoleCommand(null, coloredCommand.replace("{player}", player.getName()));

			else
				Platform.dispatchConsoleCommand(null, coloredCommand);
		}
	}

	/**
	 * Run player commands as the player, if any.
	 *
	 * @param arena
	 * @param player
	 */
	public void runAsPlayer(Arena arena, Player player) {
		for (final String cmd : this.playerCommands) {
			final String coloredCommand = CompChatColor.translateColorCodes(arena.getMessenger().replaceVariables(cmd.replace("{player}", player.getName())));

			if (cmd.startsWith("@tell "))
				arena.getMessenger().tell(player, coloredCommand.replaceFirst("@tell ", ""));

			else if (cmd.startsWith("@connect "))
				ProxyUtil.sendBungeeMessage(player, "Connect", coloredCommand.replaceFirst("@connect ", ""));

			else
				Platform.toPlayer(player).dispatchCommand(coloredCommand);
		}

		for (final String command : this.playerConsoleCommands) {
			final String coloredCommand = CompChatColor.translateColorCodes(arena.getMessenger().replaceVariables(command));

			if (command.startsWith("@tell "))
				arena.getMessenger().tell(player, coloredCommand.replaceFirst("@tell ", ""));

			else if (command.startsWith("@connect "))
				ProxyUtil.sendBungeeMessage(player, "Connect", coloredCommand.replaceFirst("@connect ", ""));

			else
				Platform.dispatchConsoleCommand(Platform.toPlayer(player), coloredCommand);
		}
	}

	/**
	 * Run console commands as the server console, translates \@tell and \@connect variables
	 *
	 * @param arena the arena
	 * @param player the sender
	 */
	public void runAsConsole(Arena arena, Player player) {
		for (final String command : this.consoleCommands) {
			final String coloredCommand = CompChatColor.translateColorCodes(arena.getMessenger().replaceVariables(command.replace("{player}", player.getName())));

			if (command.startsWith("@tell "))
				arena.getMessenger().tell(player, coloredCommand.replaceFirst("@tell ", ""));

			else if (command.startsWith("@connect "))
				ProxyUtil.sendBungeeMessage(player, "Connect", coloredCommand.replaceFirst("@connect ", ""));

			else
				Platform.dispatchConsoleCommand(null, coloredCommand);
		}
	}
}
