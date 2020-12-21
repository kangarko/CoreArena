package org.mineacademy.game.impl;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.YamlSectionConfig;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.settings.Settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class ArenaPlayer extends YamlSectionConfig {

	// --------------------------------------------------------------------------------
	// Data saved to data.db
	// --------------------------------------------------------------------------------

	@Getter
	private final UUID uuid;

	@Getter
	private final String playerName;

	@Getter
	private int nuggets;

	private StrictMap<String, Integer> purchasedTiers = new StrictMap<>();

	// --------------------------------------------------------------------------------
	// Internal data
	// --------------------------------------------------------------------------------

	private InArenaCache arenaCache;
	private SetupCache setupCache;
	private ClassCache classCache;

	public final class ClassCache {
		/**
		 * The player's class, if any
		 */
		public ArenaClass assignedClass;

		/**
		 * Tier of his class.
		 */
		public int classTier;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public final class InArenaCache {

		/**
		 * Name of the location from which the player escaped boredom.
		 */
		public final Location prevLocation;

		/**
		 * Whether the kicking process has been initiated
		 */
		public boolean pendingRemoval = false;

		/**
		 * Whether the joining process has been initiated
		 */
		public boolean pendingJoining = false;

		/**
		 * The player's spawn point, if any
		 */
		public SpawnPoint spawnPoint;

		/**
		 * How many lifes left, if configured
		 */
		public int lifesLeft = -1;

		/**
		 * Temporary permissions
		 */
		public StrictList<PermissionAttachment> givenPermissions = new StrictList<>();

		/**
		 * The experience points
		 */
		@Getter
		private int exp;

		/**
		 * The experience levels
		 */
		@Getter
		private int level;

		public Arena getArena(Player player) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);
			Valid.checkNotNull(arena, "Player " + player.getName() + " has arena cache but lacks an arena!");

			return arena;
		}

		public void giveAndShowExp(Player pl, int expToGive) {
			final double totalExp = exp + expToGive;

			// Calculate levels to give
			final double levelsToGive = level + totalExp / Settings.Experience.EXP_PER_LEVEL;

			final int newLevel = (int) MathUtil.floor(levelsToGive);

			// Calculate rest experience
			final String[] split = Double.toString(levelsToGive).split("\\.");
			Valid.checkBoolean(split.length == 2, "Malformed level calculation length " + split.length);

			final double rest = Double.parseDouble("0." + split[1]);
			final int newExp = (int) Math.round(rest * Settings.Experience.EXP_PER_LEVEL);

			level = newLevel;
			exp = newExp;

			updateExpBar(pl);
		}

		public void takeAndShowLevels(Player pl, int levels) {
			Valid.checkBoolean(level >= levels, "Cannot take more levels than player have (" + level + " < " + levels + ")!");
			level = level - levels;

			updateExpBar(pl);
		}

		private void updateExpBar(Player pl) {
			pl.setExp((float) ((double) exp / Settings.Experience.EXP_PER_LEVEL));
			pl.setLevel(level);
		}
	}

	@Setter
	public final class SetupCache {

		/**
		 * The arena player is editing
		 */
		public final Arena arena;

		/**
		 * His scoreboard when editing
		 */
		private SimpleSidebarEdit sidebar;

		public SetupCache(Arena arena) {
			this.arena = arena;
			this.sidebar = new SimpleSidebarEdit(arena);
		}

		public void showSidebar(Player player) {
			if (sidebar != null && !sidebar.isViewing(player))
				sidebar.show(player);
		}

		public void hideSidebar(Player player) {
			if (sidebar != null && sidebar.isViewing(player))
				sidebar.hide(player);
		}
	}

	// --------------------------------------------------------------------------------
	// Loading
	// --------------------------------------------------------------------------------

	public ArenaPlayer(UUID uuid, String name) {
		super("Players." + uuid.toString());

		this.uuid = uuid;
		this.playerName = name;

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		this.nuggets = getInteger("Nuggets", 0);

		loadPurchasedTiers();
	}

	private void loadPurchasedTiers() {
		final Object tiersRaw = getObject("Tiers");

		if (tiersRaw != null)
			if (tiersRaw instanceof MemorySection) {
				final MemorySection section = (MemorySection) tiersRaw;

				for (final String tier : section.getKeys(false)) {
					final int level = section.getInt(tier);

					purchasedTiers.override(tier, level);
				}

			} else if (tiersRaw instanceof HashMap) {
				final HashMap<String, Integer> map = (HashMap<String, Integer>) tiersRaw;

				purchasedTiers = new StrictMap<>(map);
			}
	}

	// --------------------------------------------------------------------------------
	// Gettings for data.db
	// --------------------------------------------------------------------------------

	public void setNuggets(int nuggets) {
		this.nuggets = nuggets;

		save("Nuggets", nuggets);

		if (Settings.MySQL.AGGRESIVE)
			MySQLDatabase.save(this, true);
	}

	public int getTierOf(ArenaClass clazz) {
		return purchasedTiers.getOrDefault(clazz.getName(), 1);
	}

	public void setHigherTier(ArenaClass clazz) {
		final int currentTier = purchasedTiers.getOrDefault(clazz.getName(), 1);
		final int newTier = currentTier + 1;

		Valid.checkBoolean(newTier <= clazz.getTiers(), "Report / Cannot upgrade to a higher tier then class supports! " + newTier + " vs " + clazz.getTiers());

		purchasedTiers.override(clazz.getName(), newTier);
		save("Tiers", purchasedTiers);

		loadPurchasedTiers();
	}

	// --------------------------------------------------------------------------------
	// Other getters
	// --------------------------------------------------------------------------------

	public ClassCache getClassCache() {
		if (classCache == null)
			classCache = new ClassCache();

		return classCache;
	}

	public void setCurrentSetup(Arena arena) {
		Valid.checkBoolean(setupCache == null, "Setup cache already set for arena " + (setupCache != null ? setupCache.arena.getName() : "null") + "!");

		this.setupCache = new SetupCache(arena);
	}

	public void removeCurrentSetup() {
		setupCache = null;
	}

	public SetupCache getSetupCache() {
		Valid.checkNotNull(setupCache, "Player cache has not any setup!");

		return setupCache;
	}

	public boolean hasSetupCache() {
		return setupCache != null;
	}

	public void setCurrentArena(Player player, Arena arena) {
		if (arenaCache != null)
			throw new FoException("Arena cache already set! " + player.getName() + " plays " + CoreArenaPlugin.getArenaManager().findArena(player));

		this.arenaCache = new InArenaCache(player.getLocation());
	}

	public void removeCurrentArena() {
		arenaCache = null;
		classCache = null;
	}

	public InArenaCache getArenaCache() {
		Valid.checkNotNull(arenaCache, "Player cache has not any arena!");

		return arenaCache;
	}

	public boolean hasArenaCache() {
		return arenaCache != null;
	}
}