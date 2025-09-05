package org.mineacademy.corearena.command;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.menu.MenuRewards;
import org.mineacademy.corearena.menu.MenuRewards.MenuMode;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.settings.Lang;

final class RewardsCommand extends AbstractCoreSubcommand {

	public RewardsCommand() {
		super("rewards|r");

		this.setValidArguments(0, 0);
		this.setDescription("Open the rewards menu.");
	}

	@Override
	protected void onCommand() {
		if (this.args.length != 0)
			this.returnTell(Lang.legacy("part-usage") + "/" + this.getLabel() + " " + this.getSublabel());

		if (!canAccessMenu(this.getPlayer()))
			return;

		MenuRewards.showRewardsMenu(this.getPlayer(), MenuMode.PURCHASE);
	}

	public static boolean canAccessMenu(Player player) throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

		if (data.hasArenaCache())
			Messenger.error(player, Lang.component("menu-cannot-open-in-arena"));

		return true;
	}
}