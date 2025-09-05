package org.mineacademy.corearena.command;

import java.util.List;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.settings.Lang;

final class RemoveCommand extends AbstractCoreSubcommand {

	public RemoveCommand() {
		super("remove|rm|delete");

		this.setValidArguments(0, 1);
		this.setDescription("Delete an arena.");
		this.setUsage("[arena]");
	}

	@Override
	protected void onCommand() {
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

		CoreArenaPlugin.getArenaManager().removeArena(arena.getName());
		this.tellSuccess("Arena '" + arena.getName() + "' has been deleted.");
	}

	@Override
	public List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWordArenaNames();

		return NO_COMPLETE;
	}
}