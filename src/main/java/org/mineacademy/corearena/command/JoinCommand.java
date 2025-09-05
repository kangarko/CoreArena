package org.mineacademy.corearena.command;

import java.util.List;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.ArenaPlaceholder;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.JoinCause;
import org.mineacademy.fo.settings.Lang;

final class JoinCommand extends AbstractCoreSubcommand {

	public JoinCommand() {
		super("join|j");

		this.setValidArguments(0, 1);
		this.setDescription("Join an existing arena.");
		this.setUsage("[arena]");

		this.addPlaceholder(new ArenaPlaceholder(0));
	}

	@Override
	protected void onCommand() {
		final String arenaName = this.args.length > 0 ? this.args[0] : null;
		Arena arena = arenaName != null ? this.getArena(arenaName) : this.getArenas().findArena(this.getPlayer().getLocation());

		if (arena == null && this.getArenas().getArenas().size() == 1)
			arena = this.getArenas().getArenas().iterator().next();

		if (arena == null) {

			if (this.args.length > 0)
				this.tellError(Lang.component("arena-error-not-found", "arena", arenaName));
			else
				this.tellError(Lang.component("arena-error-no-arena-at-location"));

			CoreArenaPlugin.getArenaManager().tellAvailableArenas(this.getSender());
			return;
		}

		arena.joinPlayer(this.getPlayer(), JoinCause.COMMAND);
	}

	@Override
	public List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWordArenaNames();

		return NO_COMPLETE;
	}
}