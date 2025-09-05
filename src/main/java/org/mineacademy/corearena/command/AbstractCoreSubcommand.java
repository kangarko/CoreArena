package org.mineacademy.corearena.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.command.placeholder.Placeholder;
import org.mineacademy.corearena.command.placeholder.PositionPlaceholder;
import org.mineacademy.corearena.command.placeholder.StaticPlaceholder;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.manager.ClassManager;
import org.mineacademy.corearena.manager.SetupManager;
import org.mineacademy.corearena.manager.UpgradesManager;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ArenaManager;
import org.mineacademy.corearena.model.ArenaRegistry;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.settings.Lang;

abstract class AbstractCoreSubcommand extends SimpleSubCommand {

	/**
	 * A list of placeholders to replace in this command, see {@link Placeholder}
	 *
	 * These are used when sending player messages
	 */
	private final List<Placeholder> placeholders = new ArrayList<>();

	protected AbstractCoreSubcommand(String aliases) {
		super(aliases);

		this.addPlaceholder(new StaticPlaceholder("currency_name", Lang.numberFormatNoAmount("currency-name", 0)));
	}

	/**
	 * Registers a new placeholder to be used when sending messages to the player
	 *
	 * @param placeholder
	 */
	protected final void addPlaceholder(final Placeholder placeholder) {
		this.placeholders.add(placeholder);
	}

	@Override
	protected Map<String, Object> preparePlaceholders() {
		final Map<String, Object> map = super.preparePlaceholders();

		for (final Placeholder placeholder : this.placeholders) {
			final String toReplace = null;

			if (placeholder instanceof PositionPlaceholder) {
				final PositionPlaceholder arguedPlaceholder = (PositionPlaceholder) placeholder;

				if (this.args.length > arguedPlaceholder.getPosition())
					map.put(String.valueOf(arguedPlaceholder.getPosition()), this.args[arguedPlaceholder.getPosition()]);
				else
					continue;
			} else
				map.put(placeholder.getIdentifier(), placeholder.replace(toReplace));
		}

		return map;
	}

	protected final boolean canEditArena(Arena arena) throws CommandException {
		if (arena.getState() != ArenaState.STOPPED)
			this.returnTell(Lang.component("command-edit-arena-running"));

		final ArenaManager commonManager = ArenaRegistry.getCommonManager();

		{ // Play
			if (commonManager.isPlaying(this.getPlayer()))
				this.returnTell(Lang.component("command-disallowed-while-playing"));
		}

		// Already editing
		{
			final Arena editedArena = commonManager.findEditedArena(this.getPlayer());

			if (editedArena != null && !editedArena.getName().equalsIgnoreCase(arena.getName()))
				this.returnTell(Lang.component("command-edit-already-editing", "arena", editedArena.getName()));
		}

		return true;
	}

	protected final Arena getArena(String name) {
		return this.getArenas().findArena(name);
	}

	protected final ArenaManager getArenas() {
		return CoreArenaPlugin.getArenaManager();
	}

	protected final Upgrade getUpgrade(String name) {
		return this.getUpgrades().findUpgrade(name);
	}

	protected final UpgradesManager getUpgrades() {
		return CoreArenaPlugin.getUpgradesManager();
	}

	protected final ArenaClass getClass(String name) {
		return this.getClasses().findClass(name);
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

	protected final ArenaPlayer getCache(Player player) {
		return CoreArenaPlugin.getDataFor(player);
	}

	protected final Arena findArenaWhenUnspecified(Player player) {
		Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena == null)
			arena = CoreArenaPlugin.getArenaManager().findArena(player.getLocation());

		return arena;
	}

	protected final void returnTellAvailable(CommandSender sender) throws CommandException {
		CoreArenaPlugin.getArenaManager().tellAvailableArenas(sender);

		throw new CommandException();
	}

	protected final List<String> completeLastWordArenaNames() {
		return this.completeLastWord(CoreArenaPlugin.getArenaManager().getArenasNames());
	}
}
