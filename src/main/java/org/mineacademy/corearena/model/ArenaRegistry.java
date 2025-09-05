package org.mineacademy.corearena.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The main class that you need to utilize in order for your plugin to be
 * recognized within other plugins.
 *
 * Please register your arena manually at {@link #register(Arena)}!
 */
public class ArenaRegistry {

	/**
	 * A list of all registered arenas by their respective plugin.
	 * Your plugin must implement {@link ArenaPlugin} class.
	 */
	private static final HashMap<ArenaPlugin, List<Arena>> registered = new HashMap<>();

	/**
	 * The general arena manager.
	 */
	private static final ArenaManager commonArenaManager = new CommonArenaManager();

	/**
	 * Register your arena within your arena plugin.
	 *
	 * @param arena the arena
	 */
	public static void register(Arena arena) {
		final List<Arena> current = registered.getOrDefault(arena.getPlugin(), new ArrayList<>());
		Valid.checkBoolean(!current.contains(arena), "Arena " + arena.getName() + " already registered for " + arena.getPlugin().getName());

		current.add(arena);
		registered.put(arena.getPlugin(), current);
	}

	/**
	 * Unregister an arena of a certain arena plugin.
	 *
	 * @param arena the arena
	 */
	public static void unregister(Arena arena) {
		final List<Arena> current = registered.get(arena.getPlugin());

		Objects.requireNonNull(current, "Plugin " + arena.getPlugin().getName() + " associated with arena " + arena.getName() + " is not registered");
		Valid.checkBoolean(current.remove(arena), "Arena " + arena.getName() + " is not registered for " + arena.getPlugin().getName());
	}

	/**
	 * Clears all registered arenas for a plugin.
	 * Removes all instances of the plugin with a name
	 *
	 * @param plugin the plugin
	 * @return if the plugin was found
	 */
	public static boolean unregisterAll(String plugin) {
		boolean found = false;

		for (final Iterator<ArenaPlugin> it = registered.keySet().iterator(); it.hasNext();) {
			final ArenaPlugin other = it.next();

			if (other.getName().equals(plugin)) {
				it.remove();

				found = true;
			}
		}

		return found;
	}

	/**
	 * Unregister all arenas of an associated plugin
	 *
	 * @param plugin the plugin
	 */
	public static void unregisterAll(ArenaPlugin plugin) {
		Valid.checkBoolean(registered.remove(plugin) != null, "Plugin " + plugin + " is not registered at all");
	}

	/**
	 * Get all registered arena plugins.
	 *
	 * @return registered plugins
	 */
	public static Set<ArenaPlugin> getRegisteredPlugins() {
		return Collections.unmodifiableSet(registered.keySet());
	}

	/**
	 * Get all arenas for a plugin.
	 *
	 * @param plugin the plugin
	 * @return the arenas for that plugin
	 */
	public static List<Arena> getArenas(ArenaPlugin plugin) {
		final List<Arena> list = registered.get(plugin);

		return list != null ? Collections.unmodifiableList(list) : null;
	}

	/**
	 * Get if the plugin is registered - by name - not by the instance.
	 * If you want to check if it is registered by instance, use {@link #getRegisteredPlugins()}.contains()
	 *
	 * @param plugin the plugin
	 * @return if the plugin is registered
	 */
	public static boolean isRegistered(String plugin) {
		for (final ArenaPlugin other : getRegisteredPlugins())
			if (other.getName().equals(plugin))
				return true;

		return false;
	}

	/**
	 * Returns an arena manager for a specified plugin
	 *
	 * @param plugin an arena plugin
	 * @return the arena manager
	 */
	public static ArenaManager getArenaManager(ArenaPlugin plugin) {
		return new SpecificArenaManager(plugin);
	}

	/**
	 * Returns arena manager that has all arenas from all plugins.
	 *
	 * BEWARE: Misuse leads to problems since methods in here return/search in all arenas
	 * in all compatible plugins (e.g. Puncher+CoreArena).
	 *
	 * @deprecated potentially dangerous because it returns all arenas from all plugins mixed up,
	 * 			   use {@link #getArenaManager(ArenaPlugin)} instead
	 * @return the common arena manager
	 */
	@Deprecated
	public static ArenaManager getCommonManager() {
		return commonArenaManager;
	}

	/**
	 * An arena manager that only returns arenas for a specific {@link ArenaPlugin}
	 */
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class SpecificArenaManager extends CommonArenaManager {

		/**
		 * The plugin linked to this manager.
		 */
		@NonNull
		private final ArenaPlugin plugin;

		/**
		 * Get all arenas from the registered plugin
		 *
		 * @return
		 */
		@Override
		public Set<Arena> getArenas() {
			final HashSet<Arena> set = new HashSet<>(super.getArenas());

			// Remove all arenas not owned by our plugin
			set.removeIf(arena -> !arena.getPlugin().getName().equals(this.plugin.getName()));

			return set;
		}
	}

	/**
	 * Represents an arena manager that is shared for all of the registered arenas.
	 *
	 * @deprecated potentialy dangerous because we return all arenas from all plugins
	 */
	@Deprecated
	public static class CommonArenaManager implements ArenaManager {

		private CommonArenaManager() {
		}

		/**
		 * Return all arenas from all plugins.
		 *
		 * This is the main method every other method below calls and should be overriden
		 * by your own to only return arenas from your plugin
		 *
		 * @return all arenas from all plugins
		 */
		@Override
		public Set<Arena> getArenas() {
			final Set<Arena> all = new HashSet<>();

			for (final List<Arena> pluginArenas : registered.values())
				all.addAll(pluginArenas);

			return all;
		}

		@Override
		public final List<String> getArenasNames() {
			final List<String> all = new ArrayList<>();

			this.getArenas().forEach(a -> all.add(a.getName()));

			return all;
		}

		@Override
		public final Arena findArena(String name) {
			for (final Arena arena : this.getArenas())
				if (arena.getName().equalsIgnoreCase(name))
					return arena;

			return null;
		}

		@Override
		public final Arena findArena(Location loc) {
			for (final Arena arena : this.getArenas())
				if (arena.getData().getRegion() != null && arena.getData().getRegion().isWithin(loc))
					return arena;

			return null;
		}

		@Override
		public final Arena findArena(Player pl) {
			for (final Arena arena : this.getArenas()) {
				if (!arena.getSetup().isReady())
					continue;

				if (arena.getPlayers().contains(pl))
					return arena;
			}

			return null;
		}

		@Override
		public final ArenaSign findSign(Sign sign) {
			for (final Arena arena : this.getArenas()) {
				final ArenaSigns signs = arena.getData().getSigns();

				if (signs != null) {
					final ArenaSign arenaSign = signs.getSignAt(sign.getLocation());

					if (arenaSign != null)
						return arenaSign;
				}
			}

			return null;
		}

		@Override
		public Arena findEditedArena(Player player) {
			for (final Arena arena : this.getArenas()) {
				final Player editor = arena.getSetup().getEditor();

				if (editor != null && editor.isOnline() && editor.getName().equals(player.getName()))
					return arena;
			}

			return null;
		}

		@Override
		public boolean isEditing(Player player) {
			return this.findEditedArena(player) != null;
		}

		@Override
		public final boolean isPlaying(Player pl) {
			return this.findArena(pl) != null;
		}
	}
}
