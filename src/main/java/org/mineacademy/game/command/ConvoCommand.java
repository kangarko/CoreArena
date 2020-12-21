package org.mineacademy.game.command;

import org.mineacademy.fo.Common;

public class ConvoCommand extends AbstractCoreSubcommand {

	public ConvoCommand() {
		super("conversation|c");

		setDescription("Reply to server's conversation manually.");
		setUsage("<message ...>");
		setMinArguments(1);
	}

	@Override
	protected final void onCommand() {
		checkBoolean(getPlayer().isConversing(), "&cYou must be conversing with the server!");

		getPlayer().acceptConversationInput(Common.joinRange(0, args));
	}
}