package org.mineacademy.corearena.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.ArenaPlaceholder;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.settings.Lang;

final class TpCommand extends AbstractCoreSubcommand {

	public TpCommand() {
		super("tp");

		this.setValidArguments(0, 1);
		this.setDescription("Teleport to an arena's lobby.");
		this.setUsage("[arena]");

		this.addPlaceholder(new ArenaPlaceholder(0));
	}

	@Override
	protected void onCommand() {
		this.checkConsole();

		final Player player = this.getPlayer();
		final String arenaName = this.args.length > 0 ? this.args[0] : "";
		Arena arena = null;

		if (arenaName.isEmpty()) {
			if (this.isPlayer())
				arena = this.findArenaWhenUnspecified(this.getPlayer());

			if (arena == null) {
				this.tellError(Lang.component("arena-error-no-arena-at-location"));

				this.returnTellAvailable(this.getSender());
			}

		} else
			arena = this.getArena(arenaName);

		if (arena == null) {
			this.tellError(Lang.component("arena-error-not-found", "arena", arenaName));

			this.returnTellAvailable(this.getSender());
		}

		this.checkNotNull(arena.getData().getLobby(), "&cThis arena lacks a lobby point. Use /" + this.getLabel() + " tools to set it.");

		player.teleport(arena.getData().getLobby().getLocation().clone().add(0.5, 1.1, 0.5));
		this.tellSuccess("Teleported to the lobby of " + arena.getName() + ".");
	}

	@Override
	public List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (this.args.length == 1)
			return this.completeLastWord(CoreArenaPlugin.getArenaManager().getArenasNames());

		return tab;
	}
}