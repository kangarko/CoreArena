package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.model.Reward;
import org.mineacademy.game.settings.Localization;

public final class RewardCostConvo extends MenuNumberQuestion {

	private final Reward reward;

	public RewardCostConvo(Menu menu, Reward reward) {
		super(menu);

		this.reward = reward;
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		reward.setCost(input.intValue());

		CoreArenaPlugin.getRewadsManager().updateReward(reward);
		tell(context.getForWhom(), Localization.Conversation.Reward.PRICE_SET.replace("{amount}", Localization.Currency.format(input.intValue())));

		Common.runLater(1, new BukkitRunnable() {

			@Override
			public void run() {
				getMenu().displayTo((Player) context.getForWhom());
			}
		});
	}

	@Override
	protected String getQuestion() {
		return Localization.Conversation.Reward.HELP.replace("{material}", ItemUtil.bountifyCapitalized(reward.getItem().getType()));
	}
}
