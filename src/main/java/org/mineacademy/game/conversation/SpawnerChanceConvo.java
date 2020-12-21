package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.mineacademy.game.menu.MenuMonsterSpawn;
import org.mineacademy.game.settings.Localization;

public final class SpawnerChanceConvo extends MenuNumberQuestion {

	public SpawnerChanceConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		final MenuMonsterSpawn menu = (MenuMonsterSpawn) getMenu();

		menu.getSpawnPoint().setChance(input.intValue());
		menu.saveSpawnerChanges();

		tell(context.getForWhom(), Localization.Conversation.SpawnerChance.SET.replace("{chance}", input.intValue() + ""));
	}

	@Override
	protected boolean isNumberValid(Number input) {
		return input.intValue() <= 100;
	}

	@Override
	protected String getQuestion() {
		return Localization.Conversation.SpawnerChance.HELP;
	}
}
