package org.mineacademy.corearena.command;

import java.util.List;

import org.mineacademy.corearena.command.placeholder.ArenaPlaceholder;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.fo.settings.Lang;

final class StopCommand extends AbstractCoreSubcommand {

	public StopCommand() {
		super("stop");

		this.setValidArguments(0, 1);
		this.setDescription("Stop a running arena.");
		this.setUsage("[arena]");

		this.addPlaceholder(new ArenaPlaceholder(0));
	}

	@Override
	protected void onCommand() {
		String arenaName = this.args.length > 0 ? this.args[0] : "";
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

		arenaName = arena.getName();

		this.checkBoolean(arena.getState() != ArenaState.STOPPED, Lang.component("command-stop-already-stopped", "arena", arenaName));
		arena.stopArena(StopCause.INTERRUPTED_COMMAND);
		this.tell(Lang.component("command-stop-success", "arena", arenaName));
	}

	@Override
	public List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWordArenaNames();

		return NO_COMPLETE;
	}
}