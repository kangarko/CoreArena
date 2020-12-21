package org.mineacademy.game.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.TabUtil;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;

public class TpCommand extends AbstractCoreSubcommand {

	public TpCommand() {
		super("tp");

		setDescription("Teleport to an arena's lobby.");
		setUsage("<arena>");
		setMinArguments(1);

		addPlaceholder(new PlaceholderArena(0));
	}

	@Override
	protected final void onCommand() {
		checkConsole();

		final Player player = getPlayer();
		final String arenaName = args[0];

		final Arena arena = getArenas().findArena(arenaName);
		checkNotNull(arena, Localization.Arena.Error.NOT_FOUND);
		checkNotNull(arena.getData().getLobby(), "&cThis arena lacks a lobby point. Use /" + getLabel() + " tools to set it.");

		player.teleport(arena.getData().getLobby().getLocation().clone().add(0, 1.1, 0));
		tell("Teleported to the lobby of " + arena.getName() + ".");
	}

	@Override
	public final List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (args.length == 1)
			return TabUtil.complete(args[0], CoreArenaPlugin.getArenaManager().getAvailable());

		return tab;
	}
}