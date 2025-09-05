package org.mineacademy.corearena.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.exception.IllegalSignException;
import org.mineacademy.corearena.impl.SimpleCuboidRegion;
import org.mineacademy.corearena.impl.SimpleIncompleteRegion;
import org.mineacademy.corearena.impl.SimpleLobby;
import org.mineacademy.corearena.impl.SimpleReward;
import org.mineacademy.corearena.impl.SimpleSidebarEdit;
import org.mineacademy.corearena.impl.SimpleSign;
import org.mineacademy.corearena.impl.SimpleSpawnPoint;
import org.mineacademy.corearena.impl.SimpleTier;
import org.mineacademy.corearena.manager.SimpleSigns;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.ArenaRegion;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSign.SignType;
import org.mineacademy.corearena.model.ArenaSigns;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.model.Lobby;
import org.mineacademy.corearena.model.Reward;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.RegionPoint;
import org.mineacademy.corearena.type.RewardType;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.settings.DataFileConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public final class AllData {

	@Getter
	private static final AllData instance = new AllData();

	private final Map<RewardType, List<Reward>> rewards = new HashMap<>();

	@Getter
	private boolean snapshotNotified = false;

	private AllData() {
		this.loadRewards();

		this.snapshotNotified = DataFileConfig.getInstance().getBoolean("Misc.Notified_Snapshot", false);
	}

	public DataFileConfig getDataYamlConfig() {
		return DataFileConfig.getInstance();
	}

	public void save() {
		this.getDataYamlConfig().save();
	}

	public void save(String path, Object object) {
		this.getDataYamlConfig().save(path, object);
	}

	public void set(String path, Object object) {
		this.getDataYamlConfig().set(path, object);
	}

	public <T> T get(String path, Class<T> typeOf) {
		return this.getDataYamlConfig().get(path, typeOf);
	}

	private void loadRewards() {
		this.rewards.clear();

		for (final RewardType type : RewardType.values())
			this.loadReward(type);
	}

	private void loadReward(RewardType type) {
		final List<Reward> loaded = new ArrayList<>();

		loaded.addAll(DataFileConfig.getInstance().getList("Rewards." + type.toString(), SimpleReward.class));

		this.rewards.put(type, loaded);
	}

	public void setRewards(RewardType type, List<Reward> items) {
		Valid.checkNotNull(items, "Report / Rewards cannot be null!");

		this.rewards.put(type, items);
		this.save("Rewards." + type.toString(), this.getRewards(type));

		this.loadRewards();
	}

	public void updateReward(Reward reward) {
		this.save("Rewards." + reward.getType().toString(), this.getRewards(reward.getType()));

		this.loadRewards();
	}

	public List<Reward> getRewards(RewardType type) {
		return this.rewards.computeIfAbsent(type, b -> new ArrayList<>());
	}

	public void setSnapshotNotified() {
		this.save("Misc.Notified_Snapshot", true);

		this.snapshotNotified = true;
	}

	public void setPendingLocation(UUID id, Location loc) {
		this.set("Misc.Locations." + id.toString(), loc);
	}

	public Location getPendingLocation(UUID id) {
		return this.get("Misc.Locations." + id.toString(), Location.class);
	}

	public ClassData loadClassData(String name) {
		return new ClassData(name);
	}

	public UpgradeData loadUpgradeData(String name) {
		return new UpgradeData(name);
	}

	public ArenaData loadArenaData(String name) {
		return new SimpleArenaData(name);
	}

	public ArenaPlayer loadPlayerData(UUID uniqueId, String name) {
		return new ArenaPlayer(uniqueId, name);
	}

	public final class ClassData {

		private final String path;

		@Getter
		private ItemStack icon;
		private List<ClassTier> tiers = new ArrayList<>();

		public ClassData(String className) {
			this.path = "Classes." + className;

			this.loadIcon();
			this.loadTiers();
		}

		private void loadIcon() {
			this.icon = get(this.path + ".Icon", ItemStack.class);
		}

		private void loadTiers() {
			final Set<ClassTier> tiersSorted = new TreeSet<>(Comparator.comparingInt(ClassTier::getTier));
			final List<SimpleTier> tiersList = getDataYamlConfig().getList(this.path + ".Tiers", SimpleTier.class, this.getClassName());

			if (tiersList != null)
				tiersSorted.addAll(tiersList);

			// Fix order
			int startingTier = 1;

			for (final ClassTier tier : tiersSorted)
				tier.setTier(startingTier++);

			this.tiers = new ArrayList<>(tiersSorted);
		}

		public void setIcon(ItemStack item) {
			save(this.path + ".Icon", item);

			this.loadIcon();
		}

		public int getTiers() {
			return this.tiers.size();
		}

		public ClassTier getTier(int tier) {
			Valid.checkBoolean(tier > 0, "Cannot get the zero tier");

			return tier > this.tiers.size() ? null : this.tiers.get(tier - 1);
		}

		public void setTierNoSave(ClassTier tier) {
			if (this.getTiers() >= tier.getTier())
				this.tiers.set(tier.getTier() - 1, tier);
			else
				this.tiers.add(tier);
		}

		public void removeTier(ClassTier tier) {
			this.tiers.remove(tier);

			this.saveAndReload();
		}

		public void saveUpdatedTier(ClassTier tier) {
			this.setTierNoSave(tier);

			this.saveAndReload();
		}

		private void saveAndReload() {
			save(this.path + ".Tiers", this.tiers);

			this.loadTiers();
		}

		public String getClassName() {
			return this.path.replace("Classes.", "");
		}

		public boolean isValid() {
			return !getDataYamlConfig().getMap(this.path).isEmpty();
		}

		public void clear() {
			set(this.path, null);
		}
	}

	public final class UpgradeData {

		private final String path;

		@Getter
		private ArenaClass arenaClass;

		@Getter
		private ItemStack[] items;

		public UpgradeData(String upgradeName) {
			this.path = ".Upgrades." + upgradeName;

			this.loadArenaClass();
			this.loadItems();
		}

		private void loadArenaClass() {
			final String className = getDataYamlConfig().getString(this.path + ".Class");

			if (className != null) {
				this.arenaClass = CoreArenaPlugin.getClassManager().findClass(className);

				if (this.arenaClass == null) {
					Common.log("&cThe upgrade " + this.getUpgradeName() + " has assigned non-existing class " + className + ", removing..");

					save(this.path + ".Class", null);
				}
			} else
				this.arenaClass = null;
		}

		private void loadItems() {
			final List<ItemStack> list = getDataYamlConfig().getList(this.path + ".Items", ItemStack.class);

			if (list != null)
				this.items = list.toArray(new ItemStack[list.size()]);
		}

		public void setArenaClass(ArenaClass clazz) {
			this.arenaClass = clazz;

			save(this.path + ".Class", clazz != null ? clazz.getName() : null);
		}

		public void setItems(ItemStack[] items) {
			this.items = items;

			save(this.path + ".Items", items);
		}

		public String getUpgradeName() {
			return this.path.replace("Upgrades.", "");
		}

		public boolean isValid() {
			return !getDataYamlConfig().getMap(this.path).isEmpty();
		}

		public void clear() {
			set(this.path, null);
		}
	}

	@Getter
	public final class SimpleArenaData implements ArenaData {

		private final String path;

		/**
		 * Is the arena enabled for playing?
		 */
		@Getter
		private boolean enabled;

		/**
		 * The lobby from the data section
		 */
		private Lobby lobby;

		/**
		 * The region from the data section
		 */
		private ArenaRegion region;

		/**
		 * Die Sign von dem Datasektion
		 */
		private ArenaSigns signs;

		/**
		 * The icon
		 */
		private ItemStack icon;

		/**
		 * Spawn points
		 */
		private Map<SpawnPointType, List<SpawnPoint>> spawnPoints;

		public SimpleArenaData(String arenaName) {
			this.path = "Arena." + arenaName;

			this.loadBasics();
			this.loadLobby();
			this.loadRegion();
			this.loadIcon();
			this.loadSpawnPoints();
		}

		@Override
		public void onPostLoad() {
			this.loadSigns();
		}

		private void loadBasics() {
			this.enabled = getDataYamlConfig().getBoolean(this.path + ".Enabled", true);
		}

		private void loadLobby() {
			final Location location = get(this.path + ".Lobby.Location", Location.class);

			if (location != null)
				this.lobby = new SimpleLobby(location);
		}

		private void loadRegion() {
			final Location primary = get(this.path + ".Region.Primary", Location.class);
			final Location secondary = get(this.path + ".Region.Secondary", Location.class);

			if (primary != null && secondary != null)
				this.region = new SimpleCuboidRegion(primary, secondary);
			else
				this.region = new SimpleIncompleteRegion(primary, secondary);
		}

		private void loadSigns() {
			final Map<SignType, List<ArenaSign>> loaded = new HashMap<>();

			for (final SignType type : SignType.values()) {
				final List<ArenaSign> loadedSigns = new ArrayList<>();
				final List<?> signsRaw = (List<?>) getDataYamlConfig().getObject(this.path + ".Signs." + type);

				boolean forceResave = false;

				if (signsRaw != null && !signsRaw.isEmpty() && !(signsRaw.get(0) instanceof SimpleSign)) {
					final List<Map<String, Object>> maps = (List<Map<String, Object>>) signsRaw;

					for (final Map<String, Object> map : maps)
						if (!map.isEmpty()) {
							ArenaSign sign;

							try {
								sign = SimpleSign.deserialize(SerializedMap.fromObject(map), type, this.getArenaName());

							} catch (final IllegalSignException ex) {
								Common.log("[Arena " + this.getArenaName() + "] " + ex.getProblem());
								forceResave = true;

								continue;
							}

							loadedSigns.add(sign);
						}
				} else if (signsRaw != null)
					signsRaw.forEach(sign -> loadedSigns.add((SimpleSign) sign));

				if (forceResave)
					save(this.path + ".Signs." + type, loadedSigns);

				loaded.put(type, loadedSigns);
			}

			this.signs = new SimpleSigns(loaded);
		}

		private void loadIcon() {
			this.icon = get(this.path + ".Icon", ItemStack.class);
		}

		private void loadSpawnPoints() {
			this.spawnPoints = new HashMap<>();

			for (final SpawnPointType type : SpawnPointType.values())
				this.loadSpawnPoint(type);
		}

		private void loadSpawnPoint(SpawnPointType type) {
			Valid.checkNotNull(this.spawnPoints, "Report / Spawnpoints not yet set!");
			Valid.checkBoolean(!this.spawnPoints.containsKey(type), type + " already loaded!");

			final List<?> pointsRaw = getDataYamlConfig().getList(this.path + ".Spawnpoint." + type);

			if (pointsRaw != null) {
				final List<SpawnPoint> loaded = new ArrayList<>();

				for (final Object obj : pointsRaw)
					if (obj instanceof SimpleSpawnPoint)
						loaded.add((SimpleSpawnPoint) obj);

					else if (obj instanceof Map) {
						final Map<String, Object> map = (Map<String, Object>) obj;

						if (!map.isEmpty()) {
							final SimpleSpawnPoint point = SimpleSpawnPoint.deserialize(SerializedMap.fromObject(map), type);

							loaded.add(point);
						}
					}

				this.spawnPoints.put(type, loaded);
			}
		}

		@Override
		public void setRegion(Location loc, RegionPoint point) {
			Valid.checkNotNull(loc.getWorld(), "Region point " + point + " asserted a location lacking world! Loc: " + loc);

			save(this.path + ".Region." + point, loc);

			if (this.region == null) {
				this.region = point == RegionPoint.PRIMARY ? new SimpleIncompleteRegion(loc, null) : new SimpleIncompleteRegion(null, loc);

			} else {
				final Location primary = point == RegionPoint.PRIMARY ? loc : this.region.getPrimary();
				final Location secondary = point == RegionPoint.SECONDARY ? loc : this.region.getSecondary();

				if (primary != null && secondary != null)
					this.region = new SimpleCuboidRegion(primary, secondary);
				else
					this.region = new SimpleIncompleteRegion(primary, secondary);
			}
		}

		@Override
		public void removeRegion(RegionPoint point) {
			save(this.path + ".Region." + point, null);

			this.region = null;
		}

		@Override
		public void setLobby(Location loc) {
			Valid.checkNotNull(loc.getWorld(), "Lobby asserted a location lacking world! Loc: " + loc);

			save(this.path + ".Lobby.Location", loc);

			this.lobby = new SimpleLobby(loc);
		}

		@Override
		public void removeLobby() {
			save(this.path + ".Lobby.Location", null);

			this.lobby = null;
		}

		@Override
		public void addSign(ArenaSign sign) {
			final List<ArenaSign> loaded = this.signs.getSigns(sign.getType());
			loaded.add(sign);

			save(this.path + ".Signs." + sign.getType(), loaded);
		}

		@Override
		public void removeSign(Location loc) {
			final ArenaSign sign = this.signs.getSignAt(loc);
			Valid.checkNotNull(sign, "Report / No sign found at " + SerializeUtil.serializeLocation(loc));

			this.removeSign(sign);
		}

		@Override
		public void removeSign(ArenaSign sign) {
			Valid.checkNotNull(sign, "Report / Cannot remove null signs");
			Valid.checkBoolean(sign.getLocation().getBlock().getType() == Material.AIR, "Report / Znitch cedulu od verchu");

			final List<ArenaSign> loaded = this.signs.getSigns(sign.getType());
			loaded.remove(sign);

			save(this.path + ".Signs." + sign.getType(), loaded);
		}

		@Override
		public boolean hasIcon() {
			return this.getIcon() != null && !this.getIcon().equals(Constants.Items.DEFAULT_ICON);
		}

		@Override
		public ItemStack getIcon() {
			return Common.getOrDefault(this.icon, Constants.Items.DEFAULT_ICON);
		}

		@Override
		public void setIcon(ItemStack icon) {
			save(this.path + ".Icon", icon);
		}

		@Override
		public void updateSpawnPoint(SpawnPoint point) {
			final List<SpawnPoint> existing = this.spawnPoints.get(point.getType());

			sanityCheck:
			{
				for (int i = 0; i < existing.size(); i++) {
					final SpawnPoint previous = existing.get(i);

					if (Valid.locationEquals(previous.getLocation(), point.getLocation())) {
						existing.set(i, point);

						break sanityCheck;
					}
				}

				existing.add(point); // Add the point if does not exist
			}

			this.saveSpawnPoints(point.getType());
		}

		@Override
		public void addSpawnPoint(SpawnPoint point) {
			Valid.checkNotNull(point, "Report / Point cannot be null!");

			this.getSpawnPoints(point.getType()).add(point);
			this.saveSpawnPoints(point.getType());
		}

		@Override
		public void removeSpawnPoint(SpawnPointType type, Location loc) {
			final SpawnPoint found = this.findSpawnPoint(loc);

			if (found != null) {
				this.spawnPoints.get(type).remove(found);
				this.saveSpawnPoints(type);
			}
		}

		private void saveSpawnPoints(SpawnPointType type) {
			save(this.path + ".Spawnpoint." + type, this.getSpawnPoints(type));
		}

		@Override
		public List<SpawnPoint> getSpawnPoints(SpawnPointType type) {
			if (this.spawnPoints == null)
				this.spawnPoints = new HashMap<>();

			if (!this.spawnPoints.containsKey(type))
				this.spawnPoints.put(type, new ArrayList<>());

			return this.spawnPoints.get(type);
		}

		@Override
		public SpawnPoint findSpawnPoint(Location location) {
			if (this.spawnPoints != null)
				for (final List<SpawnPoint> points : this.spawnPoints.values())
					for (final SpawnPoint point : points)
						if (Valid.locationEquals(point.getLocation(), location))
							return point;

			return null;
		}

		@Override
		public ArenaRegion getRegion() {
			if (this.region == null)
				this.loadRegion();

			return this.region;
		}

		@Override
		public ArenaSigns getSigns() {
			Valid.checkNotNull(this.signs, "signs = null");

			return this.signs;
		}

		@Override
		public void setEnabled(boolean enabled) {
			save(this.path + ".Enabled", enabled);

			this.enabled = enabled;
		}

		private String getArenaName() {
			return this.path.replace("Arena.", "");
		}

		@Override
		public String getObjectName() {
			return "arena";
		}

		@Override
		public boolean isValid() {
			return !getDataYamlConfig().getMap(this.path).isEmpty();
		}

		@Override
		public void clear() {
			set(this.path, null);
		}
	}

	public final class ArenaPlayer {

		private final String path;

		// --------------------------------------------------------------------------------
		// Data saved to data file
		// --------------------------------------------------------------------------------

		@Getter
		private final UUID uniqueId;

		@Getter
		private final String playerName;

		@Getter
		private int nuggets;

		private Map<String, Integer> purchasedTiers = new HashMap<>();

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
			public List<PermissionAttachment> givenPermissions = new ArrayList<>();

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

			public void giveAndShowExp(Player player, int expToGive) {
				final double totalExp = this.exp + expToGive;

				// Calculate levels to give
				final double levelsToGive = this.level + totalExp / Settings.Experience.EXP_PER_LEVEL;

				final int newLevel = (int) MathUtil.floor(levelsToGive);

				// Calculate rest experience
				final String[] split = Double.toString(levelsToGive).split("\\.");
				Valid.checkBoolean(split.length == 2, "Malformed level calculation length " + split.length);

				final double rest = Double.parseDouble("0." + split[1]);
				final double expPerLevel = Settings.Experience.EXP_PER_LEVEL;

				final int newExp = (int) Math.round(rest * (expPerLevel >= 0 ? expPerLevel : 0));

				this.level = newLevel;
				this.exp = newExp;

				this.updateExpBar(player);
			}

			public void takeAndShowLevels(Player player, int levels) {
				Valid.checkBoolean(this.level >= levels, "Cannot take more levels than player have (" + this.level + " < " + levels + ")!");
				this.level = this.level - levels;

				this.updateExpBar(player);
			}

			public void setLevel(Player player, int level) {
				this.level = level;

				this.updateExpBar(player);
			}

			private void updateExpBar(Player player) {
				final double expPerLevel = Settings.Experience.EXP_PER_LEVEL;

				player.setExp((float) (this.exp / (expPerLevel >= 0 ? expPerLevel : 1)));

				player.setLevel(this.level);
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
				if (this.sidebar != null && !this.sidebar.isViewing(player))
					this.sidebar.show(player);
			}

			public void hideSidebar(Player player) {
				if (this.sidebar != null && this.sidebar.isViewing(player))
					this.sidebar.hide(player);
			}
		}

		// --------------------------------------------------------------------------------
		// Loading
		// --------------------------------------------------------------------------------

		public ArenaPlayer(UUID uuid, String name) {
			this.path = "Players." + uuid.toString();

			this.uniqueId = uuid;
			this.playerName = name;

			this.nuggets = getDataYamlConfig().getInteger(this.path + ".Nuggets", 0);

			this.loadPurchasedTiers();
		}

		private void loadPurchasedTiers() {
			final Map<String, Integer> tiersRaw = getDataYamlConfig().getMap(this.path + ".Tiers", String.class, Integer.class);

			this.purchasedTiers = new HashMap<>();

			for (final Map.Entry<String, Integer> entry : tiersRaw.entrySet()) {
				final String tierName = entry.getKey();
				final int level = entry.getValue();

				this.purchasedTiers.put(tierName, level);
			}
		}

		// --------------------------------------------------------------------------------
		// Gettings for data file
		// --------------------------------------------------------------------------------

		public void setNuggets(int nuggets) {
			this.nuggets = nuggets;

			set(this.path + ".Nuggets", nuggets);
		}

		public int getTierOf(ArenaClass clazz) {
			return this.purchasedTiers.getOrDefault(clazz.getName(), 1);
		}

		public void setHigherTier(ArenaClass clazz) {
			final int currentTier = this.purchasedTiers.getOrDefault(clazz.getName(), 1);
			final int newTier = currentTier + 1;

			Valid.checkBoolean(newTier <= clazz.getTiers(), "Report / Cannot upgrade to a higher tier then class supports! " + newTier + " vs " + clazz.getTiers());

			this.purchasedTiers.put(clazz.getName(), newTier);
			save(this.path + ".Tiers", this.purchasedTiers);

			this.loadPurchasedTiers();
		}

		// --------------------------------------------------------------------------------
		// Other getters
		// --------------------------------------------------------------------------------

		public ClassCache getClassCache() {
			if (this.classCache == null)
				this.classCache = new ClassCache();

			return this.classCache;
		}

		public void setCurrentSetup(Arena arena) {
			Valid.checkBoolean(this.setupCache == null, "Setup cache already set for arena " + (this.setupCache != null ? this.setupCache.arena.getName() : "null") + "!");

			this.setupCache = new SetupCache(arena);
		}

		public void removeCurrentSetup() {
			this.setupCache = null;
		}

		public SetupCache getSetupCache() {
			Valid.checkNotNull(this.setupCache, "Player cache has not any setup!");

			return this.setupCache;
		}

		public boolean hasSetupCache() {
			return this.setupCache != null;
		}

		public void setCurrentArena(Player player, Arena arena) {
			if (this.arenaCache != null)
				throw new FoException("Arena cache already set! " + player.getName() + " plays " + CoreArenaPlugin.getArenaManager().findArena(player));

			this.arenaCache = new InArenaCache(player.getLocation());
		}

		public void removeCurrentArena() {
			this.arenaCache = null;
			this.classCache = null;
		}

		public InArenaCache getArenaCache() {
			Valid.checkNotNull(this.arenaCache, "Player cache has not any arena!");

			return this.arenaCache;
		}

		public boolean hasArenaCache() {
			return this.arenaCache != null;
		}
	}
}
