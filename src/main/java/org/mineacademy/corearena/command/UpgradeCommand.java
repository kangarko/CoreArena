package org.mineacademy.corearena.command;

import org.mineacademy.corearena.menu.MenuRewards;

final class UpgradeCommand extends AbstractCoreSubcommand {

	public UpgradeCommand() {
		super("upgrade|u");

		this.setValidArguments(0, 0);
		this.setDescription("Open the class upgrade menu.");
	}

	@Override
	protected void onCommand() {
		this.checkConsole();

		if (!RewardsCommand.canAccessMenu(this.getPlayer()))
			return;

		MenuRewards.showUpgradeMenu(this.getPlayer());
	}
}
