package org.mineacademy.game.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.settings.YamlSectionConfig;
import org.mineacademy.game.exception.IllegalSignException;
import org.mineacademy.game.manager.SimpleSigns;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaRegion;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSigns;
import org.mineacademy.game.model.Lobby;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.model.ArenaSign.SignType;
import org.mineacademy.game.type.RegionPoint;
import org.mineacademy.game.type.SpawnPointType;
import org.mineacademy.game.util.Constants;

import lombok.Getter;

@Getter
public final class SimpleData extends YamlSectionConfig implements ArenaData {

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
	private StrictMap<SpawnPointType, List<SpawnPoint>> spawnPoints;

	public SimpleData(String arenaName) {
		super("Arena." + arenaName);

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		if (!isSetAbsolute("Created"))
			save("Created", TimeUtil.currentTimeSeconds());

		loadBasics();
		loadLobby();
		loadRegion();
		loadIcon();
		loadSpawnPoints();
	}

	@Override
	public void onPostLoad() {
		loadSigns();
	}

	private void loadBasics() {
		enabled = getBoolean("Enabled", true);
	}

	private void loadRegion() {
		final Location primary = getLocation("Region.Primary");
		final Location secondary = getLocation("Region.Secondary");

		if (primary != null && secondary != null)
			region = new SimpleCuboidRegion(primary, secondary);
		else
			region = new SimpleIncompleteRegion(primary, secondary);
	}

	private void loadLobby() {
		final Location loc = getLocation("Lobby.Location");

		if (loc != null)
			lobby = new SimpleLobby(loc);
	}

	private void loadSigns() {
		final StrictMap<SignType, List<ArenaSign>> loaded = new StrictMap<>();

		for (final SignType type : SignType.values()) {
			final StrictList<ArenaSign> loadedSigns = new StrictList<>();
			final List<?> signsRaw = getList("Signs." + type);
			boolean forceResave = false;

			if (signsRaw != null) {
				final ArrayList<HashMap<String, Object>> maps = (ArrayList<HashMap<String, Object>>) signsRaw;

				for (final HashMap<String, Object> map : maps)
					if (!map.isEmpty()) {
						ArenaSign sign;

						try {
							sign = SimpleSign.deserialize(SerializedMap.of(map), type, getArenaName());
						} catch (final IllegalSignException ex) {
							Common.log("[Arena " + getArenaName() + "] " + ex.getProblem());
							forceResave = true;

							continue;
						}

						loadedSigns.add(sign);
					}
			}

			if (forceResave)
				save("Signs." + type, loadedSigns);

			loaded.put(type, loadedSigns.getSource());
		}

		signs = new SimpleSigns(loaded);
	}

	private void loadIcon() {
		final Object obj = getObject("Icon");

		if (obj != null) {
			Valid.checkBoolean(obj instanceof ItemStack, "Unexpected icon of " + getArenaName() + ": " + obj);

			this.icon = (ItemStack) obj;
		} else
			this.icon = null;
	}

	private void loadSpawnPoints() {
		spawnPoints = new StrictMap<>();

		for (final SpawnPointType type : SpawnPointType.values())
			loadSpawnPoint(type);
	}

	private void loadSpawnPoint(SpawnPointType type) {
		Valid.checkNotNull(spawnPoints, "Report / Spawnpoints not yet set!");
		Valid.checkBoolean(!spawnPoints.contains(type), type + " already loaded!");

		final List<?> pointsRaw = getList("Spawnpoint." + type);

		if (pointsRaw != null) {
			final StrictList<SpawnPoint> loaded = new StrictList<>();

			for (final Object obj : pointsRaw)
				if (obj instanceof SimpleSpawnPoint)
					loaded.add((SimpleSpawnPoint) obj);

				else if (obj instanceof Map) {
					final Map<String, Object> map = (Map<String, Object>) obj;

					if (!map.isEmpty()) {
						final SimpleSpawnPoint point = SimpleSpawnPoint.deserialize(SerializedMap.of(map), type);

						loaded.add(point);
					}
				}

			spawnPoints.put(type, loaded.getSource());
		}
	}

	@Override
	public void setRegion(Location loc, RegionPoint point) {
		Valid.checkNotNull(loc.getWorld(), "Region point " + point + " asserted a location lacking world! Loc: " + loc);

		save("Region." + point, loc);

		loadRegion();
	}

	@Override
	public void removeRegion(RegionPoint point) {
		save("Region." + point, null);

		region = null;
	}

	@Override
	public void setLobby(Location loc) {
		Valid.checkNotNull(loc.getWorld(), "Lobby asserted a location lacking world! Loc: " + loc);

		save("Lobby.Location", loc);

		loadLobby();
	}

	@Override
	public void removeLobby() {
		save("Lobby.Location", null);

		lobby = null;
	}

	@Override
	public void addSign(ArenaSign sign) {
		final List<ArenaSign> loaded = signs.getSigns(sign.getType());

		loaded.add(sign);

		save("Signs." + sign.getType(), loaded);
		loadSigns();
	}

	@Override
	public void removeSign(Location loc) {
		final ArenaSign sign = signs.getSignAt(loc);
		Valid.checkNotNull(sign, "Report / No sign found at " + Common.shortLocation(loc));

		removeSign(sign);
	}

	@Override
	public void removeSign(ArenaSign sign) {
		Valid.checkNotNull(sign, "Report / Cannot remove null signs");
		Valid.checkBoolean(sign.getLocation().getBlock().getType() == Material.AIR, "Report / Znitch cedulu od verchu");

		final List<ArenaSign> loaded = signs.getSigns(sign.getType());
		loaded.remove(sign);

		save("Signs." + sign.getType(), loaded);
		loadSigns();
	}

	@Override
	public boolean hasIcon() {
		return getIcon() != null && !getIcon().equals(Constants.Items.DEFAULT_ICON);
	}

	@Override
	public ItemStack getIcon() {
		return Common.getOrDefault(icon, Constants.Items.DEFAULT_ICON);
	}

	@Override
	public void setIcon(ItemStack icon) {
		save("Icon", icon);

		loadIcon();
	}

	@Override
	public void updateSpawnPoint(SpawnPoint point) {
		final List<SpawnPoint> existing = spawnPoints.get(point.getType());

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

		saveAndLoadSpawn(point.getType());
	}

	@Override
	public void addSpawnPoint(SpawnPoint point) {
		Valid.checkNotNull(point, "Report / Point cannot be null!");

		getSpawnPoints(point.getType()).add(point);
		saveAndLoadSpawn(point.getType());
	}

	@Override
	public void removeSpawnPoint(SpawnPointType type, Location loc) {
		final SpawnPoint found = findSpawnPoint(loc);
		Valid.checkNotNull(found, "Report / There is no spawnpoint at " + Common.shortLocation(loc));

		spawnPoints.get(type).remove(found);
		saveAndLoadSpawn(type);
	}

	private void saveAndLoadSpawn(SpawnPointType type) {
		save("Spawnpoint." + type, getSpawnPoints(type));

		loadSpawnPoints();
	}

	@Override
	public List<SpawnPoint> getSpawnPoints(SpawnPointType type) {
		if (spawnPoints == null)
			spawnPoints = new StrictMap<>();

		if (!spawnPoints.contains(type))
			spawnPoints.put(type, new ArrayList<>());

		return spawnPoints.get(type);
	}

	@Override
	public SpawnPoint findSpawnPoint(Location loc) {
		if (spawnPoints != null)
			for (final List<SpawnPoint> points : spawnPoints.values())
				for (final SpawnPoint point : points)
					if (Valid.locationEquals(point.getLocation(), loc))
						return point;

		return null;
	}

	@Override
	public ArenaRegion getRegion() {
		if (region == null)
			loadRegion();

		return region;
	}

	@Override
	public ArenaSigns getSigns() {
		Valid.checkNotNull(signs, "signs = null");

		return signs;
	}

	@Override
	public void setEnabled(boolean enabled) {
		save("Enabled", enabled);

		loadBasics();
	}

	private String getArenaName() {
		return getSection().replace("Arena.", "");
	}
}
