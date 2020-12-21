package org.mineacademy.game.command;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.settings.Localization;

public class ListCommand extends AbstractCoreSubcommand {

	public ListCommand() {
		super("list");

		setDescription("Browse available arenas.");
	}

	@Override
	protected final void onCommand() {
		CoreArenaPlugin.getArenaManager().tellAvailableArenas(sender);

		if (sender instanceof Player)
			tell(Localization.Commands.Join.SUGGEST);
	}

	@Override
	protected String[] getMultilineUsageMessage() {
		return new String[] {
				"&fjoin &6<arena> &e- Join an arena.",
		};
	}
}