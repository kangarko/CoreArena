package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.mineacademy.game.menu.MenuMonsterSpawn;
import org.mineacademy.game.settings.Localization;

public final class SpawnerMinPlayerConvo extends MenuNumberQuestion {

	public SpawnerMinPlayerConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected void onSuccess(ConversationContext context, Number input) {
		final MenuMonsterSpawn menu = (MenuMonsterSpawn) getMenu();

		menu.getSpawnPoint().setMinimumPlayers(input.intValue());
		menu.saveSpawnerChanges();

		tell(context.getForWhom(), Localization.Conversation.SpawnerMinPlayers.SET.replace("{amount_players}", Localization.Cases.PLAYER.formatWithCount(input.intValue())));
	}

	@Override
	protected String getQuestion() {
		return Localization.Conversation.SpawnerMinPlayers.HELP;
	}
}
