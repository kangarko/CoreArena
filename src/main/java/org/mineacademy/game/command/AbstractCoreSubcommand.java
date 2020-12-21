package org.mineacademy.game.command;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.game.command.placeholder.Placeholder;
import org.mineacademy.game.command.placeholder.PositionPlaceholder;
import org.mineacademy.game.command.placeholder.StaticPlaceholder;
import org.mineacademy.game.manager.ClassManager;
import org.mineacademy.game.manager.SetupManager;
import org.mineacademy.game.manager.UpgradesManager;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ArenaManager;
import org.mineacademy.game.model.ArenaRegistry;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;

abstract class AbstractCoreSubcommand extends SimpleSubCommand {

	/**
	 * A list of placeholders to replace in this command, see {@link Placeholder}
	 *
	 * These are used when sending player messages
	 */
	private final StrictList<Placeholder> placeholders = new StrictList<>();

	protected AbstractCoreSubcommand(String aliases) {
		super(aliases);

		addPlaceholder(new StaticPlaceholder("currency_name", Localization.Currency.rawPluralName()));
	}

	/**
	 * Registers a new placeholder to be used when sending messages to the player
	 *
	 * @param placeholder
	 */
	protected final void addPlaceholder(final Placeholder placeholder) {
		placeholders.add(placeholder);
	}

	@Override
	protected String replacePlaceholders(String message) {
		// Replace previous
		message = super.replacePlaceholders(message);

		// Replace saved placeholders
		for (final Placeholder placeholder : placeholders) {
			String toReplace = message;

			if (placeholder instanceof PositionPlaceholder) {
				final PositionPlaceholder arguedPlaceholder = (PositionPlaceholder) placeholder;

				if (args.length > arguedPlaceholder.getPosition())
					toReplace = args[arguedPlaceholder.getPosition()];
				else
					continue;
			}

			message = message.replace("{" + placeholder.getIdentifier() + "}", placeholder.replace(toReplace));
		}

		return message;
	}

	protected final boolean canEditArena(Arena arena) throws CommandException {
		if (arena.getState() != ArenaState.STOPPED)
			returnTell(Localization.Commands.Edit.ARENA_RUNNING);

		final ArenaManager commonManager = ArenaRegistry.getCommonManager();

		{ // Play
			if (commonManager.isPlaying(getPlayer()))
				returnTell(Localization.Commands.DISALLOWED_WHILE_PLAYING);
		}

		// Already editing
		{
			final Arena editedArena = commonManager.findEditedArena(getPlayer());

			if (editedArena != null && !editedArena.getName().equalsIgnoreCase(arena.getName()))
				returnTell(Localization.Commands.Edit.ALREADY_EDITING.replace("{arena}", editedArena.getName()));
		}

		return true;
	}

	protected final Arena getArena(String name) {
		return getArenas().findArena(name);
	}

	protected final ArenaManager getArenas() {
		return CoreArenaPlugin.getArenaManager();
	}

	protected final Upgrade getUpgrade(String name) {
		return getUpgrades().findUpgrade(name);
	}

	protected final UpgradesManager getUpgrades() {
		return CoreArenaPlugin.getUpgradesManager();
	}

	protected final ArenaClass getClass(String name) {
		return getClasses().findClass(name);
	}

	protected final ClassManager getClasses() {
		return CoreArenaPlugin.getClassManager();
	}

	protected final SetupManager getSetup() {
		return CoreArenaPlugin.getSetupManager();
	}

	protected final CoreArenaPlugin getCore() {
		return CoreArenaPlugin.getInstance();
	}
}
