package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

public final class TierCostConvo extends MenuNumberQuestion {

	private final ArenaClass clazz;
	private final ClassTier tier;

	public TierCostConvo(Menu menu, ArenaClass clazz, ClassTier tier) {
		super(menu);

		this.clazz = clazz;
		this.tier = tier;
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		this.tier.setLevelCost(input.intValue());
		this.clazz.addOrUpdateTier(this.tier);

		tell(context.getForWhom(), Lang.component("conversation-tier-price-set", "amount", Lang.numberFormat("currency-name", input.intValue())));

		Platform.runTask(1, new SimpleRunnable() {

			@Override
			public void run() {
				TierCostConvo.this.getMenu().displayTo((Player) context.getForWhom());
			}
		});
	}

	@Override
	protected String getQuestion() {
		return Lang.legacy("conversation-tier-help", "class", this.clazz.getName(), "tier", this.tier.getTier() + "");
	}
}
