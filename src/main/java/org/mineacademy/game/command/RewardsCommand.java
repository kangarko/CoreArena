package org.mineacademy.game.command;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.menu.MenuRewards;
import org.mineacademy.game.menu.MenuRewards.MenuMode;
import org.mineacademy.game.settings.Localization;

public class RewardsCommand extends AbstractCoreSubcommand {

	public RewardsCommand() {
		super("rewards|r");

		setDescription("Open the rewards menu.");
	}

	@Override
	protected final void onCommand() {
		if (args.length != 0)
			returnTell(Localization.Parts.USAGE + "/" + getLabel() + " " + getSublabel());

		if (!canAccessMenu(getPlayer()))
			return;

		MenuRewards.showRewardsMenu(getPlayer(), MenuMode.PURCHASE);
	}

	public static final boolean canAccessMenu(Player player) throws CommandException {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

		if (data.hasArenaCache())
			Common.tell(player, Localization.Menu.CANNOT_OPEN_IN_ARENA);

		return true;
	}
}