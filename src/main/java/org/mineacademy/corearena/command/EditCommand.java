package org.mineacademy.corearena.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.ArenaPlaceholder;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.settings.Lang;

final class EditCommand extends AbstractCoreSubcommand {

	public EditCommand() {
		super("edit|e");

		this.setValidArguments(0, 1);
		this.setDescription("Begin or stop editing an arena.");
		this.setUsage("[arena]");

		this.addPlaceholder(new ArenaPlaceholder(0));
	}

	@Override
	protected void onCommand() {
		final Player player = this.getPlayer();
		String arenaName = this.args.length == 1 ? this.args[0] : "";

		Arena arena;

		if (arenaName.isEmpty()) {
			arena = this.getArenas().findArena(player.getLocation());

			if (arena == null)
				arena = this.getSetup().getEditedArena(player);

			this.checkNotNull(arena, Lang.component("arena-error-no-arena-at-location"));
			arenaName = arena.getName();

		} else {
			arena = this.getArena(arenaName);

			this.checkNotNull(arena, Lang.component("arena-error-not-found", "arena", arenaName));
			arenaName = arena.getName();
		}

		if (!this.canEditArena(arena))
			return;

		if (this.getSetup().isArenaEdited(arena)) {
			this.getSetup().removeEditedArena(arena);

			this.tellInfo(Lang.component("command-edit-disabled", "arena", arenaName));

		} else {
			this.getSetup().addEditedArena(player, arena);

			this.tellInfo(Lang.component("command-edit-enabled", "arena", arenaName));
		}
	}

	@Override
	public List<String> tabComplete() {
		final List<String> tab = new ArrayList<>();

		if (this.args.length == 1)
			if (this.isPlayer() && this.getSetup().getEditedArena(this.getPlayer()) != null)
				tab.add(this.getSetup().getEditedArena(this.getPlayer()).getName());
			else
				for (final String key : CoreArenaPlugin.getArenaManager().getArenasNames())
					if (key.toLowerCase().startsWith(this.args[0].toLowerCase()))
						tab.add(key);

		return tab;
	}
}