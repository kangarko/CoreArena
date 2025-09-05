package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.ConversationContext;
import org.mineacademy.corearena.menu.MenuMonsterSpawn;
import org.mineacademy.fo.settings.Lang;

public final class SpawnerChanceConvo extends MenuNumberQuestion {

	public SpawnerChanceConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		final MenuMonsterSpawn menu = (MenuMonsterSpawn) this.getMenu();

		menu.getSpawnPoint().setChance(input.intValue());
		menu.saveSpawnerChanges();

		tell(context.getForWhom(), Lang.component("conversation-spawnerchance-set", "chance", input.intValue() + ""));
	}

	@Override
	protected boolean isNumberValid(Number input) {
		return input.intValue() <= 100;
	}

	@Override
	protected String getQuestion() {
		return Lang.legacy("conversation-spawnerchance-help");
	}
}
