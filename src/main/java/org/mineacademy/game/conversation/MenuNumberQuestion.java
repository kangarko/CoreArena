package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.settings.Localization;

public abstract class MenuNumberQuestion extends MenuQuestion {

	public MenuNumberQuestion(Menu menu) {
		super(menu);
	}

	@Override
	protected final Prompt getFirstPrompt() {
		return new NumericPrompt() {

			@Override
			protected boolean isNumberValid(ConversationContext context, Number input) {
				return input instanceof Integer && input.intValue() > 0 && MenuNumberQuestion.this.isNumberValid(input);
			}

			@Override
			public String getPromptText(ConversationContext context) {
				return Common.colorize(getQuestion());
			}

			@Override
			protected String getFailedValidationText(ConversationContext context, String invalidInput) {
				return Common.colorize(Common.getTellPrefix() + Localization.Conversation.NUMBER_HELP);
			}

			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
				onSuccess(context, input);

				return Prompt.END_OF_CONVERSATION;
			}
		};
	}

	protected boolean isNumberValid(Number input) {
		return true;
	}

	protected abstract String getQuestion();

	protected abstract void onSuccess(ConversationContext context, Number input);
}
