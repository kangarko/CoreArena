package org.mineacademy.corearena.command;

import java.util.List;

import org.mineacademy.corearena.command.placeholder.ArenaPlaceholder;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.fo.settings.Lang;

final class StartCommand extends AbstractCoreSubcommand {

	public StartCommand() {
		super("start");

		this.setValidArguments(0, 1);
		this.setDescription("Start a pending arena.");
		this.setUsage("[arena]");

		this.addPlaceholder(new ArenaPlaceholder(0));
	}

	@Override
	protected void onCommand() {
		String arenaName = this.args.length > 0 ? this.args[0] : "";
		Arena arena = null;

		if (arenaName.isEmpty()) {
			if (this.isPlayer())
				arena = findArenaWhenUnspecified(this.getPlayer());

			if (arena == null) {
				this.tellError(Lang.component("arena-error-no-arena-at-location"));

				returnTellAvailable(this.getSender());
			}

		} else
			arena = this.getArena(arenaName);

		if (arena == null) {
			this.tellError(Lang.component("arena-error-not-found", "arena", arenaName));

			returnTellAvailable(this.getSender());
		}

		arenaName = arena.getName();

		if (arena.getState() == ArenaState.STOPPED) {
			arena.startLobby();
			this.tellSuccess(Lang.component("command-start-success", "arena", arenaName));

		} else if (arena.getState() == ArenaState.LOBBY)
			this.tellInfo(arena.startArena(true) ? Lang.component("command-start-success", "arena", arena.getName()) : Lang.component("command-start-fail", "arena", arena.getName()));

		else
			this.tellError(Lang.component("arena-error-already-running", "arena", arena.getName()));
	}

	@Override
	public List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWordArenaNames();

		return NO_COMPLETE;
	}
}