package org.mineacademy.corearena.command;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.settings.Lang;

final class ListCommand extends AbstractCoreSubcommand {

	public ListCommand() {
		super("list|ls");

		this.setValidArguments(0, 0);
		this.setDescription("Browse available arenas.");
	}

	@Override
	protected void onCommand() {
		CoreArenaPlugin.getArenaManager().tellAvailableArenas(this.getSender());

		if (this.isPlayer())
			this.tell(Lang.component("command-join-suggest"));
	}

	@Override
	protected String[] getMultilineUsageMessage() {
		return new String[] {
				"&fjoin &6<arena> &e- Join an arena.",
		};
	}
}