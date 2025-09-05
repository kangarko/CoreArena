package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Reward;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

public final class RewardCostConvo extends MenuNumberQuestion {

	private final Reward reward;

	public RewardCostConvo(Menu menu, Reward reward) {
		super(menu);

		this.reward = reward;
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		this.reward.setCost(input.intValue());

		CoreArenaPlugin.getRewadsManager().updateReward(this.reward);
		tell(context.getForWhom(), Lang.component("conversation-reward-price-set", "amount", Lang.numberFormat("currency-name", input.intValue())));

		Platform.runTask(1, new SimpleRunnable() {

			@Override
			public void run() {
				RewardCostConvo.this.getMenu().displayTo((Player) context.getForWhom());
			}
		});
	}

	@Override
	protected String getQuestion() {
		return Lang.legacy("conversation-reward-help", "material", ChatUtil.capitalizeFully(this.reward.getItem().getType()));
	}
}
