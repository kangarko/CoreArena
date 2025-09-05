package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.conversation.SimpleConversation;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.settings.Lang;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MenuQuestion extends SimpleConversation {

	private final Menu menu;

	@Override
	protected void onConversationEnd(ConversationAbandonedEvent event) {
		final Conversable conversable = event.getContext().getForWhom();
		Valid.checkBoolean(conversable instanceof Player, "Conversation was ended by a non player: " + conversable);

		if (!event.gracefulExit())
			tell(conversable, Lang.component("conversation-cancelled"));

		else if (this.menu != null)
			this.menu.newInstance().displayTo((Player) conversable);
	}

	protected final Menu getMenu() {
		Valid.checkNotNull(this.menu, "Menu not set");

		return this.menu;
	}
}
