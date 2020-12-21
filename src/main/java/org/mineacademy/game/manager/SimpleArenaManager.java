package org.mineacademy.game.manager;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictSet;
import org.mineacademy.fo.exception.InvalidWorldException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.impl.SimpleIncompleteRegion;
import org.mineacademy.game.impl.arena.MobArena;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaManager;
import org.mineacademy.game.model.ArenaRegistry;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSigns;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.StopCause;
import org.mineacademy.game.util.Constants;
import org.mineacademy.game.util.CoreUtil;

public final class SimpleArenaManager implements ArenaManager {

	/**
	 * List of all loaded arenas
	 */
	private final StrictSet<Arena> arenas = new StrictSet<>();

	public void loadSavedArenas() {
		arenas.clear();

		for (final Arena arena : loadFromFile())
			arenas.add(arena);
	}

	public void onPostLoad() {
		for (final Arena arena : arenas)
			arena.onPostLoad();

	}

	public void stopArenas(StopCause cause) {
		for (final Arena arena : arenas)
			if (arena.getState() != ArenaState.STOPPED)
				arena.stopArena(cause);
	}

	private StrictList<Arena> loadFromFile() {
		final StrictList<Arena> loaded = new StrictList<>();

		final File[] files = FileUtil.getFiles(Constants.Folder.ARENAS, "yml");
		Common.log("Loading " + Common.plural(files.length, "arena"));

		for (final File file : files)
			try {
				final String arenaName = file.getName().replace(".yml", "");
				final Arena arena = new MobArena(arenaName);

				if (!arena.getData().isSectionValid()) {
					Common.log("Ignoring invalid arena file " + file);

					ArenaRegistry.unregister(arena);
					YamlConfig.unregisterLoadedFile(file);
					continue;
				}

				loaded.add(arena);

			} catch (final Throwable t) {
				if (t instanceof InvalidWorldException)
					Common.logFramed(false, "Warning: You arena " + file.getName() + " had invalid world and is ignored. Error was: " + t.getMessage());
				else {
					t.printStackTrace();

					Common.throwError(t, "Error loading arena from " + file.getName());
				}
			}

		return loaded;
	}

	public Arena createArena(String name) {
		Valid.checkBoolean(findArena(name) == null, "Arena " + name + " already exists");
		final Arena arena = new MobArena(name);
		arena.onPostLoad();

		arenas.add(arena);
		return arena;
	}

	public void removeArena(String name) {
		final Arena arena = findArena(name);
		Valid.checkBoolean(arena != null, "Arena " + name + " does not exist!");

		arena.getData().deleteSection();
		arena.getSettings().removeSettingsFile();

		ArenaRegistry.unregister(arena);

		arenas.remove(arena);
	}

	@Override
	public Arena findArena(String name) {
		for (final Arena arena : arenas)
			if (arena.getName().equalsIgnoreCase(name))
				return arena;

		return null;
	}

	@Override
	public Arena findArena(Player pl) {
		for (final Arena arena : arenas)
			if (arena.getState() != ArenaState.STOPPED && arena.getSetup().isReady() && arena.isJoined(pl)) {

				Valid.checkBoolean(CoreArenaPlugin.getDataFor(pl).hasArenaCache(), pl.getName() + " is in " + arena.getName() + " but lacks cache!");

				return arena;
			}

		return null;
	}

	@Override
	public ArenaSign findSign(Sign sign) {
		for (final Arena arena : arenas) {
			final ArenaSigns signs = arena.getData().getSigns();

			if (signs != null) {
				final ArenaSign arenasign = signs.getSignAt(sign.getLocation());

				if (arenasign != null)
					return arenasign;
			}
		}

		return null;
	}

	@Override
	public Arena findArena(Location loc) {
		Valid.checkNotNull(loc, "Location cannot be null!");

		for (final Arena arena : arenas)
			if (arena.getData().getRegion() != null && !(arena.getData().getRegion() instanceof SimpleIncompleteRegion))
				if (arena.getData().getRegion().isWithin(loc))
					return arena;

		return null;
	}

	public void tellAvailableArenas(CommandSender sender) {
		final SimpleComponent json = SimpleComponent.of(Common.getTellPrefix() + " Available arenas: ");

		for (int i = 0; i < arenas.size(); i++) {
			final Arena arena = arenas.getAt(i);

			final boolean acceptingJoin = arena.isEnabled() && arena.getSetup().isReady() && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena);
			final boolean isStopped = arena.getState() == ArenaState.STOPPED;
			final ChatColor color = acceptingJoin ? isStopped ? ChatColor.GREEN : ChatColor.GOLD : ChatColor.RED;

			json.append(color.toString() + arena.getName());

			if (acceptingJoin)
				json.onHover("       &8[-&7&l" + arena.getName() + "&8-]        ", "&5" + Localization.Parts.STATE + CoreUtil.getStateName(arena.getState()), isStopped ? "&5" + Localization.Parts.SIZE + Localization.Cases.PLAYER.formatWithCount(arena.getSettings().getMaximumPlayers()) : "&5" + Localization.Parts.PLAYERS + arena.getPlayers().size() + "&8/&7" + arena.getSettings().getMaximumPlayers());
			else
				json.onHover((!arena.isEnabled() ? Localization.Arena.Error.NOT_ENABLED : !arena.getSetup().isReady() ? Localization.Arena.Error.NOT_CONFIGURED : CoreArenaPlugin.getSetupManager().isArenaEdited(arena) ? Localization.Arena.Error.EDITED : "&4(Error)").replace("{arena}", arena.getName()));

			if (arenas.size() > 1 && i + 1 < arenas.size())
				json.append("&7, ");
		}

		json.send(sender);
	}

	@Override
	public Set<Arena> getArenas() {
		return Collections.unmodifiableSet(arenas.getSource());
	}

	@Override
	public List<String> getAvailable() {
		return Common.convert(arenas, Arena::getName);
	}

	@Override
	public boolean isPlaying(Player player) {
		return findArena(player) != null;
	}

	@Override
	public Arena findEditedArena(Player player) {
		return CoreArenaPlugin.getSetupManager().getEditedArena(player);
	}

	@Override
	public boolean isEditing(Player player) {
		return findEditedArena(player) != null;
	}
}
