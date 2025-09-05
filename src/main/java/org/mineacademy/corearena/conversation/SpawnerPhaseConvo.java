package org.mineacademy.corearena.conversation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.mineacademy.corearena.menu.MenuMonsterSpawn;
import org.mineacademy.corearena.model.ActivePeriod;
import org.mineacademy.corearena.model.ActivePeriod.ActiveMode;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.settings.Lang;

public final class SpawnerPhaseConvo extends MenuQuestion {

	private static final Pattern INPUT_MATCHER = Pattern.compile(Lang.plain("conversation-phase-matcher"));

	public SpawnerPhaseConvo(MenuMonsterSpawn menu) {
		super(menu);
	}

	@Override
	protected Prompt getFirstPrompt() {
		return new ValidatingPrompt() {

			@Override
			public String getPromptText(ConversationContext context) {
				return CompChatColor.translateColorCodes(String.join("\n", Lang.legacy("conversation-phase-help")));
			}

			@Override
			protected boolean isInputValid(ConversationContext context, String input) {
				return INPUT_MATCHER.matcher(input).matches();
			}

			@Override
			protected String getFailedValidationText(ConversationContext context, String invalidInput) {
				return Lang.legacy("conversation-invalid-input");
			}

			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, String input) {
				final MenuMonsterSpawn menu = (MenuMonsterSpawn) SpawnerPhaseConvo.this.getMenu();

				final Matcher m = INPUT_MATCHER.matcher(input);
				m.find();

				final ActiveMode mode = ActiveMode.fromKey(m.group(1));
				final int limit = Integer.parseInt(m.group(2));
				final int limit2 = m.groupCount() > 3 && m.group(4) != null && input.contains("-") ? Integer.parseInt(m.group(4).replace("-", "")) : limit;

				final ActivePeriod period = new ActivePeriod(mode, limit, limit2);

				menu.getSpawnPoint().setActivePeriod(period);
				menu.saveSpawnerChanges();

				tell(context.getForWhom(), this.$(period));

				return Prompt.END_OF_CONVERSATION;
			}

			private String $(ActivePeriod period) {
				return this.formatPeriod(period).replace("{period}", period.formatLimits());
			}

			private String formatPeriod(ActivePeriod period) {
				switch (period.getMode()) {
					case FROM:
						return Lang.legacy("conversation-phase-set-from");

					case ON:
						return Lang.legacy("conversation-phase-set-on");

					case TILL:
						return Lang.legacy("conversation-phase-set-till");

					case BETWEEN:
						return Lang.legacy("conversation-phase-set-between");

					default:
						throw new FoException("Unhandled period: " + period.formatPeriod());
				}
			}
		};
	}
}
