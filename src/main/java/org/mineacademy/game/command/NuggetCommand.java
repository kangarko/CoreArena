package org.mineacademy.game.command;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.game.command.placeholder.ForwardingPlaceholder;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.settings.Localization;

public class NuggetCommand extends AbstractCoreSubcommand {

	public NuggetCommand() {
		super("nugget|n");

		setDescription("Manage the Nugget currency.");
		setUsage("<player> [set/give/take] [amount]");
		setMinArguments(1);

		addPlaceholder(new ForwardingPlaceholder("player", 0));
	}

	@Override
	protected final void onCommand() {
		final OfflinePlayer other = Bukkit.getOfflinePlayer(args[0]);
		checkBoolean(other != null && other.hasPlayedBefore(), Localization.Player.NEVER_PLAYED);

		final ArenaPlayer data = other != null ? CoreArenaPlugin.getDataFor(other) : CoreArenaPlugin.getDataFor(getPlayer());

		setArg(0, data.getPlayerName());

		if (args.length < 2)
			returnTell(formatCurrencyMessage(other == null ? Localization.Commands.Nuggets.BALANCE : Localization.Commands.Nuggets.BALANCE_OTHER, data.getNuggets()));

		checkArgs(3, Localization.Parts.USAGE + "/" + getLabel() + " " + getSublabel() + " " + args[0] + " <set/give/take> <" + Localization.Parts.AMOUNT + ">");

		if (!canAccessMenu())
			return;

		final String param = args[1].toLowerCase();

		Integer amount = null;

		try {
			amount = Integer.parseInt(args[2]);

		} catch (final NumberFormatException ex) {
			returnTell(Localization.Commands.Nuggets.INVALID_AMOUNT);
		}

		Mode mode = null;

		try {
			mode = Mode.valueOf(param.toUpperCase());

		} catch (final Throwable t) {
			returnTell(Localization.Commands.Nuggets.INVALID_PARAM.replace("{available}", StringUtils.join(Mode.values(), ", ").toLowerCase()));
		}

		if (mode == Mode.SET) {
			data.setNuggets(amount);

			returnTell(formatManipulationMessage(Localization.Commands.Nuggets.SET, amount, data.getNuggets()));

		} else if (mode == Mode.GIVE) {
			data.setNuggets(data.getNuggets() + amount);

			returnTell(formatManipulationMessage(Localization.Commands.Nuggets.GAVE, amount, data.getNuggets()));

		} else if (mode == Mode.TAKE) {
			data.setNuggets(MathUtil.range(data.getNuggets() - amount, 0, Integer.MAX_VALUE));

			returnTell(formatManipulationMessage(Localization.Commands.Nuggets.TOOK, amount, data.getNuggets()));
		}

		throw new FoException("Mode " + mode + " not yet implemented! Please notify plugins developers.");
	}

	enum Mode {
		SET,
		TAKE,
		GIVE
	}

	private String formatManipulationMessage(String key, int manipulated, int balance) {
		return formatCurrencyMessage(key, balance).replace("{amount}", Localization.Currency.format(manipulated));
	}

	private String formatCurrencyMessage(String key, int nuggets) {
		return key.replace("{balance}", Localization.Currency.format(nuggets));
	}

	private boolean canAccessMenu() throws CommandException {
		if (sender instanceof Player) {
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(sender);

			if (data.hasArenaCache())
				returnTell(Localization.Commands.DISALLOWED_WHILE_PLAYING);
		}

		return true;
	}

	@Override
	public List<String> tabComplete() {

		if (args.length == 1)
			return completeLastWordPlayerNames();

		if (args.length == 2)
			return completeLastWord("set", "give", "take");

		if (args.length == 3)
			return completeLastWord("0", "1", "30");

		return null;
	}
}