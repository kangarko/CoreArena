package org.mineacademy.corearena.command;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.settings.Lang;

final class ToggleCommand extends AbstractCoreSubcommand {

	public ToggleCommand() {
		super("toggle|tg");

		this.setValidArguments(0, 1);
		this.setDescription("Enable or disable an arena");
		this.setUsage("[arena]");
	}

	@Override
	protected void onCommand() {
		final Player player = this.getPlayer();
		final String arenaName = this.args.length == 1 ? this.args[0] : "";

		Arena arena;

		if (arenaName.isEmpty()) {
			arena = this.getArenas().findArena(player.getLocation());

			if (arena == null)
				arena = this.getSetup().getEditedArena(player);

			this.checkNotNull(arena, Lang.component("arena-error-no-arena-at-location"));

		} else {
			arena = this.getArena(arenaName);
			this.checkNotNull(arena, Lang.component("arena-error-not-found", "arena", arenaName));
		}

		if (!this.canEditArena(arena))
			return;

		arena.setEnabled(!arena.isEnabled());

		this.tellSuccess(arena.isEnabled() ? Lang.component("command-change-state-enabled", "name", arena.getName()) : Lang.component("command-change-state-disabled", "name", arena.getName()));
	}
}
