package org.mineacademy.game.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.menu.IndividualClassMenu;
import org.mineacademy.game.menu.IndividualUpgradeMenu;
import org.mineacademy.game.menu.MenuGameTools;
import org.mineacademy.game.settings.Localization;

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
			return name().toLowerCase();
		}
	}

	@Override
	protected Prompt getFirstPrompt() {
		return new ValidatingPrompt() {

			@Override
			public String getPromptText(ConversationContext context) {
				return Common.colorize(Localization.Conversation.New.HELP.replace("{type}", created.toString()));
			}

			@Override
			protected boolean isInputValid(ConversationContext context, String input) {
				if (input.contains(" ")) {
					tellLater(1, context.getForWhom(), Localization.Conversation.New.NO_SPACES);

					return false;
				}

				if (created.exists(input)) {
					tellLater(1, context.getForWhom(), Localization.Conversation.New.ALREADY_EXISTS.replace("{type}", created.toString()));

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
					created.create(input);
				} catch (final Throwable t) {
					Common.error(t, "Error creating " + created + " " + input);
					tell(context.getForWhom(), Localization.Conversation.New.ERROR);

					return Prompt.END_OF_CONVERSATION;
				}

				if (created == Created.ARENA) {
					CoreArenaPlugin.getSetupManager().addEditedArena(player, CoreArenaPlugin.getArenaManager().findArena(input));
					tell(context.getForWhom(), Localization.Conversation.New.SUCCESS_ARENA.replace("{name}", input));

					Common.runLater(2, new BukkitRunnable() {

						@Override
						public void run() {
							MenuGameTools.getInstance().displayTo(player);
						}
					});

				} else {
					tell(context.getForWhom(), Localization.Conversation.New.SUCCESS.replace("{type}", created.toString()).replace("{name}", input));

					Common.runLater(2, new BukkitRunnable() {

						@Override
						public void run() {
							if (created == Created.CLASS)
								new IndividualClassMenu(input, true).displayTo(player);

							else if (created == Created.UPGRADE)
								new IndividualUpgradeMenu(input, true).displayTo(player);

							else
								throw new FoException("Menu after creating " + created + " not implemented");
						}
					});
				}

				return Prompt.END_OF_CONVERSATION;
			}
		};
	}
}