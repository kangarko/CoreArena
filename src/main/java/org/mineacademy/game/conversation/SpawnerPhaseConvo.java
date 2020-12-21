package org.mineacademy.game.conversation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.game.menu.MenuMonsterSpawn;
import org.mineacademy.game.model.ActivePeriod;
import org.mineacademy.game.model.ActivePeriod.ActiveMode;
import org.mineacademy.game.settings.Localization;

public final class SpawnerPhaseConvo extends MenuQuestion {

	private static final Pattern INPUT_MATCHER = Pattern.compile(Localization.Conversation.Phase.MATCHER);

	public SpawnerPhaseConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected Prompt getFirstPrompt() {
		return new ValidatingPrompt() {

			@Override
			public String getPromptText(ConversationContext context) {
				return Common.colorize(Localization.Conversation.Phase.HELP);
			}

			@Override
			protected boolean isInputValid(ConversationContext context, String input) {
				return INPUT_MATCHER.matcher(input).matches();
			}

			@Override
			protected String getFailedValidationText(ConversationContext context, String invalidInput) {
				tellLater(1, context.getForWhom(), Localization.Conversation.INVALID_INPUT);
				return null;
			}

			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, String input) {
				final MenuMonsterSpawn menu = (MenuMonsterSpawn) getMenu();

				final Matcher m = INPUT_MATCHER.matcher(input);
				m.find();

				final ActiveMode mode = ActiveMode.fromName(m.group(1));
				final int limit = Integer.parseInt(m.group(2));
				final int limit2 = m.groupCount() > 3 && m.group(4) != null && input.contains("-") ? Integer.parseInt(m.group(4).replace("-", "")) : limit;

				final ActivePeriod period = new ActivePeriod(mode, limit, limit2);

				menu.getSpawnPoint().setActivePeriod(period);
				menu.saveSpawnerChanges();

				tell(context.getForWhom(), $(period));

				return Prompt.END_OF_CONVERSATION;
			}

			private String $(ActivePeriod period) {
				return formatPeriod(period).replace("{period}", period.formatLimits());
			}

			private String formatPeriod(ActivePeriod period) {
				switch (period.getMode()) {
					case FROM:
						return Localization.Conversation.Phase.SET_FROM;

					case ON:
						return Localization.Conversation.Phase.SET_ON;

					case TILL:
						return Localization.Conversation.Phase.SET_TILL;

					case BETWEEN:
						return Localization.Conversation.Phase.SET_BETWEEN;

					default:
						throw new FoException("Unhandled period: " + period.formatPeriod());
				}
			}
		};
	}
}
