package org.mineacademy.game.conversation;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.conversation.SimpleConversation;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.settings.Localization;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MenuQuestion extends SimpleConversation {

	private final Menu menu;

	@Override
	protected void onConversationEnd(ConversationAbandonedEvent e) {
		final Conversable c = e.getContext().getForWhom();
		Valid.checkBoolean(c instanceof Player, "KaConversation was ended by a non player: " + c);

		if (!e.gracefulExit())
			tell(c, Localization.Conversation.CANCELLED);

		else if (menu != null)
			menu.newInstance().displayTo((Player) c);
	}

	protected final Menu getMenu() {
		Valid.checkNotNull(menu, "Menu not set");

		return menu;
	}
}
