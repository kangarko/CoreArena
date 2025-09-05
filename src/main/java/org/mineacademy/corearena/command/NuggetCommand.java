package org.mineacademy.corearena.command;

import java.util.List;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.ForwardingPlaceholder;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.Lang;

final class NuggetCommand extends AbstractCoreSubcommand {

	public NuggetCommand() {
		super("nugget|n");

		this.setValidArguments(0, 5);
		this.setDescription("Manage Nuggets for players.");
		this.setUsage("[player] or <player> [set/give/take] [amount]");

		this.addPlaceholder(new ForwardingPlaceholder("player", 0));
	}

	@Override
	protected void onCommand() {
		String playerName = null;

		if (this.args.length == 0) {
			if (this.isPlayer())
				playerName = this.getPlayer().getName();
			else
				returnTell("Please specify the player name.");
		} else
			playerName = this.args[0];

		this.findOfflinePlayer(playerName, other -> {
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(other);

			if (this.args.length < 2) {
				this.tellInfo(Lang.component("command-nuggets-balance", "player", other.getName(), "balance", Lang.numberFormat("currency-name", data.getNuggets())));

				return;
			}

			if (!this.canAccessMenu())
				return;

			final Param mode = this.findEnum(Param.class, this.args[1]);

			this.checkArgs(3, "Please specify the Nugget amount.");
			final int amount = this.findInt(2, Lang.component("command-nuggets-invalid-amount"));

			if (mode == Param.SET) {
				data.setNuggets(amount);

				this.tellSuccess(this.formatManipulationMessage(Lang.component("command-nuggets-set"), data.getPlayerName(), amount, data.getNuggets()));

			} else if (mode == Param.GIVE) {
				data.setNuggets(data.getNuggets() + amount);

				this.tellSuccess(this.formatManipulationMessage(Lang.component("command-nuggets-give"), data.getPlayerName(), amount, data.getNuggets()));

			} else if (mode == Param.TAKE) {
				data.setNuggets(MathUtil.range(data.getNuggets() - amount, 0, Integer.MAX_VALUE));

				this.tellSuccess(this.formatManipulationMessage(Lang.component("command-nuggets-take"), data.getPlayerName(), amount, data.getNuggets()));
			}
		});
	}

	enum Param {
		SET,
		TAKE,
		GIVE
	}

	private SimpleComponent formatManipulationMessage(SimpleComponent message, String playerName, int nuggets, int balance) {
		return this.formatCurrencyMessage(message, balance)
				.replaceBracket("player", playerName)
				.replaceBracket("amount", Lang.numberFormat("currency-name", nuggets));
	}

	private SimpleComponent formatCurrencyMessage(SimpleComponent message, int nuggets) {
		return message.replaceBracket("balance", Lang.numberFormat("currency-name", nuggets));
	}

	private boolean canAccessMenu() throws CommandException {
		if (this.isPlayer()) {
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(this.getSender());

			if (data.hasArenaCache())
				this.returnTell(Lang.component("command-disallowed-while-playing"));
		}

		return true;
	}

	@Override
	public List<String> tabComplete() {

		if (this.args.length == 1)
			return this.completeLastWordPlayerNames();

		if (this.args.length == 2)
			return this.completeLastWord("set", "give", "take");

		if (this.args.length == 3)
			return this.completeLastWord("0", "1", "30");

		return null;
	}
}