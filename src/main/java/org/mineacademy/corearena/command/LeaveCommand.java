package org.mineacademy.corearena.command;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.fo.settings.Lang;

final class LeaveCommand extends AbstractCoreSubcommand {

	public LeaveCommand() {
		super("leave|l");

		this.setValidArguments(0, 0);
		this.setDescription("Leave arena while playing.");
	}

	@Override
	protected void onCommand() {
		final Arena arena = this.getArenas().findArena(this.getPlayer());
		this.checkNotNull(arena, Lang.component("command-leave-not-playing"));

		final boolean left = arena.kickPlayer(this.getPlayer(), LeaveCause.COMMAND);

		if (left)
			this.tell(Lang.component("command-leave-success", "arena", arena.getName()));
	}
}