package org.mineacademy.corearena.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.menu.IndividualClassMenu;
import org.mineacademy.corearena.menu.IndividualUpgradeMenu;
import org.mineacademy.corearena.menu.MenuGameTools;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

public final class AddNewConvo extends MenuQuestion {

	private final Created created;

	public AddNewConvo(Created created, Menu menu) {
		super(menu);

		this.created = created;
	}

	public enum Created {
		ARENA {
			@Override
			public void create(String name) {
				CoreArenaPlugin.getArenaManager().createArena(name);
			}

			@Override
			protected boolean exists(String name) {
				return CoreArenaPlugin.getArenaManager().findArena(name) != null;
			}
		},

		UPGRADE {
			@Override
			public void create(String name) {
				CoreArenaPlugin.getUpgradesManager().createUpgrade(name);
			}

			@Override
			protected boolean exists(String name) {
				return CoreArenaPlugin.getUpgradesManager().findUpgrade(name) != null;
			}
		},

		CLASS {
			@Override
			public void create(String name) {
				CoreArenaPlugin.getClassManager().createClass(name);
			}

			@Override
			protected boolean exists(String name) {
				return CoreArenaPlugin.getClassManager().findClass(name) != null;
			}
		},

		;

		protected abstract void create(String name);

		protected abstract boolean exists(String name);

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	@Override
	protected Prompt getFirstPrompt() {
		return new ValidatingPrompt() {

			@Override
			public String getPromptText(ConversationContext context) {
				return Lang.legacy("conversation-new-help", "type", AddNewConvo.this.created.toString());
			}

			@Override
			protected boolean isInputValid(ConversationContext context, String input) {
				if (input.contains(" ")) {
					tell(context.getForWhom(), Lang.component("conversation-new-no-spaces"));

					return false;
				}

				if (AddNewConvo.this.created.exists(input)) {
					tell(context.getForWhom(), Lang.component("conversation-new-already-exists", "type", AddNewConvo.this.created.toString()));

					return false;
				}

				return true;
			}

			@Override
			protected String getFailedValidationText(ConversationContext context, String invalidInput) {
				return null;
			}

			@Override
			protected Prompt acceptValidatedInput(ConversationContext context, String input) {
				final Player player = (Player) context.getForWhom();

				try {
					AddNewConvo.this.created.create(input);
				} catch (final Throwable t) {
					Common.error(t, "Error creating " + AddNewConvo.this.created + " " + input);
					tell(context.getForWhom(), Lang.component("conversation-new-error"));

					return Prompt.END_OF_CONVERSATION;
				}

				if (AddNewConvo.this.created == Created.ARENA) {
					CoreArenaPlugin.getSetupManager().addEditedArena(player, CoreArenaPlugin.getArenaManager().findArena(input));
					tell(context.getForWhom(), Lang.component("conversation-new-success-arena", "name", input));

					Platform.runTask(2, new SimpleRunnable() {

						@Override
						public void run() {
							MenuGameTools.getInstance().displayTo(player);
						}
					});

				} else {
					tell(context.getForWhom(), Lang.component("conversation-new-success", "type", AddNewConvo.this.created.toString(), "name", input));

					Platform.runTask(2, new SimpleRunnable() {

						@Override
						public void run() {
							if (AddNewConvo.this.created == Created.CLASS)
								new IndividualClassMenu(input, true).displayTo(player);

							else if (AddNewConvo.this.created == Created.UPGRADE)
								new IndividualUpgradeMenu(input, true).displayTo(player);

							else
								throw new FoException("Menu after creating " + AddNewConvo.this.created + " not implemented");
						}
					});
				}

				return Prompt.END_OF_CONVERSATION;
			}
		};
	}
}