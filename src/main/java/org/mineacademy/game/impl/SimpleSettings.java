package org.mineacademy.game.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.exp.ExpFormula;
import org.mineacademy.game.hook.CoreHookManager;
import org.mineacademy.game.model.ArenaCommands;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaMaterialAllower;
import org.mineacademy.game.model.ArenaMaterialAllower.AllowMode;
import org.mineacademy.game.model.ArenaSettings;
import org.mineacademy.game.model.ArenaTrigger;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.NextPhaseMode;
import org.mineacademy.game.util.Constants;

import lombok.Getter;

@Getter
public final class SimpleSettings extends YamlConfig implements ArenaSettings {

	private final ArenaData dataSection;

	// Generic
	private int lifes;
	private int minimumTier;
	private int killHeight;

	private boolean allowOwnEquipment;
	private boolean allowNaturalDrops;
	private boolean respawningRandom;
	private boolean mobBurn;
	private boolean openClassMenu;
	private boolean explosiveArrowPlayerDamage;
	private boolean proceduralDamage;

	// Items
	private boolean igniteTnts;
	private boolean spawnWolves;
	private boolean launchFireballs;

	// Limits
	private int minimumPlayers;
	private int maximumPlayers;
	private int mobLimit;
	private int mobSpread;

	// Duration
	private int lobbyDurationSeconds;
	private int arenaDurationSeconds;
	private int phaseDurationSeconds;

	// Allowers
	private ArenaMaterialAllower breakingList;
	private ArenaMaterialAllower placeList;
	private ArenaMaterialAllower repairBlacklist;

	// Per phase
	private NextPhaseMode nextPhaseMode;
	private int nextPhaseWaitSeconds;

	private ArenaTrigger chestRefill;
	private int pvpPhase;
	private int lastPhase;
	private int autoStopPlayersLimit;
	private int endPhaseNoMonsters;
	private int maxPhase;
	private int spawnerActivationRadius;

	// Commands
	private ArenaCommands startCommands, lobbyStartCommands, phaseCommands, endCommands, finishCommands, playerLeaveCommands;

	// Experience overrides
	private ExpFormula expPhase = null, expMob = null;
	private final StrictMap<String, ExpFormula> expMobs = new StrictMap<>();

	// Rewards
	private Map<Integer /*wave*/, List<Object> /**/> rewardsEveryWave, rewardsOnWave;

	// Fart noises
	private SimpleSound playerJoinSound;
	private SimpleSound playerLeaveSound;
	private SimpleSound arenaStartSound;

	public SimpleSettings(File file) {
		this(file.getName().replace(".yml", ""));
	}

	public SimpleSettings(String arenaName) {
		this.dataSection = new SimpleData(arenaName);

		setHeader(Constants.Header.ARENA_FILE);
		loadConfiguration("prototype/arena.yml", "arenas/" + arenaName + ".yml");
	}

	@Override
	protected void onLoadFinish() {
		lifes = getInteger("Lifes");
		minimumTier = getInteger("Required_Class_Tier");
		killHeight = isSet("Kill_Height") ? getInteger("Kill_Height") : -1;
		allowOwnEquipment = getBoolean("Allow_Own_Equipment");
		allowNaturalDrops = getBoolean("Natural_Drops");
		respawningRandom = getBoolean("Random_Respawn_Location");
		mobBurn = getBoolean("Mob_Burn_On_Sunlight");
		mobSpread = getInteger("Mob_Spread");
		openClassMenu = getBoolean("Open_Class_Menu");
		explosiveArrowPlayerDamage = getBoolean("Explosive_Arrows_Damage_Players");
		proceduralDamage = getBoolean("Procedural_Damage");
		nextPhaseMode = get("Next_Phase_Mode", NextPhaseMode.class);
		nextPhaseWaitSeconds = (int) (TimeUtil.toTicks(getString("Next_Phase_Wait")) / 20);
		rewardsEveryWave = loadWaves("Rewards.Every");
		rewardsOnWave = loadWaves("Rewards.At");
		playerJoinSound = getSound("Sound.Player_Join");
		playerLeaveSound = getSound("Sound.Player_Leave");
		arenaStartSound = getSound("Sound.Arena_Start");
		spawnerActivationRadius = getInteger("Spawner_Activation_Radius");

		loadLimits();
		loadDurations();
		loadInteraction();
		loadPhases();
		loadCommands();
		loadExp();
	}

	@SuppressWarnings("rawtypes")
	private Map<Integer, List<Object>> loadWaves(String path) {
		final Map<Integer, List<Object>> map = new HashMap<>();

		if (isSet(path))
			for (final Map.Entry<Integer, List> entry : getMap(path, Integer.class, List.class).entrySet()) {
				final int wave = entry.getKey();
				final List<Object> parsed = new ArrayList<>();

				for (final Object rawObject : entry.getValue()) {
					final String raw = rawObject.toString();

					// Detected command
					if (raw.startsWith("/"))
						parsed.add(raw);
					else {
						final String[] parts = raw.split(":");
						Valid.checkBoolean(parts.length == 1 || parts.length == 2, "For reward items, please use formatting 'material_name:amount'. Given: " + raw);

						final CompMaterial material = CompMaterial.fromString(parts[0]);
						Valid.checkNotNull(material, "Unable to find rewards from material: " + parts[0]);

						final int amount = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;

						parsed.add(new Tuple<>(material, amount));
					}
				}

				map.put(wave, parsed);
			}

		return Collections.unmodifiableMap(map);
	}

	private void loadLimits() {
		pathPrefix("Player_Limit");

		minimumPlayers = MathUtil.range(getInteger("Minimum"), 1, 100);
		maximumPlayers = MathUtil.range(getInteger("Maximum"), 1, 100);

		pathPrefix(null);
		mobLimit = MathUtil.range(getInteger("Mob_Limit"), 0, 800);
	}

	private void loadDurations() {
		pathPrefix("Duration");

		lobbyDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(getString("Lobby")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 1800);
		arenaDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(getString("Arena")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 7200);
		phaseDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(getString("Phase")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 7200);
	}

	private void loadInteraction() {
		pathPrefix("Interaction");

		breakingList = getAllower("Allow_Breaking");
		placeList = getAllower("Allow_Placement");
		repairBlacklist = getAllower("Disallow_Auto_Repair");
		igniteTnts = getBoolean("Ignite_Tnts");
		spawnWolves = getBoolean("Spawn_Wolves");
		launchFireballs = getBoolean("Launch_Fireballs");
	}

	private void loadPhases() {
		pathPrefix("Phase");

		pvpPhase = getInteger("PvP");
		chestRefill = getTrigger("Chest_Refill");
		lastPhase = getInteger("Arena_End");
		autoStopPlayersLimit = getInteger("Players_End");
		endPhaseNoMonsters = getInteger("Arena_End_No_Monsters");
		maxPhase = getInteger("Max_Phase");
	}

	private void loadCommands() {
		startCommands = prepareCommands("Start");
		lobbyStartCommands = prepareCommands("Lobby_Start");
		phaseCommands = prepareCommands("Phase");
		finishCommands = prepareCommands("Finish");
		endCommands = prepareCommands("End");
		playerLeaveCommands = prepareCommands("Player_Leave");
	}

	private void loadExp() {
		pathPrefix("Experience");

		expMobs.clear();
		expPhase = isSet("Next_Phase") ? new ExpFormula(getString("Next_Phase")) : null;

		if (isSet("Kill"))
			for (final Entry<String, Object> e : getMap("Kill").asMap().entrySet()) {
				final ExpFormula formula = new ExpFormula(e.getValue().toString());

				if ("global".equalsIgnoreCase(e.getKey()))
					expMob = formula;
				else
					expMobs.put(e.getKey().toLowerCase(), formula);
			}
	}

	private ArenaCommands prepareCommands(String path) {
		pathPrefix("Commands." + path);

		final List<String> playerCmds = getStringList("Player");
		final List<String> consoleCmds = getStringList("Console");

		return new ArenaCommands(playerCmds, consoleCmds);
	}

	private ArenaMaterialAllower getAllower(String path) {
		final Object obj = getObject(path);

		if (obj == null)
			return new ArenaMaterialAllower(AllowMode.NONE);

		if (obj instanceof String && ((String) obj).equals("*"))
			return new ArenaMaterialAllower(AllowMode.ALL);

		if (obj instanceof List)
			return new ArenaMaterialAllower(getList(path, Material.class));

		throw new FoException("Please set either '*' or a list of materials at " + path + " in your " + getName() + ".yml file.");
	}

	private ArenaTrigger getTrigger(String path) {
		final Object obj = getObject(path);

		if (obj == null || obj instanceof Integer && ((Integer) obj).equals(-1))
			return null;

		if (obj instanceof Integer)
			return new ArenaTrigger((int) obj);

		if (obj instanceof List) {
			final Set<Integer> copy = new HashSet<>();

			for (final Object key : (List<?>) obj) {
				Valid.checkBoolean(key instanceof Integer, "Please only insert numbers at " + path + " in " + getName() + ", not '" + key + "' (" + key.getClass() + ")");

				copy.add((int) key);
			}

			return new ArenaTrigger(copy);
		}

		throw new FoException("Error parsing " + path + " in " + getName() + ".yml, got " + obj + " of class " + obj.getClass());
	}

	@Override
	public boolean allowOwnEquipment() {
		return allowOwnEquipment;
	}

	@Override
	public boolean allowNaturalDrops() {
		return allowNaturalDrops;
	}

	@Override
	public boolean allowMonstersBurn() {
		return mobBurn;
	}

	@Override
	public boolean igniteTnts() {
		return igniteTnts;
	}

	@Override
	public boolean spawnWolves() {
		return spawnWolves;
	}

	@Override
	public boolean launchFireballs() {
		return launchFireballs;
	}

	@Override
	public boolean openClassMenu() {
		return openClassMenu;
	}

	@Override
	public boolean explosiveArrowPlayerDamage() {
		return explosiveArrowPlayerDamage;
	}

	@Override
	public boolean hasProceduralDamage() {
		return proceduralDamage;
	}

	@Override
	public int getPhaseExp(int currentPhase) {
		final int result = (expPhase != null ? expPhase : Settings.Experience.Amount.NEXT_PHASE).calculate(currentPhase);
		Debugger.debug("rewards", "Calculated reward of phase " + currentPhase + " reward: " + result);

		return result;
	}

	@Override
	public int getExpFor(Entity entity, int currentPhase) {
		final String bossName = CoreHookManager.getBossName(entity);
		final String ename = (bossName != null ? bossName : entity.getType().toString()).toLowerCase();

		return (expMobs.contains(ename) ? expMobs.get(ename) : expMob != null ? expMob : Settings.Experience.Amount.getExpFor(entity)).calculate(currentPhase);
	}

	@Override
	public void removeSettingsFile() {
		delete();
	}
}
