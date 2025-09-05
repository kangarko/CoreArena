package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.ConversationContext;
import org.mineacademy.corearena.menu.MenuMonsterSpawn;
import org.mineacademy.fo.settings.Lang;

public final class SpawnerMinPlayerConvo extends MenuNumberQuestion {

	public SpawnerMinPlayerConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		final MenuMonsterSpawn menu = (MenuMonsterSpawn) this.getMenu();

		menu.getSpawnPoint().setMinimumPlayers(input.intValue());
		menu.saveSpawnerChanges();

		tell(context.getForWhom(), Lang.component("conversation-spawner-min-players-set", "amount_players", Lang.numberFormat("case-player", input.intValue())));
	}

	@Override
	protected String getQuestion() {
		return Lang.legacy("conversation-spawner-min-players-help");
	}
}
