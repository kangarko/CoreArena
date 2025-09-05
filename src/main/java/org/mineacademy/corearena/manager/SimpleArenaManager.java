package org.mineacademy.corearena.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.impl.SimpleIncompleteRegion;
import org.mineacademy.corearena.impl.arena.MobArena;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaManager;
import org.mineacademy.corearena.model.ArenaRegistry;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSigns;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.InvalidWorldException;
import org.mineacademy.fo.exception.YamlSyntaxError;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.Lang;

public final class SimpleArenaManager implements ArenaManager {

	/**
	 * List of all loaded arenas
	 */
	private final Set<Arena> arenas = new HashSet<>();

	public void loadSavedArenas() {
		this.arenas.clear();

		for (final Arena arena : this.loadFromFile())
			this.arenas.add(arena);
	}

	public void onPostLoad() {
		for (final Arena arena : this.arenas)
			arena.onPostLoad();

	}

	public void stopArenas(StopCause cause) {
		for (final Arena arena : this.arenas)
			if (arena.getState() != ArenaState.STOPPED)
				arena.stopArena(cause);
	}

	private List<Arena> loadFromFile() {
		final List<Arena> loaded = new ArrayList<>();

		final File[] files = FileUtil.getFiles(Constants.Folder.ARENAS, "yml");
		Common.log("Loading " + Lang.numberFormat("case-arena", files.length));

		for (final File file : files)
			try {
				final String arenaName = file.getName().replace(".yml", "");
				final Arena arena = new MobArena(arenaName);

				loaded.add(arena);

			} catch (final Throwable t) {
				if (t instanceof InvalidWorldException)
					Common.logFramed(false, "Warning: You arena " + file.getName() + " had invalid world and is ignored. Error was: " + t.getMessage());

				else if (t instanceof YamlSyntaxError)
					Common.logFramed(false, "Warning: Ignoring arena: " + t.getMessage());

				else
					Common.throwError(t, "Error loading arena from " + file.getName());
			}

		return loaded;
	}

	public Arena createArena(String name) {
		Valid.checkBoolean(this.findArena(name) == null, "Arena " + name + " already exists");
		final Arena arena = new MobArena(name);
		arena.onPostLoad();

		this.arenas.add(arena);
		return arena;
	}

	public void removeArena(String name) {
		final Arena arena = this.findArena(name);
		Valid.checkBoolean(arena != null, "Arena " + name + " does not exist!");

		arena.getData().clear();
		arena.getSettings().deleteFile();

		ArenaRegistry.unregister(arena);

		if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
			CoreArenaPlugin.getSetupManager().removeEditedArena(arena);

		this.arenas.remove(arena);
	}

	@Override
	public Arena findArena(String name) {
		for (final Arena arena : this.arenas)
			if (arena.getName().equalsIgnoreCase(name))
				return arena;

		return null;
	}

	@Override
	public Arena findArena(Player player) {
		for (final Arena arena : this.arenas)
			if (arena.getState() != ArenaState.STOPPED && arena.getSetup().isReady() && arena.isJoined(player)) {
				Valid.checkBoolean(CoreArenaPlugin.getDataFor(player).hasArenaCache(), player.getName() + " is in " + arena.getName() + " but lacks cache!");

				return arena;
			}

		return null;
	}

	@Override
	public ArenaSign findSign(Sign sign) {
		for (final Arena arena : this.arenas) {
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

		for (final Arena arena : this.arenas)
			if (arena.getData().getRegion() != null && !(arena.getData().getRegion() instanceof SimpleIncompleteRegion))
				if (arena.getData().getRegion().isWithin(loc))
					return arena;

		return null;
	}

	public void tellAvailableArenas(CommandSender sender) {
		SimpleComponent component = SimpleComponent.fromMiniAmpersand("<red>Available arenas: ");

		int i = 0;

		for (final Arena arena : this.arenas) {

			final boolean acceptingJoin = arena.isEnabled() && arena.getSetup().isReady() && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena);
			final boolean isStopped = arena.getState() == ArenaState.STOPPED;
			final CompChatColor color = acceptingJoin ? isStopped ? CompChatColor.GREEN : CompChatColor.GOLD : CompChatColor.RED;

			component = component.appendMiniAmpersand(color.toString() + arena.getName());

			if (acceptingJoin)
				component = component
						.onHoverLegacy(" &8[-&7&l" + arena.getName() + "&8-] ",
								"&5" + Lang.legacy("part-state", "state", CoreUtil.getStateName(arena.getState())),
								isStopped
										? "&5" + Lang.legacy("part-size", "size", Lang.numberFormat("case-player", arena.getSettings().getMaximumPlayers()))
										: "&5" + Lang.legacy("part-players", "players", arena.getPlayers().size() + "&8/&7" + arena.getSettings().getMaximumPlayers()))
						.onClickSuggestCmd("/" + Settings.MAIN_COMMAND_ALIASES.get(0) + " join " + arena.getName());

			else
				component = component.onHoverLegacy((!arena.isEnabled() ? Lang.legacy("arena-error-not-enabled", "arena", arena.getName()) : !arena.getSetup().isReady() ? Lang.legacy("arena-error-not-configured", "arena", arena.getName()) : CoreArenaPlugin.getSetupManager().isArenaEdited(arena) ? Lang.legacy("arena-error-edited", "arena", arena.getName()) : "&4(Error)"));

			if (this.arenas.size() > 1 && i + 1 < this.arenas.size())
				component = component.appendMiniAmpersand("&7, ");

			i++;
		}

		Common.tell(sender, component);
	}

	@Override
	public Set<Arena> getArenas() {
		return Collections.unmodifiableSet(this.arenas);
	}

	@Override
	public List<String> getArenasNames() {
		return Common.convertList(this.arenas, Arena::getName);
	}

	@Override
	public boolean isPlaying(Player player) {
		return this.findArena(player) != null;
	}

	@Override
	public Arena findEditedArena(Player player) {
		return CoreArenaPlugin.getSetupManager().getEditedArena(player);
	}

	@Override
	public boolean isEditing(Player player) {
		return this.findEditedArena(player) != null;
	}
}
