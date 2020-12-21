package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.settings.Localization;

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
		tier.setLevelCost(input.intValue());
		clazz.addOrUpdateTier(tier);

		tell(context.getForWhom(), Localization.Conversation.Tier.PRICE_SET.replace("{amount}", Localization.Currency.format(input.intValue())));

		Common.runLater(1, new BukkitRunnable() {

			@Override
			public void run() {
				getMenu().displayTo((Player) context.getForWhom());
			}
		});
	}

	@Override
	protected String getQuestion() {
		return Localization.Conversation.Tier.HELP.replace("{class}", clazz.getName()).replace("{tier}", tier.getTier() + "");
	}
}
